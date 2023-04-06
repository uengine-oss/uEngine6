package org.uengine.processdesigner.mapper.transformers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.ProcessInstance;
import org.uengine.processdesigner.mapper.Transformer;

public class DirectValueTransformer extends Transformer{
	
	public String getName() {
		return "Value";
	}
	
	public Object transform(ProcessInstance instance, Map parameterMap, Map options) {
		return getValue();
	}

	public String[] getInputArguments() {
		return new String[] {};
	}
	
	public Object value;
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}

	public Class type;
		public Class getType() {
			return type;
		}
		public void setType(Class type) {
			this.type = type;
		}
	
}
