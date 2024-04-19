package org.uengine.five.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
        return getDefinition(defPath, false);
    }

    public Object getDefinition(String defPath, boolean production) throws Exception {

        if (defPath.indexOf(".bpmn") == -1) {
            defPath = defPath + ".bpmn";
        }
        Object returned = definitionService.getXMLDefinition(defPath, production);
        String xml = (String) returned;

        ProcessDefinition processDefinition = bpmnXMLParser.parse(xml);

        int extIndex = defPath.lastIndexOf(".");
        if (extIndex != -1) {
            defPath = defPath.substring(0, extIndex);
        }
        processDefinition.setId(defPath);

        return processDefinition;
    }

}
