package org.uengine.kernel;

import org.uengine.util.UEngineUtil;

/**
 * Created by jangjinyoung on 2016. 12. 29..
 */
public class SubParamaterValueSelectionActivity extends HumanActivity {

    public SubParamaterValueSelectionActivity() {
        setName("Parameter Selection");
    }

    public String getTool() {
        String handler = "mw3." + UEngineUtil.getComponentClassName(getClass(), "handler");
        return handler;
    }

    ProcessVariable variableToBeSelected = new ProcessVariable();
        public ProcessVariable getVariableToBeSelected() {
            return variableToBeSelected;
        }
        public void setVariableToBeSelected(ProcessVariable variableToBeSelected) {
            this.variableToBeSelected = variableToBeSelected;
        }


}
