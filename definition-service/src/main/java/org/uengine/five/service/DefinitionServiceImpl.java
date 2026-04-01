package org.uengine.five.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import org.apache.commons.io.IOUtils;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
// import org.springframework.hateoas.ResourceSupport;
// import org.springframework.hateoas.Resources;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.server.ResponseStatusException;
// import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.five.repository.ProcDefRepository;
import org.uengine.five.repository.ProcDefVersionRepository;
import org.uengine.kernel.GlobalContext;
import org.uengine.modeling.resource.ContainerResource;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IContainer;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.modeling.resource.Version;
import org.uengine.modeling.resource.VersionManager;
//import org.uengine.processpublisher.BPMNUtil;
//import org.uengine.uml.model.ClassDefinition;
import org.uengine.util.UEngineUtil;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by uengine on 2017. 8. 9..
 *
 * Implementation Principles: - REST Maturity Level : 3 (Hateoas)
 * - Not using old uEngine ProcessManagerBean, this replaces the
 * ProcessManagerBean
 * - ResourceManager and CachedResourceManager will be used for definition
 * caching (Not to use the old DefinitionFactory)
 * - json must be Typed JSON to enable object polymorphism
 * - need to change the jackson engine.
 * TODO: accept? typed json is sometimes hard to read
 */
@RestController
public class DefinitionServiceImpl implements DefinitionService, DefinitionXMLService {

    static protected final String RESOURCE_ROOT = "definitions";
    static protected final String ARCHIVE_ROOT = "archive";

    @Autowired
    ResourceManager resourceManager;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    Environment environment;

    @Autowired
    InstanceService instanceService;

    @Autowired
    ProcDefDataService procDefDataService;

    @Autowired
    DefinitionLockService definitionLockService;

    @Autowired
    ProcDefRepository procDefRepository;

    @Autowired
    ProcDefVersionRepository procDefVersionRepository;

    @Autowired
    DefinitionActorNameProvider definitionActorNameProvider;

    @Value("${uengine.definition.expose-actor-names:false}")
    private boolean exposeActorNames;
    // static BpmnXMLParser bpmnXMLParser = new BpmnXMLParser();

    static ObjectMapper objectMapper = createTypedJsonObjectMapper();

    @PostConstruct
    public void init() {
    }

    /** Oracle 프로필일 때는 process-service /definition-changes 호출 생략 */
    private boolean shouldNotifyDefinitionChanges() {
        if (environment == null)
            return true;
        for (String profile : environment.getActiveProfiles()) {
            if ("oracle".equalsIgnoreCase(profile))
                return false;
        }
        return true;
    }

    @RequestMapping(value = DEFINITION, method = RequestMethod.GET, produces = "application/hal+json;charset=UTF-8")
    @Override
    public CollectionModel<DefinitionResource> listDefinition(String basePath) throws Exception {
        return _listDefinition(RESOURCE_ROOT, basePath);
    }

