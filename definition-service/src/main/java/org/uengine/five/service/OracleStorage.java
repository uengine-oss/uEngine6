package org.uengine.five.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.uengine.five.entity.ProcDefEntity;
import org.uengine.five.entity.ProcDefVersionEntity;
import org.uengine.five.repository.ProcDefRepository;
import org.uengine.five.repository.ProcDefVersionRepository;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IContainer;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.JPAStorage;

public class OracleStorage extends JPAStorage {

    private static final String DEFINITIONS_ROOT = "definitions";
    private static final String ARCHIVE_ROOT = "archive";

    private final ProcDefRepository procDefRepository;
    private final ProcDefVersionRepository procDefVersionRepository;
    private final DefinitionActorNameProvider actorNameProvider;

    public OracleStorage(ProcDefRepository procDefRepository, ProcDefVersionRepository procDefVersionRepository,
            DefinitionActorNameProvider actorNameProvider) {
        this.procDefRepository = procDefRepository;
        this.procDefVersionRepository = procDefVersionRepository;
        this.actorNameProvider = actorNameProvider;
    }

    @Override
    public void delete(IResource resource) {
        String normalized = normalizePath(resource);
        if (isArchivePath(normalized)) {
            deleteArchive(stripArchivePrefix(normalized));
            return;
        }
        deleteCurrent(stripDefinitionsPrefix(normalized));
    }

    @Override
    public void rename(IResource resource, String newName) {
        copyInternal(normalizePath(resource), normalizePath(newName), true);
    }

    @Override
    public void copy(IResource src, String desPath) {
        copyInternal(normalizePath(src), normalizePath(desPath), false);
    }

    @Override
    public void move(IResource src, IContainer container) {
        String target = normalizePath(container.getPath()) + "/" + src.getName();
        copyInternal(normalizePath(src), target, true);
    }

    @Override
    public List<IResource> listFiles(IContainer containerResource) {
        String normalized = normalizePath(containerResource);
        if (normalized.isEmpty() || ".".equals(normalized) || DEFINITIONS_ROOT.equals(normalized)) {
            return listCurrentChildren("");
        }
        if (isArchivePath(normalized)) {
            return listArchiveChildren(stripArchivePrefix(normalized));
        }
        return listCurrentChildren(stripDefinitionsPrefix(normalized));
    }

    @Override
    public void createFolder(IContainer containerResource) {
        String dbPath = stripDefinitionsPrefix(normalizePath(containerResource));
        if (dbPath.isEmpty() || isArchivePath(normalizePath(containerResource))) {
            return;
        }

        boolean isNew = !procDefRepository.existsById(dbPath);
        ProcDefEntity entity = procDefRepository.findById(dbPath).orElseGet(ProcDefEntity::new);
        entity.setId(dbPath);
        entity.setPath(dbPath);
        entity.setName(fileNameOf(dbPath));
        entity.setDirectory(Boolean.TRUE);
        entity.setResourceType(IResource.TYPE_FOLDER);
        entity.setSnapshot(null);
        entity.setDefinitionJson(null);
        applyProcDefActorNames(entity, isNew, actorNameProvider.resolveDisplayName());
        procDefRepository.save(entity);
    }

    @Override
    public boolean exists(IResource resource) {
        String normalized = normalizePath(resource);
        if (normalized.isEmpty() || ".".equals(normalized) || DEFINITIONS_ROOT.equals(normalized)) {
            return true;
        }
        if (ARCHIVE_ROOT.equals(normalized)) {
            return !procDefVersionRepository.findAll().isEmpty();
        }
        if (isArchivePath(normalized)) {
            return archiveExists(stripArchivePrefix(normalized));
        }
        return currentExists(stripDefinitionsPrefix(normalized));
    }

