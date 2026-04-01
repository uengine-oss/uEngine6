package org.uengine.modeling.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JPA 기반 저장소 구현이 공통으로 사용하는 경로/리소스 유틸리티를 제공한다.
 * 실제 영속화는 하위 구현체가 담당한다.
 */
public abstract class JPAStorage extends AbstractStorage {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    protected String normalizePath(IResource resource) {
        return normalizePath(resource != null ? resource.getPath() : null);
    }

    protected String normalizePath(String path) {
        String normalized = path == null ? "" : path.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        if (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    protected String fileNameOf(String path) {
        String normalized = normalizePath(path);
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(idx + 1) : normalized;
    }

    protected String extensionOf(String path) {
        String fileName = fileNameOf(path);
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx + 1) : "";
    }

    protected String parentOf(String path) {
        String normalized = normalizePath(path);
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(0, idx) : "";
    }

    protected String nowAsString() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    protected IResource resourceOf(String normalizedPath, boolean directory) {
        if (directory) {
            return new ContainerResource(normalizedPath);
        }
        return new DefaultResource(normalizedPath);
    }

    protected InputStream inputStreamOf(String content) {
        byte[] bytes = content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public OutputStream getOutputStream(IResource resource) throws Exception {
        throw new UnsupportedOperationException("JPAStorage does not expose direct output streams.");
    }
}
