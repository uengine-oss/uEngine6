package org.uengine.five.service;

import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.uengine.modeling.resource.IContainer;
import org.uengine.modeling.resource.IResource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by uengine on 2017. 11. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(value = "definition", collectionRelation = "definitions")
public class DefinitionResource extends RepresentationModel {

    /** TB_BPM_PROCDEF.id 또는 TB_BPM_PDEF_VER.arcv_id 와 동일한 키(버전 행은 procDefId_version). */
    String id;

    String name;

    public DefinitionResource() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DefinitionResource(IResource resource1) throws Exception {
        setName(resource1.getName());
        setDirectory(resource1 instanceof IContainer);
        setPath(resource1.getPath());

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
                                            resource1.getPath()))
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

    String createdByName;

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    String updatedByName;

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

}
