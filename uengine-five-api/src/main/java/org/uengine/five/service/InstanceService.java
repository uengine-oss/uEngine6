package org.uengine.five.service;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.Message;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.dto.StartAndCompleteCommand;
import org.uengine.five.dto.WorkItemResource;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.RoleMapping;

/**
 * Created by uengine on 2017. 8. 9..
 *
 * Implementation Principles:
 * - REST Maturity Level : 2
 * - Not using old uEngine ProcessManagerBean, this replaces the
 * ProcessManagerBean
 * - ResourceManager and CachedResourceManager will be used for definition
 * caching (Not to use the old DefinitionFactory)
 * - json must be Typed JSON to enable object polymorphism - need to change the
 * jackson engine. TODO: accept? typed json is sometimes hard to read
 */
@FeignClient(name = "bpm", url = "http://process-service:9094")
public interface InstanceService {

        @RequestMapping(value = "/instance", consumes = "application/json;charset=UTF-8", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public RepresentationModel start(@RequestBody ProcessExecutionCommand command) throws Exception;

        // @RequestMapping(value = "/instance/{instanceId}/start", method =
        // RequestMethod.POST)
        // public RepresentationModel start(@PathVariable("instanceId") String
        // instanceId) throws Exception;

        @RequestMapping(value = "/instance/{instanceId}/stop", method = RequestMethod.POST)
        public RepresentationModel stop(@PathVariable("instanceId") String instanceId) throws Exception;

        @RequestMapping(value = "/instance/{instanceId}/resume", method = RequestMethod.POST)
        public RepresentationModel resume(@PathVariable("instanceId") String instanceId) throws Exception;

        @RequestMapping(value = "/instance/{instanceId}", method = RequestMethod.GET)
        public RepresentationModel getInstance(@PathVariable("instanceId") String instanceId) throws Exception;

        @RequestMapping(value = "/instance/{instanceId}/activity/{tracingTag}/backToHere", method = RequestMethod.POST)
        public RepresentationModel backToHere(@PathVariable("instanceId") String instanceId,
                        @PathVariable("tracingTag") String tracingTag) throws Exception;

        @RequestMapping(value = "/instance/{instanceId}/messages", method = RequestMethod.POST)
        public void postMessage(@PathVariable("instanceId") String instanceId,
                        @RequestBody Message message) throws Exception;

        // @RequestMapping(value = "/instance/definition/", method = RequestMethod.POST)
        // public void onDeploy() throws Exception;

        @RequestMapping(value = "/instance/{instanceId}/variables", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public Map getProcessVariables(@PathVariable("instanceId") String instanceId) throws Exception;

        @RequestMapping(value = "/instance/{instId}/variable/{varName}", method = RequestMethod.GET)
        public Serializable getVariable(@PathVariable("instId") String instId, @PathVariable("varName") String varName)
                        throws Exception;

        @RequestMapping(value = "/instance/{instId}/task/{taskId}/variable/{varName}", method = RequestMethod.GET)
        public Serializable getVariableWithTaskId(@PathVariable("instId") String instId,
                        @PathVariable("taskId") String taskId,
                        @PathVariable("varName") String varName)
                        throws Exception;

        @RequestMapping(value = "/instance/{instanceId}/variable/{varName}", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
        public void setVariable(@PathVariable("instanceId") String instanceId, @PathVariable("varName") String varName,
                        @RequestBody String varValue) throws Exception;

        @RequestMapping(value = "/instance/{instanceId}/task/{taskId}/variable/{varName}", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
        public void setVariableWithTaskId(@PathVariable("instanceId") String instanceId,
                        @PathVariable("taskId") String taskId, @PathVariable("varName") String varName,
                        @RequestBody String json) throws Exception;

        @RequestMapping(value = "/instance/{instId}/role-mapping/{roleName}", method = RequestMethod.GET)
        public RoleMapping getRoleMapping(@PathVariable("instId") String instId,
                        @PathVariable("roleName") String roleName)
                        throws Exception;

        // Spring Data rest 에서는 자동객체를 JSON으로 바인딩 해주지만, 원래 스프링에서는 리스폰스에 대해 스프링 프레임웤이 해석할
        // 수 있는 미디어타입을 xml 에 일일히 설정했었음.
        // produces 의 의미는. 리스폰스 헤더에 콘텐트타입을 설정해줌. 그래야 브라우저가 json 객체로 받아들인다.
        @RequestMapping(value = "/instance/{instanceId}/role-mapping/{roleName}", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
        public Object setRoleMapping(@PathVariable("instanceId") String instanceId,
                        @PathVariable("roleName") String roleName, @RequestBody RoleMapping roleMapping)
                        throws Exception;

        @RequestMapping(value = "/work-item/{taskId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public WorkItemResource getWorkItem(@PathVariable("taskId") String taskId) throws Exception;

        @RequestMapping(value = "/work-item/{taskId}", method = RequestMethod.POST)
        public void putWorkItem(@PathVariable("taskId") String taskId, @RequestBody WorkItemResource workItem)
                        throws Exception;

        @RequestMapping(value = "/work-item/{taskId}/complete", method = RequestMethod.POST)
        public void putWorkItemComplete(@PathVariable("taskId") String taskId, @RequestBody WorkItemResource workItem,
                        @RequestHeader("isSimulate") String isSimulate)
                        throws Exception;

        @RequestMapping(value = "/definition-changes", method = RequestMethod.POST)
        public void postCreatedRawDefinition(@RequestBody String definitionPath) throws Exception;

        @RequestMapping(value = "/dry-run", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public Object dryRun(@RequestBody ProcessExecutionCommand command) throws Exception;

        @RequestMapping(value = "/start-and-complete", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public InstanceResource startAndComplete(@RequestBody StartAndCompleteCommand command,
                        @RequestHeader("isSimulate") String isSimulate) throws Exception;

        @RequestMapping(value = "/validate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        public Serializable validate(@RequestBody String xml)
                        throws Exception;

        @GetMapping("/instance/{instanceId}")
        public ProcessInstance getProcessInstanceLocal(@PathVariable("instanceId") String instanceId) throws Exception;
}