    @Override
    public Object getObject(IResource resource) {
        String normalized = normalizePath(resource);
        if (isArchivePath(normalized)) {
            ArchivePath archivePath = ArchivePath.parse(stripArchivePrefix(normalized));
            if (archivePath.version == null) {
                return null;
            }
            return procDefVersionRepository.findByProcDefIdAndVersion(archivePath.procDefId, archivePath.version)
                    .map(ProcDefVersionEntity::getSnapshot)
                    .orElse(null);
        }

        String procDefId = stripDefinitionsPrefix(normalized);
        // 요청에 version 쿼리 파라미가 있으면 TB_BPM_PDEF_VER에서 해당 버전 스냅샷 반환
        String version = getRequestVersionQueryParam();
        if (version != null && !version.isBlank()) {
            Optional<String> snapshot = procDefVersionRepository.findByProcDefIdAndVersion(procDefId, version)
                    .map(ProcDefVersionEntity::getSnapshot);
            if (snapshot.isPresent()) {
                return snapshot.get();
            }
        }
        return procDefRepository.findById(procDefId)
                .map(ProcDefEntity::getSnapshot)
                .orElse(null);
    }

    /** 현재 HTTP 요청의 version 쿼리 파라미 반환. 웹 요청이 아니거나 없으면 null. */
    private static String getRequestVersionQueryParam() {
        try {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
                String v = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                        .getParameter("version");
                return (v != null && !v.isBlank()) ? v.trim() : null;
            }
        } catch (IllegalStateException ignored) {
            // 비웹/비요청 스레드
        }
        return null;
    }

    @Override
    public void save(IResource resource, Object object) throws Exception {
        String normalized = normalizePath(resource);
        String content = object instanceof String ? (String) object : org.uengine.modeling.resource.Serializer.serialize(object);

        if (isArchivePath(normalized)) {
            saveArchive(stripArchivePrefix(normalized), content);
            return;
        }

        saveCurrent(stripDefinitionsPrefix(normalized), content);
    }

    @Override
    public InputStream getInputStream(IResource resource) {
        Object object = getObject(resource);
        return inputStreamOf(object != null ? String.valueOf(object) : "");
    }

    @Override
    public boolean isContainer(IResource resource) {
        String normalized = normalizePath(resource);
        if (normalized.isEmpty() || ".".equals(normalized) || DEFINITIONS_ROOT.equals(normalized) || ARCHIVE_ROOT.equals(normalized)) {
            return true;
        }
        if (isArchivePath(normalized)) {
            ArchivePath archivePath = ArchivePath.parse(stripArchivePrefix(normalized));
            return archivePath.version == null;
        }

        String currentPath = stripDefinitionsPrefix(normalized);
        Optional<ProcDefEntity> existing = procDefRepository.findById(currentPath);
        if (existing.isPresent()) {
            return Boolean.TRUE.equals(existing.get().getDirectory());
        }

        String prefix = currentPath.endsWith("/") ? currentPath : currentPath + "/";
        return procDefRepository.findAll().stream()
                .map(ProcDefEntity::getPath)
                .filter(path -> path != null && !path.isBlank())
                .anyMatch(path -> path.startsWith(prefix));
    }

    private void saveCurrent(String dbPath, String content) {
        boolean isNew = !procDefRepository.existsById(dbPath);
        ProcDefEntity entity = procDefRepository.findById(dbPath).orElseGet(ProcDefEntity::new);
        entity.setId(dbPath);
        entity.setPath(dbPath);
        entity.setName(fileNameOf(dbPath));
        entity.setDirectory(Boolean.FALSE);
        entity.setResourceType(extensionOf(dbPath));
        entity.setSnapshot(content);
        entity.setDefinitionJson(null);
        applyProcDefActorNames(entity, isNew, actorNameProvider.resolveDisplayName());
        procDefRepository.save(entity);
    }

    private void saveArchive(String archiveRelativePath, String content) {
        ArchivePath archivePath = ArchivePath.parse(archiveRelativePath);
        if (archivePath.version == null) {
            return;
        }

        boolean isNew = !procDefVersionRepository.findByProcDefIdAndVersion(archivePath.procDefId, archivePath.version)
                .isPresent();
        ProcDefVersionEntity entity = procDefVersionRepository
                .findByProcDefIdAndVersion(archivePath.procDefId, archivePath.version)
                .orElseGet(ProcDefVersionEntity::new);
        entity.setArcvId(buildArchiveId(archivePath.procDefId, archivePath.version));
        entity.setProcDefId(archivePath.procDefId);
        entity.setVersion(archivePath.version);
        entity.setSnapshot(content);
        entity.setDefinitionJson(null);
        applyVersionActorNames(entity, isNew, actorNameProvider.resolveDisplayName());
        procDefVersionRepository.save(entity);
    }

    private void deleteCurrent(String currentPath) {
        String normalized = normalizePath(currentPath);
        List<ProcDefEntity> currentEntities = procDefRepository.findAll().stream()
                .filter(entity -> matchesPath(entity.getPath(), normalized))
                .collect(Collectors.toList());
        if (!currentEntities.isEmpty()) {
            procDefRepository.deleteAll(currentEntities);
        }

        List<ProcDefVersionEntity> versionEntities = procDefVersionRepository.findAll().stream()
                .filter(entity -> matchesPath(entity.getProcDefId(), normalized))
                .collect(Collectors.toList());
        if (!versionEntities.isEmpty()) {
            procDefVersionRepository.deleteAll(versionEntities);
        }
    }

    private void deleteArchive(String archiveRelativePath) {
        ArchivePath archivePath = ArchivePath.parse(archiveRelativePath);
        if (archivePath.version != null) {
            procDefVersionRepository.findByProcDefIdAndVersion(archivePath.procDefId, archivePath.version)
                    .ifPresent(procDefVersionRepository::delete);
            return;
        }

        List<ProcDefVersionEntity> versionEntities = procDefVersionRepository.findAll().stream()
                .filter(entity -> matchesPath(entity.getProcDefId(), archivePath.procDefId))
                .collect(Collectors.toList());
        if (!versionEntities.isEmpty()) {
            procDefVersionRepository.deleteAll(versionEntities);
        }
    }

    private boolean currentExists(String currentPath) {
        String normalized = normalizePath(currentPath);
        if (procDefRepository.existsById(normalized)) {
            return true;
        }

        String prefix = normalized.endsWith("/") ? normalized : normalized + "/";
        return procDefRepository.findAll().stream()
                .map(ProcDefEntity::getPath)
                .filter(path -> path != null && !path.isBlank())
                .anyMatch(path -> path.startsWith(prefix));
    }

    private boolean archiveExists(String archiveRelativePath) {
        ArchivePath archivePath = ArchivePath.parse(archiveRelativePath);
        if (archivePath.version != null) {
            return procDefVersionRepository.findByProcDefIdAndVersion(archivePath.procDefId, archivePath.version).isPresent();
        }

        String prefix = archivePath.procDefId.endsWith("/") ? archivePath.procDefId : archivePath.procDefId + "/";
        return procDefVersionRepository.findAll().stream()
                .map(ProcDefVersionEntity::getProcDefId)
                .filter(path -> path != null && !path.isBlank())
                .anyMatch(path -> path.equals(archivePath.procDefId) || path.startsWith(prefix));
    }

    private List<IResource> listCurrentChildren(String basePath) {
        String normalizedBasePath = normalizePath(basePath);
        String prefix = normalizedBasePath.isEmpty() ? "" : normalizedBasePath + "/";
        List<ProcDefEntity> all = procDefRepository.findAll();
        Map<String, IResource> children = new LinkedHashMap<>();

        for (ProcDefEntity entity : all) {
            if (entity == null || entity.getPath() == null) {
                continue;
            }
            String path = normalizePath(entity.getPath());
            if (!prefix.isEmpty() && !path.startsWith(prefix)) {
                continue;
            }
            if (prefix.isEmpty() && path.isEmpty()) {
                continue;
            }

            String remainder = prefix.isEmpty() ? path : path.substring(prefix.length());
            if (remainder.isEmpty()) {
                continue;
            }

            int slash = remainder.indexOf('/');
            if (slash >= 0) {
                String childName = remainder.substring(0, slash);
                String childPath = normalizedBasePath.isEmpty() ? childName : normalizedBasePath + "/" + childName;
                children.putIfAbsent(childPath, resourceOf(DEFINITIONS_ROOT + "/" + childPath, true));
            } else {
                children.put(path, resourceOf(DEFINITIONS_ROOT + "/" + path, Boolean.TRUE.equals(entity.getDirectory())));
            }
        }

        return new ArrayList<>(children.values());
    }

    private List<IResource> listArchiveChildren(String basePath) {
        String normalizedBasePath = normalizePath(basePath);
        if (normalizedBasePath.isEmpty()) {
            return new ArrayList<>();
        }

        List<IResource> resources = new ArrayList<>();
        // 조회 경로(예: 부산은행/credit_review)와 구 저장 형식(부산은행/credit_review.bpmn) 모두 조회
        List<ProcDefVersionEntity> entities = procDefVersionRepository.findByProcDefId(normalizedBasePath);
        if (entities.isEmpty() && !normalizedBasePath.endsWith(".bpmn")) {
            entities = procDefVersionRepository.findByProcDefId(normalizedBasePath + ".bpmn");
        }
        for (ProcDefVersionEntity entity : entities) {
            String versionPath = ARCHIVE_ROOT + "/" + entity.getProcDefId() + "/" + entity.getVersion() + ".bpmn";
            resources.add(new DefaultResource(versionPath));
        }
        return resources;
    }

    private void copyInternal(String sourcePath, String targetPath, boolean deleteSource) {
        String normalizedSource = normalizePath(sourcePath);
        String normalizedTarget = normalizePath(targetPath);
        if (isArchivePath(normalizedSource) || isArchivePath(normalizedTarget)) {
            copyArchiveInternal(stripArchivePrefix(normalizedSource), stripArchivePrefix(normalizedTarget), deleteSource);
            return;
        }
        copyCurrentInternal(stripDefinitionsPrefix(normalizedSource), stripDefinitionsPrefix(normalizedTarget), deleteSource);
    }

    private void copyCurrentInternal(String sourcePath, String targetPath, boolean deleteSource) {
        List<ProcDefEntity> sourceEntities = procDefRepository.findAll().stream()
                .filter(entity -> matchesPath(entity.getPath(), sourcePath))
                .collect(Collectors.toList());
        List<ProcDefVersionEntity> sourceVersions = procDefVersionRepository.findAll().stream()
                .filter(entity -> matchesPath(entity.getProcDefId(), sourcePath))
                .collect(Collectors.toList());

        deleteCurrent(targetPath);

        for (ProcDefEntity sourceEntity : sourceEntities) {
            ProcDefEntity targetEntity = new ProcDefEntity();
            String replacedPath = replacePath(sourceEntity.getPath(), sourcePath, targetPath);
            targetEntity.setId(replacedPath);
            targetEntity.setPath(replacedPath);
            targetEntity.setName(fileNameOf(replacedPath));
            targetEntity.setDirectory(sourceEntity.getDirectory());
            targetEntity.setResourceType(sourceEntity.getResourceType());
            targetEntity.setSnapshot(sourceEntity.getSnapshot());
            targetEntity.setDefinitionJson(sourceEntity.getDefinitionJson());
            targetEntity.setCreatedAt(sourceEntity.getCreatedAt());
            targetEntity.setUpdatedAt(sourceEntity.getUpdatedAt());
            targetEntity.setCreatedByName(sourceEntity.getCreatedByName());
            targetEntity.setUpdatedByName(sourceEntity.getUpdatedByName());
            procDefRepository.save(targetEntity);
        }

        for (ProcDefVersionEntity sourceVersion : sourceVersions) {
            ProcDefVersionEntity targetVersion = new ProcDefVersionEntity();
            String replacedProcDefId = replacePath(sourceVersion.getProcDefId(), sourcePath, targetPath);
            targetVersion.setProcDefId(replacedProcDefId);
            targetVersion.setVersion(sourceVersion.getVersion());
            targetVersion.setArcvId(buildArchiveId(replacedProcDefId, sourceVersion.getVersion()));
            targetVersion.setVersionTag(sourceVersion.getVersionTag());
            targetVersion.setTimeStamp(sourceVersion.getTimeStamp());
            targetVersion.setSnapshot(sourceVersion.getSnapshot());
            targetVersion.setDefinitionJson(sourceVersion.getDefinitionJson());
            targetVersion.setDiff(sourceVersion.getDiff());
            targetVersion.setMessage(sourceVersion.getMessage());
            targetVersion.setCreatedByName(sourceVersion.getCreatedByName());
            targetVersion.setUpdatedByName(sourceVersion.getUpdatedByName());
            procDefVersionRepository.save(targetVersion);
        }

        if (deleteSource) {
            deleteCurrent(sourcePath);
        }
    }

    private void copyArchiveInternal(String sourcePath, String targetPath, boolean deleteSource) {
        ArchivePath source = ArchivePath.parse(sourcePath);
        ArchivePath target = ArchivePath.parse(targetPath);

        if (source.version != null && target.version != null) {
            procDefVersionRepository.findByProcDefIdAndVersion(source.procDefId, source.version).ifPresent(entity -> {
                procDefVersionRepository.findByProcDefIdAndVersion(target.procDefId, target.version)
                        .ifPresent(procDefVersionRepository::delete);
                ProcDefVersionEntity copy = new ProcDefVersionEntity();
                copy.setProcDefId(target.procDefId);
                copy.setVersion(target.version);
                copy.setArcvId(buildArchiveId(target.procDefId, target.version));
                copy.setVersionTag(entity.getVersionTag());
                copy.setTimeStamp(entity.getTimeStamp());
                copy.setSnapshot(entity.getSnapshot());
                copy.setDefinitionJson(entity.getDefinitionJson());
                copy.setDiff(entity.getDiff());
                copy.setMessage(entity.getMessage());
                copy.setCreatedByName(entity.getCreatedByName());
                copy.setUpdatedByName(entity.getUpdatedByName());
                procDefVersionRepository.save(copy);
                if (deleteSource) {
                    procDefVersionRepository.delete(entity);
                }
            });
            return;
        }

        List<ProcDefVersionEntity> sourceVersions = procDefVersionRepository.findAll().stream()
                .filter(entity -> matchesPath(entity.getProcDefId(), source.procDefId))
                .collect(Collectors.toList());
        deleteArchive(target.procDefId);

        for (ProcDefVersionEntity sourceVersion : sourceVersions) {
            ProcDefVersionEntity copy = new ProcDefVersionEntity();
            String replacedProcDefId = replacePath(sourceVersion.getProcDefId(), source.procDefId, target.procDefId);
            copy.setProcDefId(replacedProcDefId);
            copy.setVersion(sourceVersion.getVersion());
            copy.setArcvId(buildArchiveId(replacedProcDefId, sourceVersion.getVersion()));
            copy.setVersionTag(sourceVersion.getVersionTag());
            copy.setTimeStamp(sourceVersion.getTimeStamp());
            copy.setSnapshot(sourceVersion.getSnapshot());
            copy.setDefinitionJson(sourceVersion.getDefinitionJson());
            copy.setDiff(sourceVersion.getDiff());
            copy.setMessage(sourceVersion.getMessage());
            copy.setCreatedByName(sourceVersion.getCreatedByName());
            copy.setUpdatedByName(sourceVersion.getUpdatedByName());
            procDefVersionRepository.save(copy);
        }

        if (deleteSource) {
            deleteArchive(source.procDefId);
        }
    }

    private static void applyProcDefActorNames(ProcDefEntity entity, boolean isNew, String actor) {
        String a = (actor == null || actor.isBlank()) ? DefinitionActorNameProvider.DEFAULT_ACTOR : actor;
        if (isNew) {
            entity.setCreatedByName(a);
            entity.setUpdatedByName(a);
        } else {
            if (entity.getCreatedByName() == null || entity.getCreatedByName().isBlank()) {
                entity.setCreatedByName(a);
            }
            entity.setUpdatedByName(a);
        }
    }

    private static void applyVersionActorNames(ProcDefVersionEntity entity, boolean isNew, String actor) {
        String a = (actor == null || actor.isBlank()) ? DefinitionActorNameProvider.DEFAULT_ACTOR : actor;
        if (isNew) {
            entity.setCreatedByName(a);
            entity.setUpdatedByName(a);
        } else {
            if (entity.getCreatedByName() == null || entity.getCreatedByName().isBlank()) {
                entity.setCreatedByName(a);
            }
            entity.setUpdatedByName(a);
        }
    }

    private boolean isArchivePath(String normalizedPath) {
        return normalizedPath.equals(ARCHIVE_ROOT) || normalizedPath.startsWith(ARCHIVE_ROOT + "/");
    }

    private String stripDefinitionsPrefix(String normalizedPath) {
        if (normalizedPath.equals(DEFINITIONS_ROOT)) {
            return "";
        }
        if (normalizedPath.startsWith(DEFINITIONS_ROOT + "/")) {
            return normalizedPath.substring((DEFINITIONS_ROOT + "/").length());
        }
        return normalizedPath;
    }

    private String stripArchivePrefix(String normalizedPath) {
        if (normalizedPath.equals(ARCHIVE_ROOT)) {
            return "";
        }
        if (normalizedPath.startsWith(ARCHIVE_ROOT + "/")) {
            return normalizedPath.substring((ARCHIVE_ROOT + "/").length());
        }
        return normalizedPath;
    }

    private boolean matchesPath(String candidate, String sourcePath) {
        String normalizedCandidate = normalizePath(candidate);
        String normalizedSource = normalizePath(sourcePath);
        if (normalizedCandidate.equals(normalizedSource)) {
            return true;
        }
        return normalizedCandidate.startsWith(normalizedSource + "/");
    }

    private String replacePath(String original, String sourcePath, String targetPath) {
        String normalizedOriginal = normalizePath(original);
        String normalizedSource = normalizePath(sourcePath);
        String normalizedTarget = normalizePath(targetPath);
        if (normalizedOriginal.equals(normalizedSource)) {
            return normalizedTarget;
        }
        return normalizedTarget + normalizedOriginal.substring(normalizedSource.length());
    }

    private String buildArchiveId(String procDefId, String version) {
        return procDefId + "_" + version;
    }

    private static class ArchivePath {
        private final String procDefId;
        private final String version;

        private ArchivePath(String procDefId, String version) {
            this.procDefId = procDefId;
            this.version = version;
        }

        private static ArchivePath parse(String path) {
            String normalized = path == null ? "" : path.trim().replace("\\", "/");
            int idx = normalized.lastIndexOf('/');
            if (idx < 0) {
                return new ArchivePath(normalized, null);
            }

            String lastSegment = normalized.substring(idx + 1);
            if (!lastSegment.contains(".")) {
                return new ArchivePath(normalized, null);
            }

            int extIdx = lastSegment.lastIndexOf('.');
            String version = extIdx >= 0 ? lastSegment.substring(0, extIdx) : lastSegment;
            String procDefId = normalized.substring(0, idx);
            return new ArchivePath(procDefId, version);
        }
    }
}