    /**
     * Non-HAL JSON listing for lightweight clients.
     * Used by process-service to enumerate business rule files.
     */
    @Override
    @RequestMapping(value = DEFINITION, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String listDefinitionRaw(@RequestParam(value = "basePath", required = false) String basePath)
            throws Exception {
        CollectionModel<DefinitionResource> model = listDefinition(basePath);
        return new ObjectMapper().writeValueAsString(model);
    }

    @RequestMapping(value = "/versions/**", method = RequestMethod.GET)
    public CollectionModel<DefinitionResource> listDefinitionVersions(HttpServletRequest request) throws Exception {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String basePath = "/versions/";
        String defId = fullPath.substring(basePath.length());

        return _listDefinitionVersions("archive/", defId);
    }

    private CollectionModel<DefinitionResource> _listDefinitionVersions(String resourceRoot, String basePath)
            throws Exception {

        if (basePath == null) {
            basePath = "";
        }

        IContainer resource = new ContainerResource();
        resource.setPath(resourceRoot + basePath);
        List<IResource> resources = resourceManager.listFiles(resource);

        List<DefinitionResource> definitions = new ArrayList<DefinitionResource>();
        for (IResource resource1 : resources) {
            DefinitionResource definition = new DefinitionResource(resource1);
            definition.setVersion(definition.name.replace(".bpmn", ""));
            applyDefinitionResourceId(definition);
            enrichActorNamesIfEnabled(definition, true);
            definitions.add(definition);
        }
        definitions.sort(Comparator.comparing(def -> {
            String version = def.getVersion();
            try {
                return Float.parseFloat(version);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }));

        return CollectionModel.of(definitions);
    }

    private CollectionModel<DefinitionResource> _listDefinition(String resourceRoot, String basePath) throws Exception {

        if (basePath == null) {
            basePath = "";
        }

        IContainer resource = new ContainerResource();
        resource.setPath(resourceRoot + "/" + basePath);
        List<IResource> resources = resourceManager.listFiles(resource);

        List<DefinitionResource> definitions = new ArrayList<DefinitionResource>();
        for (IResource resource1 : resources) {
            if (shouldHideFromDefinitionTree(resource1)) {
                continue;
            }
            DefinitionResource definition = new DefinitionResource(resource1);
            definitions.add(definition);
            definition.path = definition.path.replace("definitions/", "");
            applyDefinitionResourceId(definition);
            enrichActorNamesIfEnabled(definition, false);
        }

        return CollectionModel.of(definitions);
    }

    private boolean shouldHideFromDefinitionTree(IResource resource) {
        return !(resource instanceof IContainer)
                && resource.getName() != null
                && resource.getName().toLowerCase().endsWith(".json");
    }

    private void enrichActorNamesIfEnabled(DefinitionResource def, boolean archiveVersionEntry) {
        if (!exposeActorNames || environment == null || !environment.acceptsProfiles(Profiles.of("oracle"))) {
            return;
        }
        try {
            if (archiveVersionEntry) {
                String p = def.getPath();
                if (p == null || !p.startsWith(ARCHIVE_ROOT + "/")) {
                    return;
                }
                String rel = p.substring((ARCHIVE_ROOT + "/").length());
                int idx = rel.lastIndexOf('/');
                if (idx < 0) {
                    return;
                }
                String last = rel.substring(idx + 1);
                if (!last.contains(".")) {
                    return;
                }
                int extIdx = last.lastIndexOf('.');
                String ver = last.substring(0, extIdx);
                String procDefId = rel.substring(0, idx);
                procDefVersionRepository.findByProcDefIdAndVersion(procDefId, ver).ifPresent(e -> {
                    def.setCreatedByName(e.getCreatedByName());
                    def.setUpdatedByName(e.getUpdatedByName());
                });
            } else {
                String procDefId = toProcDefIdForActorLookup(def.getPath());
                if (procDefId == null || procDefId.isEmpty()) {
                    return;
                }
                procDefRepository.findById(procDefId).ifPresent(e -> {
                    def.setCreatedByName(e.getCreatedByName());
                    def.setUpdatedByName(e.getUpdatedByName());
                    if (e.getName() != null && !e.getName().isBlank()) {
                        def.setName(e.getName());
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    private static String toProcDefIdForActorLookup(String path) {
        if (path == null) {
            return null;
        }
        String prefix = RESOURCE_ROOT + "/";
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return path;
    }

    /**
     * HAL/JSON에 id를 항상보낸다. 현재본은 definitions/ 접두 제거 경로, 버전 행은 arcv_id(procDefId_version) 형식.
     */
    private static void applyDefinitionResourceId(DefinitionResource def) {
        if (def == null) {
            return;
        }
        String p = def.getPath();
        if (p == null) {
            return;
        }
        if (p.startsWith(ARCHIVE_ROOT + "/")) {
            String rel = p.substring((ARCHIVE_ROOT + "/").length());
            int idx = rel.lastIndexOf('/');
            if (idx < 0) {
                return;
            }
            String last = rel.substring(idx + 1);
            if (!last.contains(".")) {
                return;
            }
            int extIdx = last.lastIndexOf('.');
            String ver = last.substring(0, extIdx);
            String procDefId = rel.substring(0, idx);
            def.setId(procDefId + "_" + ver);
        } else {
            def.setId(toProcDefIdForActorLookup(p));
        }
    }

    /**
     * PUT body에 name이 있으면(비어 있지 않으면) Oracle TB_BPM_PROCDEF.name 을 갱신한다. 경로 id는 정의 상대 경로 기준.
     */
    private void applyOptionalProcDefDisplayName(DefinitionRequest req, String definitionPath) {
        if (req == null) {
            return;
        }
        String proposed = req.getName();
        if (proposed == null || proposed.isBlank()) {
            return;
        }
        if (environment == null || !environment.acceptsProfiles(Profiles.of("oracle"))) {
            return;
        }
        String id = procDefIdUnderDefinitions(definitionPath);
        if (id.isEmpty()) {
            return;
        }
        String trimmed = proposed.trim();
        if (trimmed.length() > 255) {
            trimmed = trimmed.substring(0, 255);
        }
        final String finalName = trimmed;
        procDefRepository.findById(id).ifPresent(e -> {
            e.setName(finalName);
            procDefRepository.save(e);
        });
    }

    private String procDefIdUnderDefinitions(String definitionPath) {
        String full = toDefinitionResourcePath(definitionPath);
        String prefix = RESOURCE_ROOT + "/";
        if (full.startsWith(prefix)) {
            return full.substring(prefix.length());
        }
        return full;
    }

    @RequestMapping(value = "/version/{version}" + DEFINITION + "/", method = RequestMethod.GET)
    public CollectionModel<DefinitionResource> listVersionDefinitions(@PathVariable("version") String version,
            String basePath) throws Exception {
        VersionManager versionManager = GlobalContext.getComponent(VersionManager.class);

        return _listDefinition(versionManager.versionDirectoryOf(new Version(version)), basePath);
    }

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public CollectionModel<VersionResource> listVersions() throws Exception {
        VersionManager versionManager = GlobalContext.getComponent(VersionManager.class);

        List<VersionResource> versionResources = new ArrayList<VersionResource>();
        for (Version version : versionManager.listVersions()) {
            VersionResource versionResource = new VersionResource(version);
            versionResources.add(versionResource);
        }

        return CollectionModel.of(versionResources);
    }

    @RequestMapping(value = "/version", method = RequestMethod.POST)
    public CollectionModel<VersionResource> versionUp(Version version, @QueryParam("major") boolean major,
            @QueryParam("makeProduction") boolean makeProduction) throws Exception {

        VersionManager versionManager = GlobalContext.getComponent(VersionManager.class);
        versionManager.load("codi", null);

        if (major)
            versionManager.majorVersionUp();
        else
            versionManager.minorVersionUp();

        return listVersions();

    }

    @RequestMapping(value = "/version/{version:.+}/production", method = RequestMethod.POST)
    public VersionResource makeProduction(@PathVariable("version") String version) throws Exception {

        VersionManager versionManager = GlobalContext.getComponent(VersionManager.class);
        versionManager.load("codi", null);

        Version versionObj = new Version(version);
        versionManager.makeProductionVersion(versionObj);

        // VersionResource versionResource = new VersionResource(versionObj);

        return getVersion(version);
    }

    @RequestMapping(value = "/version/production", method = RequestMethod.GET)
    public VersionResource getProduction() throws Exception {

        VersionManager versionManager = GlobalContext.getComponent(VersionManager.class);
        versionManager.load("codi", null);

        Version versionObj = versionManager.getProductionVersion();

        return new VersionResource(versionObj);
    }

    @RequestMapping(value = "/version/{version:.+}", method = RequestMethod.GET)
    public VersionResource getVersion(@PathVariable("version") String version) throws Exception {

        VersionManager versionManager = GlobalContext.getComponent(VersionManager.class);
        List<Version> versions = versionManager.listVersions();

        for (Version theVersion : versions) {
            if (theVersion.equals(new Version(version))) {
                VersionResource versionResource = new VersionResource(theVersion);

                return versionResource;
            }
        }

        throw new ResourceNotFoundException(); // make 404 error
    }

    @RequestMapping(value = DEFINITION
            + "/{defPath:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @Override
    @SuppressWarnings("rawtypes")
    public RepresentationModel getDefinition(@PathVariable("defPath") String definitionPath) throws Exception {
        // case of directory:
        IResource resource = new DefaultResource(toDefinitionResourcePath(definitionPath));
        if (resourceManager.exists(resource) && resourceManager.isContainer(resource)) { // is a folder
            return listDefinition(definitionPath);
        }

        // case of file:
        // definitionPath = UEngineUtil.getNamedExtFile(definitionPath, "xml");

        resource = new DefaultResource(toDefinitionResourcePath(definitionPath));

        if (!resourceManager.exists(resource)) {
            throw new ResourceNotFoundException(); // make 404 error
        }

        DefinitionResource halDefinition = new DefinitionResource(resource);
        applyDefinitionResourceId(halDefinition);
        enrichActorNamesIfEnabled(halDefinition, false);

        return halDefinition;

    }

    @RequestMapping(value = DEFINITION + "/**", method = RequestMethod.GET)
    public Object getDefinition(HttpServletRequest request) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring(DEFINITION.length() + 1);

        return getDefinition(definitionPath);

    }

    /**
     * TODO: need ACL referenced by token
     * 
     * @throws Exception
     */
    @RequestMapping(value = DEFINITION + "/**", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public DefinitionResource renameOrMove(@RequestBody DefinitionResource definition_, HttpServletRequest request)
            throws Exception {

        DefinitionResource definition = definition_;

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        String definitionPath = path.substring(DEFINITION.length());
        if (definitionPath.indexOf(".") != -1) {
            definitionPath = UEngineUtil.getNamedExtFile(definitionPath, "xml");
        }
        IResource resource = new DefaultResource(RESOURCE_ROOT + "/" + definitionPath);

        if (!definition.getPath().equals(definitionPath)) {
            String newPath = RESOURCE_ROOT + "/" + definition.getPath();
            resourceManager.rename(resource, newPath);
            return new DefinitionResource(new ContainerResource(newPath));
        }

        return new DefinitionResource(resource);
    }

    @RequestMapping(value = DEFINITION + "/**", method = { RequestMethod.POST })
    public DefinitionResource createFolder(@RequestBody DefinitionResource newResource_, HttpServletRequest request)
            throws Exception {

        DefinitionResource newResource = newResource_;

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        String definitionPath = path.substring(DEFINITION.length());

        if (newResource == null) {
            if (definitionPath.indexOf(".") == -1) { // it is a package (directory)
                IContainer container = new ContainerResource();
                container.setPath(toDefinitionResourcePath(definitionPath));
                resourceManager.createFolder(container);
                return new DefinitionResource(container);
            } else {
                throw new Exception(
                        "Only folder can be created with this method. Use POST : " + DEFINITION_RAW + " instead.");
            }
        } else {
            String example = "e.g.{\"name\": \"folder\", \"directory\":true}";

            Assert.notNull(newResource.getName(), "folder name must be present. " + example);
            Assert.isTrue(newResource.isDirectory(), "On directory can be created with this method. " + example);

            IContainer container = new ContainerResource();
            container.setPath(toDefinitionResourcePath(definitionPath + "/" + newResource.getName()));
            resourceManager.createFolder(container);

            return new DefinitionResource(container);
        }

    }

    @RequestMapping(value = DEFINITION + "/**", method = { RequestMethod.DELETE })
    public void deleteDefinition(HttpServletRequest request) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring(DEFINITION.length());
        if (path.indexOf(".") == -1) {
            definitionPath = UEngineUtil.getNamedExtFile(definitionPath, "bpmn");
        }
        IResource resource = new DefaultResource(toDefinitionResourcePath(definitionPath));
        resourceManager.delete(resource);

    }

    // ----------------- raw definition services -------------------- //

    @SuppressWarnings("deprecation")
    public static ObjectMapper createTypedJsonObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ignore null
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT); // ignore zero and false when it is int
                                                                                 // or boolean
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "_type");
        return objectMapper;

    }

    /**
     * TODO: need ACL referenced by token
     * 
     * @param definition
     * @throws Exception
     */
    @Override
    @RequestMapping(value = DEFINITION_RAW
            + "/{defPath:.+}", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public DefinitionResource putRawDefinition(@PathVariable("defPath") String definitionPath,
            @RequestBody DefinitionRequest definitionRequest) throws Exception {

        try {
            definitionActorNameProvider.beginRawSave(
                    definitionRequest != null ? definitionRequest.getUpdatedByName() : null);

            String dp = definitionPath;
            if (!dp.startsWith("/")) {
                dp = "/" + dp;
            }

            // directory
            if (dp.indexOf(".") == -1) {
                IContainer container = new ContainerResource();
                container.setPath(toDefinitionResourcePath(dp));
                resourceManager.createFolder(container);
                applyOptionalProcDefDisplayName(definitionRequest, dp);
                return new DefinitionResource(container);
            }

            String fileExt = UEngineUtil.getFileExt(dp);

            // archive only for bpmn versions
            if (definitionRequest != null && definitionRequest.getVersion() != null && "bpmn".equalsIgnoreCase(fileExt)) {
                DefaultResource versionResource = new DefaultResource(
                        toArchiveResourcePath(dp, definitionRequest.getVersion()));
                resourceManager.save(versionResource, definitionRequest.getDefinition());
            }

            DefaultResource resource = new DefaultResource(toDefinitionResourcePath(dp));
            resourceManager.save(resource, definitionRequest.getDefinition());
            applyOptionalProcDefDisplayName(definitionRequest, dp);
            if ("bpmn".equalsIgnoreCase(fileExt) && shouldNotifyDefinitionChanges()) {
                instanceService.postCreatedRawDefinition(dp);
            }

            return new DefinitionResource(resource);
        } finally {
            definitionActorNameProvider.endRawSave();
        }
    }

    @RequestMapping(value = DEFINITION_RAW + "/**", method = { RequestMethod.POST, RequestMethod.PUT })
    public DefinitionResource putRawDefinition(@RequestBody DefinitionRequest definitionRequest,
            HttpServletRequest request)
            throws Exception {

        try {
            definitionActorNameProvider.beginRawSave(
                    definitionRequest != null ? definitionRequest.getUpdatedByName() : null);

            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

            String definitionPath = path.substring(DEFINITION_RAW.length());

            String fileName = definitionPath.contains("/")
                    ? definitionPath.substring(definitionPath.lastIndexOf("/") + 1)
                    : definitionPath;
            if (!fileName.contains(".")) {
                definitionPath = definitionPath + ".bpmn";
            }

            String fileExt = UEngineUtil.getFileExt(definitionPath);

            // 버전 아카이브는 BPMN에 대해서만 저장
            if (definitionRequest.getVersion() != null && "bpmn".equalsIgnoreCase(fileExt)) {
                DefaultResource versionResource = new DefaultResource(
                        toArchiveResourcePath(definitionPath, definitionRequest.getVersion()));
                resourceManager.save(versionResource, definitionRequest.getDefinition());
            }

            DefaultResource resource = new DefaultResource(toDefinitionResourcePath(definitionPath));
            resourceManager.save(resource, definitionRequest.getDefinition());

            if (shouldNotifyDefinitionChanges() && "bpmn".equalsIgnoreCase(fileExt)) {
                try {
                    instanceService.postCreatedRawDefinition(definitionPath);
                } catch (FeignException fe) {
                    // process-service가 내려준 JSON 바디가 커질 수 있어(message 위주로) 요약해서 리턴
                    HttpStatus status = HttpStatus.resolve(fe.status());
                    if (status == null)
                        status = HttpStatus.BAD_GATEWAY;

                    String body = fe.contentUTF8();
                    String summarized = summarizeFeignBody(body);
                    throw new ResponseStatusException(status,
                            "[process-service:/definition-changes 실패] definitionPath=" + definitionPath + "\n"
                                    + summarized,
                            fe);
                }
            }

            if (definitionPath.indexOf(".") == -1) { // it is a package (directory)
                IContainer container = new ContainerResource();
                container.setPath(RESOURCE_ROOT + "/" + definitionPath);
                resourceManager.createFolder(container);
                applyOptionalProcDefDisplayName(definitionRequest, definitionPath);
                return new DefinitionResource(container);
            }

            applyOptionalProcDefDisplayName(definitionRequest, definitionPath);
            return new DefinitionResource(resource);
        } finally {
            definitionActorNameProvider.endRawSave();
        }
    }

    private String summarizeFeignBody(String body) {
        if (body == null || body.isBlank())
            return "(empty body)";
        // Spring Boot 기본 에러 JSON이면 message(또는 trace)만 뽑아낸다.
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<?, ?> map = om.readValue(body, java.util.Map.class);
            Object msg = map.get("message");
            Object trace = map.get("trace");

            // 우선순위:
            // 1) message 안에 "Stacktrace:"가 있으면 그 뒤만 리턴
            // 2) trace 필드가 있으면 trace만 리턴
            // 3) message만 리턴
            if (msg != null) {
                String m = String.valueOf(msg);
                int idx = m.indexOf("Stacktrace:");
                if (idx >= 0) {
                    String only = m.substring(idx + "Stacktrace:".length());
                    // leading newline 제거
                    if (only.startsWith("\n"))
                        only = only.substring(1);
                    return only;
                }
            }
            if (trace != null) {
                return String.valueOf(trace);
            }
            if (msg != null) {
                return String.valueOf(msg);
            }
        } catch (Exception ignore) {
        }
        // JSON이 아니면 길이 제한만 적용
        int limit = 4000;
        if (body.length() > limit) {
            return body.substring(0, limit) + "\n...(truncated " + (body.length() - limit) + " chars)";
        }
        return body;
    }

    @RequestMapping(value = DEFINITION_RAW
            + "/{defPath:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawDefinition(@PathVariable("defPath") String definitionPath/*
                                                                                  * , @RequestParam(value = "unwrap",
                                                                                  * required = false) boolean unwrap
                                                                                  */) throws Exception {
        String version = null;
        if (definitionPath.contains("/version/")) {
            String[] parts = definitionPath.split("/version/");
            definitionPath = parts[0];
            version = parts[1];
        }
        if (definitionPath.indexOf(".") == -1) {
            definitionPath = UEngineUtil.getNamedExtFile(RESOURCE_ROOT + "/" + definitionPath, "xml");
        }
        if (!(definitionPath.startsWith(RESOURCE_ROOT))) {
            definitionPath = RESOURCE_ROOT + "/" + definitionPath;
        }
        Object definition = getDefinitionLocal(definitionPath, version);

        // if(unwrap) {
        // return objectMapper.writeValueAsString(definition);
        // }else{
        // DefinitionWrapper definitionWrapper = new DefinitionWrapper(definition);
        // String uEngineProcessJSON =
        // objectMapper.writeValueAsString(definitionWrapper);
        return definition;
        // }

    }

    /**
     * Feign-friendly raw definition getter (full path via query param).
     */
    @RequestMapping(value = DEFINITION_RAW, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawDefinitionByParam(@RequestParam("defPath") String definitionPath) throws Exception {
        return getRawDefinition(definitionPath);
    }

    @RequestMapping(value = DEFINITION_RAW
            + "/**", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawDefinition(HttpServletRequest request/* , @RequestParam("unwrap") boolean unwrap */)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring(DEFINITION_RAW.length() + 1);

        return getRawDefinition(definitionPath);

    }

    /**
     * Feign-friendly raw definition saver (full path via query param).
     */
    @RequestMapping(value = DEFINITION_RAW, method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public DefinitionResource putRawDefinitionByParam(@RequestParam("defPath") String definitionPath,
            @RequestBody DefinitionRequest definitionRequest) throws Exception {
        return putRawDefinition(definitionPath, definitionRequest);
    }

    @RequestMapping(value = DEFINITION_SYSTEM + "/**", method = { RequestMethod.POST, RequestMethod.PUT })
    public DefinitionResource putRawSystem(@RequestBody String definition, HttpServletRequest request)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String name = path.substring(DEFINITION_SYSTEM.length());
        // if (definitionPath.indexOf(".") == -1) { // it is a package (directory)
        // IContainer container = new ContainerResource();
        // container.setPath(RESOURCE_ROOT + "/" + definitionPath);
        // resourceManager.createFolder(container);
        // return new DefinitionResource(container);
        // }
        String definitionPath = RESOURCE_ROOT + "/system" + name + ".json";
        String fileExt = UEngineUtil.getFileExt(definitionPath);

        // 무조건 xml 파일로 결국 저장됨.
        DefaultResource resource = new DefaultResource(definitionPath);

        if (fileExt.endsWith("json")) {
            resourceManager.save(resource, definition);
        } else {
            throw new Exception("unknown resource type: " + definitionPath);
        }

        return new DefinitionResource(resource);
    }

    @RequestMapping(value = DEFINITION_MAP + "/**", method = { RequestMethod.POST, RequestMethod.PUT })
    public DefinitionResource putRawDefinitionMap(@RequestBody String definition, HttpServletRequest request)
            throws Exception {

        // String path = (String)
        // request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        // if (definitionPath.indexOf(".") == -1) { // it is a package (directory)
        // IContainer container = new ContainerResource();
        // container.setPath(RESOURCE_ROOT + "/" + definitionPath);
        // resourceManager.createFolder(container);
        // return new DefinitionResource(container);
        // }
        String definitionPath = RESOURCE_ROOT + "/" + "map.json";
        String fileExt = UEngineUtil.getFileExt(definitionPath);

        // 무조건 xml 파일로 결국 저장됨.
        DefaultResource resource = new DefaultResource(definitionPath);

        if (fileExt.endsWith("json")) {
            resourceManager.save(resource, definition);
        } else {
            throw new Exception("unknown resource type: " + definitionPath);
        }

        return new DefinitionResource(resource);
    }

    @RequestMapping(value = DEFINITION_SYSTEM, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public RepresentationModel<?> getSystem() throws Exception {
        // case of directory:
        IResource resource = new DefaultResource(RESOURCE_ROOT + "/");
        if (resourceManager.exists(resource) && resourceManager.isContainer(resource)) { // is a folder
            return _listSystem(RESOURCE_ROOT, "system");
        }

        return null;
    }

    private CollectionModel<DefinitionResource> _listSystem(String resourceRoot, String basePath) throws Exception {

        if (basePath == null) {
            basePath = "";
        }

        IContainer resource = new ContainerResource();
        resource.setPath(resourceRoot + "/" + basePath);
        List<IResource> resources = resourceManager.listFiles(resource);

        List<DefinitionResource> definitions = new ArrayList<DefinitionResource>();
        for (IResource resource1 : resources) {
            DefinitionResource definition = new DefinitionResource(resource1);
            applyDefinitionResourceId(definition);
            enrichActorNamesIfEnabled(definition, false);
            definitions.add(definition);
        }

        return CollectionModel.of(definitions);
    }

    @RequestMapping(value = DEFINITION
            + "/release/{releaseVerison}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> releaseVersions(@PathVariable("releaseVerison") String releaseVerison) throws Exception {
        IContainer resourceDir = new ContainerResource(RESOURCE_ROOT);
        if (!resourceManager.exists(resourceDir) || !resourceManager.isContainer(resourceDir)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found or is not a directory");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            zipDirectory(resourceDir, zipOutputStream);
        }

        byte[] zipBytes = byteArrayOutputStream.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + releaseVerison + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);

    }

    private void zipDirectory(IContainer folder, ZipOutputStream zipOutputStream) throws Exception {
        for (IResource resource : resourceManager.listFiles(folder)) {
            if (resource instanceof IContainer) {
                zipDirectory((IContainer) resource, zipOutputStream);
                continue;
            }
            zipOutputStream.putNextEntry(new ZipEntry(resource.getPath()));
            try (InputStream inputStream = resourceManager.getInputStream(resource)) {
                IOUtils.copy(inputStream, zipOutputStream);
            }
            zipOutputStream.closeEntry();
        }
    }

    public void unzipDirectory(InputStream inputStream, String releaseName) throws Exception {
        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                String fileName = zipEntry.getName();

                if (zipEntry.isDirectory()) {
                    IContainer container = new ContainerResource(fileName);
                    resourceManager.createFolder(container);
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    DefaultResource resource = new DefaultResource(fileName);
                    String content = baos.toString(StandardCharsets.UTF_8);
                    resourceManager.save(resource, content);
                    addArchive(fileName, releaseName, content);
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        } catch (IllegalArgumentException e) {
            System.err.println("Error processing ZIP entry: " + e.getMessage());
            throw new IOException("Error processing ZIP entry", e);
        }
    }

    private void addArchive(String fileName, String releaseName, String definition) throws Exception {
        if (fileName.lastIndexOf('.') == -1)
            return;
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        if (".bpmn".equals(extension) || ".form".equals(extension)) {
            String folderName = fileName.replace(RESOURCE_ROOT + "/", "");
            DefaultResource versionResource = new DefaultResource(
                    ARCHIVE_ROOT + "/" + folderName + "/" + releaseName + extension);
            resourceManager.save(versionResource, definition);
        }
    }

    @RequestMapping(value = "/definition/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDefinition(@RequestParam("file") MultipartFile file) {
        try {
            String releaseNameWithoutExtension = file.getOriginalFilename().substring(0,
                    file.getOriginalFilename().lastIndexOf('.'));
            unzipDirectory(file.getInputStream(), releaseNameWithoutExtension);

            return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @RequestMapping(value = DEFINITION_SYSTEM
            + "/**", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawSystem(HttpServletRequest request) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String name = path.substring(DEFINITION_SYSTEM.length());
        String definitionPath = RESOURCE_ROOT + "/system" + name + ".json";
        Object definition = getDefinitionLocal(definitionPath, null);

        // if(unwrap) {
        // return objectMapper.writeValueAsString(definition);
        // }else{
        // DefinitionWrapper definitionWrapper = new DefinitionWrapper(definition);
        // String uEngineProcessJSON =
        // objectMapper.writeValueAsString(definitionWrapper);
        return definition;
        // }

    }

    @RequestMapping(value = DEFINITION_MAP, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawDefinitionMap() throws Exception {
        String definitionPath = RESOURCE_ROOT + "/" + "map.json";
        DefaultResource resource = new DefaultResource(definitionPath);
        try {
            if (!resourceManager.exists(resource)) {
                return defaultDefinitionMapJson();
            }
            Object definition = getDefinitionLocal(definitionPath, null);
            return definition != null ? definition : defaultDefinitionMapJson();
        } catch (Exception e) {
            return defaultDefinitionMapJson();
        }
    }

    private static Object defaultDefinitionMapJson() {
        Map<String, List<?>> defaultDefinitionMap = new LinkedHashMap<>();
        defaultDefinitionMap.put("mega_proc_list", new ArrayList<>());
        return defaultDefinitionMap;
    }

    private static final String METRICS_JSON = "metrics.json";

    @RequestMapping(value = DEFINITION_METRICS, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawDefinitionMetrics() throws Exception {
        String definitionPath = RESOURCE_ROOT + "/" + METRICS_JSON;
        DefaultResource resource = new DefaultResource(definitionPath);
        try {
            if (!resourceManager.exists(resource)) {
                return defaultMetricsJson();
            }
            Object definition = getDefinitionLocal(resource.getPath(), null);
            return definition != null ? definition : defaultMetricsJson();
        } catch (Exception e) {
            return defaultMetricsJson();
        }
    }

    private static Object defaultMetricsJson() {
        Map<String, List<?>> defaultMetrics = new LinkedHashMap<>();
        defaultMetrics.put("domains", new ArrayList<>());
        defaultMetrics.put("mega_processes", new ArrayList<>());
        defaultMetrics.put("processes", new ArrayList<>());
        return defaultMetrics;
    }

    @RequestMapping(value = DEFINITION_METRICS, method = RequestMethod.PUT, consumes = "text/plain")
    public void putRawDefinitionMetrics(@RequestBody String metricsJson) throws Exception {
        String definitionPath = RESOURCE_ROOT + "/" + METRICS_JSON;
        DefaultResource resource = new DefaultResource(definitionPath);
        resourceManager.save(resource, metricsJson);
    }

    @RequestMapping(value = DEFINITION
            + "/xml/{defPath:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String getXMLDefinition(@PathVariable("defPath") String definitionPath,
            @RequestParam("version") String version) throws Exception {
        Object xml = getDefinitionLocal(toDefinitionResourcePath(definitionPath), version);
        if (xml == null) {
            throw new ResourceNotFoundException();
        }
        return String.valueOf(xml);

    }

    @RequestMapping(value = DEFINITION
            + "/xml/**", method = RequestMethod.GET, produces = "application/xml;charset=UTF-8")
    public String getXMLDefinition(HttpServletRequest request) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring((DEFINITION + "/xml").length() + 1);

        String version = request.getParameter("version");

        return getXMLDefinition(definitionPath, version);

    }

    public Object getDefinitionLocal(String definitionPath, String version) throws Exception {
        try {
            String normalizedPath = definitionPath.startsWith(RESOURCE_ROOT)
                    ? definitionPath
                    : toDefinitionResourcePath(definitionPath);
            if (version != null && !version.isBlank()) {
                normalizedPath = toArchiveResourcePath(normalizedPath, version);
            }
            IResource resource = new DefaultResource(normalizedPath);
            if (!resourceManager.exists(resource)) {
                throw new ResourceNotFoundException();
            }
            Object definition = resourceManager.getObject(resource);

            return definition;

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new RuntimeException("Error when to load definition: " + definitionPath, e);
        }

    }

    private String toDefinitionResourcePath(String definitionPath) {
        String normalized = definitionPath == null ? "" : definitionPath.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith(RESOURCE_ROOT + "/")) {
            return normalized;
        }
        return RESOURCE_ROOT + (normalized.isEmpty() ? "" : "/" + normalized);
    }

    private String toArchiveResourcePath(String definitionPath, String version) {
        String currentPath = toDefinitionResourcePath(definitionPath);
        String relativePath = currentPath.startsWith(RESOURCE_ROOT + "/")
                ? currentPath.substring((RESOURCE_ROOT + "/").length())
                : currentPath;
        return ARCHIVE_ROOT + "/" + relativePath + "/" + version + ".bpmn";
    }

    // --------------- 요소별 댓글 (DefinitionService Feign 계약 구현) ---------------

    @Override
    @RequestMapping(value = "/definition/element-comments", method = RequestMethod.GET)
    public List<ElementCommentDto> listElementComments(
            @RequestParam("procDefId") String procDefId,
            @RequestParam(value = "elementId", required = false) String elementId) {
        return procDefDataService.listComments(procDefId, elementId);
    }

    @Override
    @RequestMapping(value = "/definition/element-comment-counts", method = RequestMethod.GET)
    public java.util.Map<String, java.util.Map<String, Integer>> getElementCommentCounts(
            @RequestParam("procDefId") String procDefId) {
        return procDefDataService.getElementCommentCounts(procDefId);
    }

    @Override
    @RequestMapping(value = "/definition/element-comments", method = RequestMethod.POST, consumes = "application/json")
    public ElementCommentDto createElementComment(@RequestBody ElementCommentCreateRequest request) {
        HttpServletRequest req = currentRequest();
        return procDefDataService.createComment(request, req);
    }

    @Override
    @RequestMapping(value = "/definition/element-comments/{commentId}", method = RequestMethod.PATCH, consumes = "application/json")
    public ElementCommentDto updateElementComment(
            @PathVariable("commentId") String commentId,
            @RequestBody ElementCommentPatchRequest request) {
        return procDefDataService.updateCommentContent(commentId, request)
                .orElseThrow(() -> new ResourceNotFoundException("comment not found: " + commentId));
    }

    @Override
    @RequestMapping(value = "/definition/element-comments/{commentId}", method = RequestMethod.DELETE)
    public void deleteElementComment(@PathVariable("commentId") String commentId) {
        if (!procDefDataService.deleteComment(commentId)) {
            throw new ResourceNotFoundException("comment not found: " + commentId);
        }
    }

    @Override
    @RequestMapping(value = "/definition/element-comments/{commentId}/resolve", method = RequestMethod.PATCH, consumes = "application/json")
    public ElementCommentDto resolveElementComment(
            @PathVariable("commentId") String commentId,
            @RequestBody ElementCommentResolveRequest request) {
        HttpServletRequest req = currentRequest();
        return procDefDataService.resolveComment(commentId, request, req)
                .orElseThrow(() -> new ResourceNotFoundException("comment not found: " + commentId));
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    // --------------- 동시 수정 방지 Lock API ---------------

    /**
     * path 쿼리 파라미터 지원: 슬래시 포함 id 시 GET
     * /definition/lock?path=부산은행/credit_review_call (400 방지)
     */
    @RequestMapping(value = "/definition/lock", method = RequestMethod.GET, produces = "application/json;charset=UTF-8", params = "path")
    public DefinitionLockDto getLockByPath(@RequestParam("path") String path) {
        return definitionLockService.getLock(path).orElse(null);
    }

    @RequestMapping(value = "/definition/lock/{id:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public DefinitionLockDto getLock(@PathVariable("id") String id) {
        return definitionLockService.getLock(id).orElse(null);
    }

    @RequestMapping(value = "/definition/lock", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json;charset=UTF-8")
    public DefinitionLockDto putLock(@RequestBody DefinitionLockDto body) {
        HttpServletRequest req = currentRequest();
        String userId = body.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = DefinitionLockService.getCurrentUserId(req);
        }
        String resourceId = body.getId();
        if (resourceId == null || resourceId.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "id is required");
        }
        return definitionLockService.putLock(resourceId, userId);
    }

    /**
     * path 쿼리 파라미터 지원: 슬래시 포함 id 시 DELETE /definition/lock?path=... (라우팅/인코딩 이슈 방지)
     */
    @RequestMapping(value = "/definition/lock", method = RequestMethod.DELETE, params = "path")
    public ResponseEntity<Void> deleteLockByPath(@RequestParam("path") String path) {
        definitionLockService.deleteLock(path);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/definition/lock/{id:.+}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteLock(@PathVariable("id") String id) {
        definitionLockService.deleteLock(id);
        return ResponseEntity.noContent().build();
    }
}
