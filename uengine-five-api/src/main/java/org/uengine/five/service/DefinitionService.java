package org.uengine.five.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = DEFINITION, method = RequestMethod.GET, produces = "application/hal+json;charset=UTF-8")
    public RepresentationModel listDefinition(@RequestParam(value = "basePath", required = false) String basePath)
            throws Exception;

    /**
     * Raw JSON (HAL) listing for callers that want to parse files themselves.
     * Useful for non-HATEOAS clients (e.g. process-service business rules).
     */
    @RequestMapping(value = DEFINITION, method = RequestMethod.GET, produces = "application/json")
    public String listDefinitionRaw(@RequestParam(value = "basePath", required = false) String basePath)
            throws Exception;

    @RequestMapping(value = "/version/production", method = RequestMethod.GET)
    public RepresentationModel getProduction() throws Exception;

    @RequestMapping(value = "/version/{version}" + DEFINITION, method = RequestMethod.GET)
    public RepresentationModel listVersionDefinitions(@PathVariable("version") String version,
            @RequestParam(value = "basePath", required = false) String basePath) throws Exception;

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public RepresentationModel listVersions() throws Exception;

    @RequestMapping(value = DEFINITION
            + "/{defPath}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public RepresentationModel getDefinition(@PathVariable("defPath") String definitionPath) throws Exception;

    /**
     * Feign 안정성을 위해 raw definition 경로는 query parameter로 전달한다.
     * (regex path variable {defPath:.+} 는 Feign contract 파싱에서 문제를 일으킬 수 있음)
     */
    @RequestMapping(value = DEFINITION_RAW, method = RequestMethod.GET)
    public Object getRawDefinition(@RequestParam("defPath") String definitionPath) throws Exception;

    @RequestMapping(value = DEFINITION_RAW, method = RequestMethod.PUT, consumes = "application/json")
    public Object putRawDefinition(@RequestParam("defPath") String definitionPath,
            @RequestBody DefinitionRequest definitionRequest) throws Exception;

    @RequestMapping(value = DEFINITION_MAP, method = RequestMethod.GET)
    public Object getRawDefinitionMap() throws Exception;

}
