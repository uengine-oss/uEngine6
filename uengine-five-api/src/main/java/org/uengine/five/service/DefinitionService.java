package org.uengine.five.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//import javax.ws.rs.QueryParam;

/**
 * Created by uengine on 2017. 8. 9..
 *
 * Implementation Principles:
 * - REST Maturity Level : 3 (Hateoas)
 * - Not using old uEngine ProcessManagerBean, this replaces the
 * ProcessManagerBean
 * - ResourceManager and CachedResourceManager will be used for definition
 * caching (Not to use the old DefinitionFactory)
 * - json must be Typed JSON to enable object polymorphism - need to change the
 * jackson engine. TODO: accept? typed json is sometimes hard to read
 */
@FeignClient(name = "definition", url = "http://definition-service:9093")
public interface DefinitionService {

    public static final String DEFINITION_RAW = "/definition/raw";
    public static final String DEFINITION = "/definition";
    public static final String DEFINITION_MAP = "/definition/map";
    public static final String DEFINITION_SYSTEM = "/definition/system";

    @RequestMapping(value = DEFINITION, method = RequestMethod.GET)
    public RepresentationModel listDefinition(String basePath) throws Exception;

    @RequestMapping(value = "/version/production", method = RequestMethod.GET)
    public RepresentationModel getProduction() throws Exception;

    @RequestMapping(value = "/version/{version}" + DEFINITION, method = RequestMethod.GET)
    public RepresentationModel listVersionDefinitions(String version, String basePath) throws Exception;

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public RepresentationModel listVersions() throws Exception;

    @RequestMapping(value = DEFINITION
            + "/{defPath}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public RepresentationModel getDefinition(@PathVariable("defPath") String definitionPath) throws Exception;

    @RequestMapping(value = DEFINITION_RAW + "/{defPath}", method = RequestMethod.GET)
    public Object getRawDefinition(@PathVariable("defPath") String definitionPath/*
                                                                                  * , @RequestParam(value = "unwrap",
                                                                                  * required = false) boolean unwrap
                                                                                  */) throws Exception;

    @RequestMapping(value = DEFINITION_MAP + "/{defPath}", method = RequestMethod.GET)
    public Object getRawDefinitionMap() throws Exception;

    @RequestMapping(value = "/definition/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDefinition(@RequestParam("file") MultipartFile file);

    @RequestMapping(value = "/versions/**", method = RequestMethod.GET)
    public RepresentationModel listDefinitionVersions(HttpServletRequest request) throws Exception;
    // definiton/test.bpmn
    // definiton/test.bpmn/versions
    // version/test.bpmn

}
