package org.uengine.processdesigner.mapper.transformers;

import java.util.Map;

import org.uengine.kernel.ProcessInstance;
import org.uengine.processdesigner.mapper.Transformer;

public class AbsTransformer extends Transformer{
	
	public AbsTransformer() {
		setName("Abs");
	}


	public String[] getInputArguments() {
		return new String[]{"input"};
	}

	@Override
	public Object transform(ProcessInstance instance, Map parameterMap, Map options) {
		Object val = parameterMap.get("input");
		return Math.abs(Double.parseDouble("" + val));
	}
}
