package org.uengine.five.dto;

import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.*;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.uengine.five.service.DefinitionService;
import org.uengine.five.service.InstanceService;
import org.uengine.kernel.ProcessInstance;
import org.uengine.util.UEngineUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by uengine on 2017. 11. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Relation(value = "instance", collectionRelation = "instances")
public class InstanceResource extends RepresentationModel {

        String name;

        String instanceId;

        String status;

        public String getInstanceId() {
                return instanceId;
        }

        public void setInstanceId(String instanceId) {
                this.instanceId = instanceId;
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }

        public InstanceResource() {
        }

        public InstanceResource(ProcessInstance processInstance) throws Exception {
                setName(processInstance.getName());
                setInstanceId(processInstance.getInstanceId());
                setStatus(processInstance.getStatus());

                add(
                                linkTo(
                                                methodOn(InstanceService.class)
                                                                .getInstance(processInstance.getInstanceId()))
                                                .withSelfRel());

                add(
                                linkTo(
                                                methodOn(InstanceService.class)
                                                                .getProcessVariables(processInstance.getInstanceId()))
                                                .withRel("variables"));

                add(
                                linkTo(
                                                methodOn(org.uengine.five.service.InstanceService.class)
                                                                .getRoleMapping(processInstance.getInstanceId(), null))
                                                .withRel("role-mapping"));

                add(
                                linkTo(
                                                methodOn(DefinitionService.class).getDefinition(UEngineUtil
                                                                .getNamedExtFile(processInstance.getProcessDefinition()
                                                                                .getId(), "json")))
                                                .withRel("definition"));

                add(
                                linkTo(
                                                methodOn(DefinitionService.class).getRawDefinition(UEngineUtil
                                                                .getNamedExtFile(processInstance.getProcessDefinition()
                                                                                .getId(), "json")))
                                                .withRel("rawDefinition"));

                // if (!processInstance.isRunning(""))
                // add(
                // linkTo(
                // methodOn(InstanceService.class)
                // .start(processInstance.getInstanceId()))
                // .withRel("start"));
                // else {

                // TODO: create stop method
                add(
                                linkTo(
                                                methodOn(InstanceService.class)
                                                                .stop(processInstance.getInstanceId()))
                                                .withRel("stop"));

                // TODO: create resume method
                if (processInstance.isSuspended("")) {
                        add(
                                        linkTo(
                                                        methodOn(InstanceService.class).resume(
                                                                        processInstance.getInstanceId()))
                                                        .withRel("resume"));

                } else {
                        // TODO: create suspend method
                        add(
                                        linkTo(
                                                        methodOn(InstanceService.class)
                                                                        .stop(processInstance.getInstanceId()))
                                                        .withRel("suspend"));

                }
                // }

                // EntityLinks entityLinks = GlobalContext.getComponent(EntityLinks.class);

                // if(entityLinks!=null){
                // add(
                // entityLinks.linkForSingleResource(ProcessInstanceEntity.class, new
                // Long(processInstance.getInstanceId())).withRel("entity")
                // );
                // }

        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

}
