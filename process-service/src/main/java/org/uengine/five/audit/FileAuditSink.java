package org.uengine.five.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 감사 로그를 인스턴스별로 파일 저장.
 * 경로: 인스턴스 번호 기준 1단계 50000, 2단계 10000 분할, 폴더·파일명 10자리 고정 → basePath/0000000000/0000000000/0000000003.log
 */
@Component
public class FileAuditSink implements AuditSink {

    private static final Logger log = LoggerFactory.getLogger(FileAuditSink.class);
    private static final String FALLBACK_AUDIT_DIR = "bpm-audit-log";
    private static final int FIRST_LEVEL_DIVISOR = 50000;
    private static final int SECOND_LEVEL_DIVISOR = 10000;

    private final AuditProperties.File fileProps;
    private final ObjectMapper objectMapper;

    private final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();

    public FileAuditSink(AuditProperties auditProperties, ObjectMapper objectMapper) {
        this.fileProps = auditProperties != null ? auditProperties.getFile() : new AuditProperties.File();
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public void record(AuditEvent event) {
        if (event == null) return;
        Long instId = event.getInstId() != null ? event.getInstId() : event.getRootInstId();
        if (instId == null) return;
        String line = toLine(event);
        if (line == null) return;
        String fileKey = instanceFileKey(instId);
        BufferedWriter w = writerFor(fileKey);
        if (w == null) return;
        try {
            synchronized (w) {
                w.write(line);
                w.newLine();
                w.flush();
            }
        } catch (IOException e) {
            log.warn("Audit file write failed for {}: {}", fileKey, e.getMessage());
        }
    }

    @Override
    public List<AuditEvent> listByRootInstanceId(Long rootInstId, int limit) {
        if (rootInstId == null) return Collections.emptyList();
        return listByInstanceId(rootInstId, limit);
    }

    private List<AuditEvent> listByInstanceId(Long instId, int limit) {
        try {
            Path base = resolveBasePath();
            Path logFile = base.resolve(instanceDirPath(instId)).resolve(instanceFileName(instId));
            if (!Files.isRegularFile(logFile)) return Collections.emptyList();
            List<AuditEvent> events = new ArrayList<>();
            readAuditFile(logFile, events);
            events.sort((a, b) -> {
                Date da = a.getOccurredAt() != null ? a.getOccurredAt() : new Date(0);
                Date db = b.getOccurredAt() != null ? b.getOccurredAt() : new Date(0);
                return db.compareTo(da);
            });
            int size = limit > 0 ? Math.min(limit, events.size()) : events.size();
            return events.subList(0, size);
        } catch (Exception e) {
            log.warn("Failed to list audit for instance {}: {}", instId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean supportsQuery() { return true; }

    /**
     * 인스턴스 번호 기준 경로: (instId/50000)/((instId%50000)/10000)/00000003.log
     */
    private String instanceFileKey(Long instId) {
        return instanceDirPath(instId) + "/" + instanceFileName(instId);
    }

    /** 1단계 50000, 2단계 10000 분할 디렉터리 경로 (상대경로, 폴더명 10자리 고정) */
    private String instanceDirPath(Long instId) {
        long first = instId / FIRST_LEVEL_DIVISOR;
        long second = (instId % FIRST_LEVEL_DIVISOR) / SECOND_LEVEL_DIVISOR;
        return String.format("%010d/%010d", first, second);
    }

    /** 인스턴스 번호 10자리 고정 파일명 (예: 0000000003.log) */
    private String instanceFileName(Long instId) {
        return String.format("%010d.log", instId);
    }

    private Path resolveBasePath() {
        String basePath = fileProps != null ? fileProps.getBasePath() : null;
        if (basePath == null || basePath.trim().isEmpty()) {
            basePath = System.getProperty("java.io.tmpdir") + "/" + FALLBACK_AUDIT_DIR;
            log.info("Audit file basePath not set, using: {}", basePath);
        } else {
            basePath = basePath.trim();
        }
        return Paths.get(basePath).toAbsolutePath();
    }

    private void readAuditFile(Path logFile, List<AuditEvent> events) throws IOException {
        try (Stream<String> lines = Files.lines(logFile, StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                if (line == null || line.trim().isEmpty()) return;
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = objectMapper.readValue(line, Map.class);
                    AuditEvent event = toEvent(map);
                    if (event != null) events.add(event);
                } catch (Exception e) {
                    log.debug("Parse audit line failed: {}", e.getMessage());
                }
            });
        }
    }

    private AuditEvent toEvent(Map<String, Object> map) {
        try {
            AuditEvent event = new AuditEvent();
            if (map.get("eventType") != null) {
                try {
                    event.setEventType(AuditEventType.valueOf(map.get("eventType").toString()));
                } catch (Exception e) {
                    event.setEventType(AuditEventType.CUSTOM);
                }
            }
            if (map.get("rootInstId") != null) {
                Object v = map.get("rootInstId");
                event.setRootInstId(v instanceof Number ? ((Number) v).longValue() : Long.parseLong(v.toString()));
            }
            if (map.get("instId") != null) {
                Object v = map.get("instId");
                event.setInstId(v instanceof Number ? ((Number) v).longValue() : Long.parseLong(v.toString()));
            }
            if (map.get("occurredAt") != null) {
                Object v = map.get("occurredAt");
                if (v instanceof Date) event.setOccurredAt((Date) v);
                else if (v instanceof String) {
                    String s = v.toString();
                    if (s.contains("T") && s.contains("+")) {
                        try {
                            java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                            event.setOccurredAt(iso.parse(s));
                        } catch (Exception e) { /* ignore */ }
                    }
                } else if (v instanceof Number) event.setOccurredAt(new Date(((Number) v).longValue()));
            }
            if (map.get("tracingTag") != null) event.setTracingTag(map.get("tracingTag").toString());
            if (map.get("actor") != null) event.setActor(map.get("actor").toString());
            if (map.get("payload") != null && map.get("payload") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) map.get("payload");
                event.setPayload(payload);
            }
            return event;
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedWriter writerFor(String fileKey) {
        return writers.computeIfAbsent(fileKey, k -> {
            try {
                Path base = resolveBasePath();
                Path file = base.resolve(k);
                Files.createDirectories(file.getParent());
                BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                log.info("Audit log file opened: {}", file);
                return w;
            } catch (IOException e) {
                log.error("Could not create audit log file {}: {} - check path and permissions", fileKey, e.getMessage(), e);
                return null;
            }
        });
    }

    private String toLine(AuditEvent event) {
        try {
            if ("json-pretty".equalsIgnoreCase(fileProps.getFormat())) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(toMap(event));
            }
            return objectMapper.writeValueAsString(toMap(event));
        } catch (Exception e) {
            log.warn("Audit event serialize failed: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> toMap(AuditEvent event) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("eventType", event.getEventType() != null ? event.getEventType().name() : "CUSTOM");
        m.put("rootInstId", event.getRootInstId());
        m.put("instId", event.getInstId());
        m.put("occurredAt", event.getOccurredAt());
        m.put("tracingTag", event.getTracingTag());
        m.put("actor", event.getActor());
        if (event.getPayload() != null) m.put("payload", event.getPayload());
        return m;
    }

    @PreDestroy
    public void closeWriters() {
        for (Map.Entry<String, BufferedWriter> e : writers.entrySet()) {
            try {
                e.getValue().close();
            } catch (IOException ex) {
                log.warn("Close audit writer {} failed: {}", e.getKey(), ex.getMessage());
            }
        }
        writers.clear();
    }
}
