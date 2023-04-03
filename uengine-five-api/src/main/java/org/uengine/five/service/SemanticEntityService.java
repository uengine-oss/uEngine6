package org.uengine.five.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "semantic-entity", url="http://semantic-entity:9095")
public interface SemanticEntityService {

    @RequestMapping(value = "/mean", method = RequestMethod.GET)
    public int mean(@RequestParam("expression") String expression, @RequestParam("value") String value) throws Exception;

    @RequestMapping(value = "/entity-value", method = RequestMethod.GET)
    public String entityValue(@RequestParam("expression") String expression, @RequestParam("entity-type") String entityType);
}