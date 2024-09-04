package org.uengine.five.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
// import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.NeedArrangementToSerialize;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.UEngineException;
import org.uengine.modeling.resource.ContainerResource;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IContainer;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.modeling.resource.Serializer;
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
    InstanceService instanceService;
    // static BpmnXMLParser bpmnXMLParser = new BpmnXMLParser();

    static ObjectMapper objectMapper = createTypedJsonObjectMapper();

    @PostConstruct
    public void init() {
    }

    @RequestMapping(value = DEFINITION, method = RequestMethod.GET)
    @Override
    public CollectionModel<DefinitionResource> listDefinition(String basePath) throws Exception {
        return _listDefinition(RESOURCE_ROOT, basePath);
    }

    @RequestMapping(value = DEFINITION + "/{defId:.+}/versions", method = RequestMethod.GET)
    @Override
    public CollectionModel<DefinitionResource> listDefinitionVersions(@PathVariable("defId") String defId) throws Exception {
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
            definitions.add(definition);
        }

        definitions.sort(Comparator.comparing(DefinitionResource::getVersion));

        CollectionModel<DefinitionResource> halResources = new CollectionModel<DefinitionResource>(definitions);
        return halResources;
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
            DefinitionResource definition = new DefinitionResource(resource1);
            definitions.add(definition);
            definition.path = definition.path.replace("definitions/", "");
        }

        CollectionModel<DefinitionResource> halResources = new CollectionModel<DefinitionResource>(definitions);
        return halResources;
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

        CollectionModel<VersionResource> halResources = new CollectionModel<VersionResource>(versionResources);
        return halResources;
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
    public RepresentationModel getDefinition(@PathVariable("defPath") String definitionPath) throws Exception {

        // case of directory:
        IResource resource = new DefaultResource(RESOURCE_ROOT + "/" + definitionPath);
        if (resourceManager.exists(resource) && resourceManager.isContainer(resource)) { // is a folder
            return listDefinition(definitionPath);
        }

        // case of file:
        // definitionPath = UEngineUtil.getNamedExtFile(definitionPath, "xml");

        resource = new DefaultResource(RESOURCE_ROOT + "/" + definitionPath);

        if (!resourceManager.exists(resource)) {
            throw new ResourceNotFoundException(); // make 404 error
        }

        DefinitionResource halDefinition = new DefinitionResource(resource);

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
            IResource resource = new DefaultResource(RESOURCE_ROOT + definitionPath);
            if (definitionPath.indexOf(".") == -1) { // it is a package (directory)
                IContainer container = new ContainerResource();
                container.setPath(RESOURCE_ROOT + "/" + definitionPath);
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
            container.setPath(RESOURCE_ROOT + definitionPath + "/" + newResource.getName());
            resourceManager.createFolder(container);

            return new DefinitionResource(container);
        }

    }

    @RequestMapping(value = DEFINITION + "/**", method = { RequestMethod.DELETE })
    public void deleteDefinition(HttpServletRequest request) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring(DEFINITION_RAW.length());
        if (definitionPath.indexOf(".") != -1) {
            definitionPath = UEngineUtil.getNamedExtFile(definitionPath, "xml");
        }
        IResource resource = new DefaultResource(RESOURCE_ROOT + "/" + definitionPath);
        resourceManager.delete(resource);

    }

    // ----------------- raw definition services -------------------- //

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
    @RequestMapping(value = DEFINITION_RAW + "/**", method = { RequestMethod.POST, RequestMethod.PUT })
    public DefinitionResource putRawDefinition(@RequestBody DefinitionRequest definitionRequest,
            HttpServletRequest request)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        String definitionPath = path.substring(DEFINITION_RAW.length());
        if (definitionPath.indexOf(".") == -1) { // it is a package (directory)
            IContainer container = new ContainerResource();
            container.setPath(RESOURCE_ROOT + "/" + definitionPath);
            resourceManager.createFolder(container);
            return new DefinitionResource(container);
        }
        // definitionPath = definitionPath + ".bpmn";
        String fileExt = UEngineUtil.getFileExt(definitionPath);

        // 무조건 xml 파일로 결국 저장됨.
        if (definitionRequest.getVersion() != null) {
            DefaultResource versionResource = new DefaultResource(
                    "/archive" + definitionPath + "/" + definitionRequest.getVersion() + ".bpmn");
            resourceManager.save(versionResource, definitionRequest.getDefinition());
        }

        DefaultResource resource = new DefaultResource(RESOURCE_ROOT + definitionPath);
        Object definitionDeployed = null;

        resourceManager.save(resource, definitionRequest.getDefinition());
        instanceService.postCreatedRawDefinition(resource.getPath());

        // TODO: deploy filter 로 등록된 bean 들을 호출:
        if (definitionDeployed != null && definitionDeployed instanceof ProcessDefinition) {
            invokeDeployFilters((ProcessDefinition) definitionDeployed, resource.getPath());
        }

        return new DefinitionResource(resource);
    }

    @RequestMapping(value = DEFINITION_RAW
            + "/{defPath:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawDefinition(@PathVariable("defPath") String definitionPath/*
                                                                                  * , @RequestParam(value = "unwrap",
                                                                                  * required = false) boolean unwrap
                                                                                  */) throws Exception {
        if (definitionPath.indexOf(".") == -1) {
            definitionPath = UEngineUtil.getNamedExtFile(RESOURCE_ROOT + "/" + definitionPath, "xml");
        }
        if(!(definitionPath.startsWith(RESOURCE_ROOT) || definitionPath.startsWith("archive"))) {
            definitionPath = RESOURCE_ROOT + "/" + definitionPath;
        }
        // 무조건 xml 파일로 결국 저장됨.   
        DefaultResource resource = new DefaultResource(definitionPath);
        Serializable definition = (Serializable) getDefinitionLocal(resource.getPath());

        // if(unwrap) {
        // return objectMapper.writeValueAsString(definition);
        // }else{
        // DefinitionWrapper definitionWrapper = new DefinitionWrapper(definition);
        // String uEngineProcessJSON =
        // objectMapper.writeValueAsString(definitionWrapper);
        return definition;
        // }

    }

    @RequestMapping(value = DEFINITION_RAW
            + "/**", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getRawDefinition(HttpServletRequest request/* , @RequestParam("unwrap") boolean unwrap */)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring(DEFINITION_RAW.length() + 1);

        return getRawDefinition(definitionPath);

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

        Object definitionDeployed = null;

        if (fileExt.endsWith("json")) {
            resourceManager.save(resource, definition);
        } else {
            throw new Exception("unknown resource type: " + definitionPath);
        }

        // TODO: deploy filter 로 등록된 bean 들을 호출:
        if (definitionDeployed != null && definitionDeployed instanceof ProcessDefinition) {
            invokeDeployFilters((ProcessDefinition) definitionDeployed,
                    resource.getPath().substring(RESOURCE_ROOT.length() + 2));
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

        Object definitionDeployed = null;

        if (fileExt.endsWith("json")) {
            resourceManager.save(resource, definition);
        } else {
            throw new Exception("unknown resource type: " + definitionPath);
        }

        // TODO: deploy filter 로 등록된 bean 들을 호출:
        if (definitionDeployed != null && definitionDeployed instanceof ProcessDefinition) {
            invokeDeployFilters((ProcessDefinition) definitionDeployed,
                    resource.getPath().substring(RESOURCE_ROOT.length() + 2));
        }

        return new DefinitionResource(resource);
    }

    private void invokeDeployFilters(ProcessDefinition definitionDeployed, String path) throws UEngineException {

        Map<String, DeployFilter> filters = GlobalContext.getComponents(DeployFilter.class);
        if (filters != null && filters.size() > 0) {
            for (DeployFilter theFilter : filters.values()) {
                try {
                    theFilter.beforeDeploy(definitionDeployed, null, path, true);
                } catch (Exception e) {
                    throw new UEngineException("Error when to invoke DeployFilter: " + theFilter.getClass().getName(),
                            e);
                }
            }
        }
    }

    @RequestMapping(value = DEFINITION_SYSTEM, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public RepresentationModel getSystem() throws Exception {

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
            definitions.add(definition);
        }

        CollectionModel<DefinitionResource> halResources = new CollectionModel<DefinitionResource>(definitions);
        return halResources;
    }

    @RequestMapping(value = DEFINITION
            + "/release/{releaseVerison}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity releaseVersions(@PathVariable("releaseVerison") String releaseVerison) throws Exception {

        // // case of directory:
        // IResource resource = new DefaultResource(RESOURCE_ROOT + "/");
        // if (resourceManager.exists(resource) &&
        // resourceManager.isContainer(resource)) { // is a folder
        // return _listSystem(RESOURCE_ROOT, "system");
        // }

        File resourceDir = new File(RESOURCE_ROOT + "/");
        if (resourceDir.exists() && resourceDir.isDirectory()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            zipDirectory(resourceDir, resourceDir.getName(), zipOutputStream);

            zipOutputStream.close();
            byteArrayOutputStream.close();

            byte[] zipBytes = byteArrayOutputStream.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + releaseVerison + ".zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found or is not a directory");

    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zipOutputStream) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zipOutputStream);
                continue;
            }
            zipOutputStream.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, zipOutputStream);
            fileInputStream.close();
            zipOutputStream.closeEntry();
        }
    }

    public void unzipDirectory(InputStream inputStream, String releaseName) throws IOException {
        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                String filePath = fileName;
                File newFile = new File(filePath);

                System.out.println("Processing file: " + newFile.getAbsolutePath());

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory: " + parent);
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                addArchive(fileName, releaseName, filePath);

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        } catch (IllegalArgumentException e) {
            System.err.println("Error processing ZIP entry: " + e.getMessage());
            throw new IOException("Error processing ZIP entry", e);
        }
    }

    private void addArchive(String fileName, String releaseName, String filePath) throws IOException {
        if (fileName.lastIndexOf('.') == -1)
            return;
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String uploadPath = ARCHIVE_ROOT + File.separator;
        if (".bpmn".equals(extension) || ".form".equals(extension)) {
            String folderName = fileName.replace(RESOURCE_ROOT + "/", "");
            File folder = new File(uploadPath, folderName);

            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("폴더 생성 실패: " + folder.getAbsolutePath());
            }
            File sourceFile = new File(filePath);
            File targetReleaseFile = new File(uploadPath + folderName, releaseName + extension);
            byte[] buffer = new byte[1024];
            try (FileInputStream fis = new FileInputStream(sourceFile);
                    FileOutputStream fos = new FileOutputStream(targetReleaseFile)) {
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
        }
    }

    @RequestMapping(value = "/definition/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDefinition(@RequestParam("file") MultipartFile file) {
        try {
            String uploadPath = RESOURCE_ROOT + File.separator;
            File targetFile = new File(uploadPath);

            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }

            if (targetFile.exists()) {
                String releaseNameWithoutExtension = file.getOriginalFilename().substring(0,
                        file.getOriginalFilename().lastIndexOf('.'));
                unzipDirectory(file.getInputStream(), releaseNameWithoutExtension);
            }

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
        // 무조건 xml 파일로 결국 저장됨.
        DefaultResource resource = new DefaultResource(definitionPath);
        Serializable definition = (Serializable) getDefinitionLocal(resource.getPath());

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
        // 무조건 xml 파일로 결국 저장됨.
        DefaultResource resource = new DefaultResource(definitionPath);
        Serializable definition = (Serializable) getDefinitionLocal(resource.getPath());

        // if(unwrap) {
        // return objectMapper.writeValueAsString(definition);
        // }else{
        // DefinitionWrapper definitionWrapper = new DefinitionWrapper(definition);
        // String uEngineProcessJSON =
        // objectMapper.writeValueAsString(definitionWrapper);
        return definition;
        // }

    }

    @RequestMapping(value = DEFINITION
            + "/xml/{defPath:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String getXMLDefinition(@PathVariable("defPath") String definitionPath,
            @RequestParam("version") String version) throws Exception {

        definitionPath = definitionPath.startsWith(RESOURCE_ROOT) ? definitionPath.replace(RESOURCE_ROOT, "")
                : definitionPath;
        definitionPath = UEngineUtil.getNamedExtFile(definitionPath, "xml");

        // replace to production version if requested:
        if (version != null) {
            VersionManager versionManager = GlobalContext.getComponent(VersionManager.class);
            versionManager.load("codi", null);

            definitionPath = versionManager.getProductionResourcePath(definitionPath);
        }

        Serializable definition = (Serializable) getDefinitionLocal(definitionPath);
        String uEngineProcessXML = Serializer.serialize(definition);
        return uEngineProcessXML;

    }

    @RequestMapping(value = DEFINITION
            + "/xml/**", method = RequestMethod.GET, produces = "application/xml;charset=UTF-8")
    public String getXMLDefinition(HttpServletRequest request) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring((DEFINITION + "/xml").length() + 1);

        String version = request.getParameter("version");

        return getXMLDefinition(definitionPath, version);

    }

    public Object getDefinitionLocal(String definitionPath) throws Exception {

        try {
            if (definitionPath.indexOf(".") == -1) {
                definitionPath = definitionPath + ".xml";
            }

            IResource resource = new DefaultResource(
                    definitionPath);
            Object definition = resourceManager.getObject(resource);

            // TODO: move to framework
            if (definition instanceof NeedArrangementToSerialize) {
                ((NeedArrangementToSerialize) definition).afterDeserialization();
            }

            if (definition instanceof ProcessDefinition) {
                ProcessDefinition processDefinition = (ProcessDefinition) definition;
                { // TODO: will be moved to afterDeserialize of ProcessDefinition
                    processDefinition.setId(resource.getPath().substring(RESOURCE_ROOT.length() + 1));
                    if (processDefinition.getName() == null) {
                        processDefinition.setName(resource.getPath());
                    }
                }
            }

            return definition;

        } catch (Exception e) {
            throw new UEngineException("Error when to load definition: " + definitionPath, e);
        }

    }

}
