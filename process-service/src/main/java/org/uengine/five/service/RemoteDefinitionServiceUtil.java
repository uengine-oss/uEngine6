package org.uengine.five.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.NeedArrangementToSerialize;
import org.uengine.kernel.ProcessDefinition;

/**
 * Oracle/remote 모드에서 definition-service를 통해 BPMN 정의를 읽는다.
 */
public class RemoteDefinitionServiceUtil implements DefinitionServiceUtil {

    @Autowired
    private DefinitionService definitionService;

    private static final BpmnXMLParser BPMN_XML_PARSER = new BpmnXMLParser();

    @Override
    public Object getDefinition(String defPath) throws Exception {
        return getDefinition(defPath, null);
    }

    @Override
    public Object getDefinition(String defPath, String version) throws Exception {
        return getDefinition(defPath, version, null);
    }

    @Override
    public Object getDefinition(String defPath, String version, String authToken) throws Exception {
        if (!defPath.endsWith(".bpmn")) {
            defPath = defPath + ".bpmn";
        }

        ProcessTransactionContext tc = ProcessTransactionContext.getThreadLocalInstance();
        ProcessDefinition processDefinition = (ProcessDefinition) tc.getSharedContext("def:" + defPath + "@" + version);
        if (processDefinition == null) {
            String rawPath = version != null && !version.isEmpty()
                    ? defPath + "/version/" + version
                    : defPath;
            Object returned = definitionService.getRawDefinition(rawPath);
            processDefinition = toProcessDefinition(returned);
            tc.setSharedContext("def:" + defPath + "@" + version, processDefinition);
        }

        int extIndex = defPath.lastIndexOf(".");
        if (extIndex != -1) {
            defPath = defPath.substring(0, extIndex);
        }
        processDefinition.setId(defPath);
        return processDefinition;
    }

    private ProcessDefinition toProcessDefinition(Object returned) throws Exception {
        ProcessDefinition processDefinition;
        if (returned instanceof String) {
            processDefinition = BPMN_XML_PARSER.parse((String) returned);
        } else if (returned instanceof ProcessDefinition) {
            processDefinition = (ProcessDefinition) returned;
        } else {
            processDefinition = ProcessServiceApplication.objectMapper.convertValue(returned, ProcessDefinition.class);
        }

        if (processDefinition instanceof NeedArrangementToSerialize) {
            ((NeedArrangementToSerialize) processDefinition).afterDeserialization();
        }
        return processDefinition;
    }
}
