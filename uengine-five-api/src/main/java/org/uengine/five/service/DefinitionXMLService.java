package org.uengine.five.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
// @FeignClient(name="definition", url="http://definition-service:9093")// Local
// file system 을 그냥 사용하기로함.
public interface DefinitionXMLService {

    // @RequestMapping(value = DefinitionService.DEFINITION + "/xml/{defPath}",
    // method = RequestMethod.GET, produces = "application/xml;charset=UTF-8")
    public String getXMLDefinition(@PathVariable("defPath") String definitionPath,
            @RequestParam("production") String version) throws Exception;

}