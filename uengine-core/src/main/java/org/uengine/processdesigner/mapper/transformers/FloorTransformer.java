package org.uengine.processdesigner.mapper.transformers;

import java.util.Map;

import org.uengine.kernel.ProcessInstance;
import org.uengine.processdesigner.mapper.Transformer;

public class FloorTransformer extends Transformer{
	
	public FloorTransformer() {
		setName("Floor");
	}

	public String[] getInputArguments() {
		return new String[]{"input"};
	}

	@Override
	public Object transform(ProcessInstance instance, Map parameterMap, Map options) {
		Object val = parameterMap.get("input");
		return Math.floor(Double.parseDouble("" + val));
	}
}
