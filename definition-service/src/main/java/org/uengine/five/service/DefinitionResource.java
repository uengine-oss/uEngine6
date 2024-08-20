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
@Relation(value = "definition", collectionRelation = "definitions")
public class DefinitionResource extends RepresentationModel {

    String name;
    public DefinitionResource() {
    }

    public DefinitionResource(IResource resource1) throws Exception {
        if(resource1.getName().contains("@")) {
            String[] splitName = resource1.getName().split("@");
            String id = splitName[0];
            String name = splitName[1].split("\\.")[0];

            setName(name);
            setDirectory(resource1 instanceof IContainer);
            setPath(resource1.getPath());
        } else {
            setName(resource1.getName());
            setDirectory(resource1 instanceof IContainer);
            setPath(resource1.getPath());
        }
        
        
        add(
                linkTo(
                        methodOn(DefinitionServiceImpl.class)
                                .getDefinition(
                                    resource1.getPath()))
                        .withSelfRel());

        if (!isDirectory()) {
            add(
                    linkTo(
                            methodOn(DefinitionServiceImpl.class)
                                    .getRawDefinition(
                                            UEngineUtil.getNamedExtFile(resource1.getPath(), "json")))
                            .withRel("raw"));
            // add(
            // linkTo(
            // methodOn(DefinitionServiceImpl.class)
            // .getXMLDefinition(
            // UEngineUtil.getNamedExtFile(relativePath, "json"), false
            // )
            // ).withRel("xml")
            // );
            add(
                    ControllerLinkBuilder.linkTo(
                            methodOn(InstanceService.class)
                                    .start(
                                            null))
                            .withRel("instantiation"));

        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean directory;

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String version;
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    


}
