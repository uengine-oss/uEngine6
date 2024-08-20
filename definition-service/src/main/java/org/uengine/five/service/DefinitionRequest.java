package org.uengine.five.service;

import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.uengine.modeling.resource.IContainer;
import org.uengine.modeling.resource.IResource;
import org.uengine.util.UEngineUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by uengine on 2017. 11. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefinitionRequest {

    
    String definition;
    String version;
    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    


}
