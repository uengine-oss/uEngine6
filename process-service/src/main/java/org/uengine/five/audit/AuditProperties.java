package org.uengine.five.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 감사 로그 설정.
 * 저장소 타입(jpa/file/composite), 파일 경로·포맷 등 유연하게 변경 가능.
 */
@Component
@ConfigurationProperties(prefix = "uengine.audit")
public class AuditProperties {

    public static final String SINK_NONE = "none";
    public static final String SINK_JPA = "jpa";
    public static final String SINK_FILE = "file";
    public static final String SINK_COMPOSITE = "composite";

    /** 사용할 Sink: none | jpa | file | composite. none이면 감사 테이블/저장소 불필요(코어 실행에 필수 아님). */
    private String sink = SINK_NONE;

    /** composite 일 때 사용할 Sink 이름 목록 (예: jpa,file) */
    private List<String> sinkNames = new ArrayList<>();

    /** 향후 사용: 감사 레벨(none|basic|audit|full). 미구현 시 모든 이벤트 기록. */
    // private String level = "full";

    private File file = new File();

    public String getSink() { return sink; }
    public void setSink(String sink) { this.sink = sink; }

    public List<String> getSinkNames() { return sinkNames; }
    public void setSinkNames(List<String> sinkNames) { this.sinkNames = sinkNames; }

    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }

    public static class File {
        /** 감사 로그 전용 저장 경로 (application.yml uengine.audit.file.basePath, definition 경로와 별도) */
        private String basePath = "";
        /** 파일 형식: json (한 줄에 한 이벤트) | json-pretty */
        private String format = "json";

        public String getBasePath() { return basePath; }
        public void setBasePath(String basePath) { this.basePath = basePath; }

        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
    }
}
