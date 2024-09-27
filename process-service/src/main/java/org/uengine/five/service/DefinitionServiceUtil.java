package org.uengine.five.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.ProcessDefinition;

/**
 * Created by uengine on 2017. 11. 15..
 */
@Component
public class DefinitionServiceUtil {

    @Autowired
    DefinitionXMLService definitionService;

    static BpmnXMLParser bpmnXMLParser = new BpmnXMLParser();

    public Object getDefinition(String defPath) throws Exception {
        return getDefinition(defPath, null);
    }

    public Object getDefinition(String defPath, String version) throws Exception {
        if (!defPath.endsWith(".bpmn")) {
            defPath = defPath + ".bpmn";
        }

        ProcessTransactionContext tc = ProcessTransactionContext.getThreadLocalInstance();

        ProcessDefinition processDefinition = (ProcessDefinition) tc.getSharedContext("def" + version + ":" + defPath);
        if (processDefinition == null) {
            Object returned = definitionService.getXMLDefinition(defPath, version);
            String xml = (String) returned;

            processDefinition = bpmnXMLParser.parse(xml);

            tc.setSharedContext("def" + version + ":" + defPath, processDefinition);
        }

        int extIndex = defPath.lastIndexOf(".");
        if (extIndex != -1) {
            defPath = defPath.substring(0, extIndex);
        }
        processDefinition.setId(defPath);

        return processDefinition;
    }

}
