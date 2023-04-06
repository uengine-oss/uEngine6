package org.uengine.processdesigner.mapper.transformers;

import java.util.Map;

import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.UEngineException;
import org.uengine.processdesigner.mapper.Transformer;

public class MaxTransformer extends Transformer{
	
	public MaxTransformer(){
		setName("Max");
	}
	
	public String[] getInputArguments() {
		return (new String[]{"value1", "value2"});
	}

	public Object transform(ProcessInstance instance, Map parameterMap,  Map options) throws UEngineException {
		Object maxValue = null;
		String[] inputArgs = getInputArguments();
		
		for(int i=0; i<inputArgs.length; i++){
			Object value = (Object) parameterMap.get(inputArgs[i]);
			try{
				switch (inputType) {
					case 'L':
						value = (long) Double.parseDouble(value.toString());
						break;
					case 'D':
						value = Double.parseDouble(value.toString());
						break;
					default:
						value = String.valueOf(value);
						break;
				}
			}catch (Exception e) {
				throw new UEngineException(e.getMessage(), e);
			}
			Comparable cmpVal = (Comparable) value;
			if(maxValue == null || cmpVal.compareTo(maxValue) > 0) maxValue = cmpVal;			
		}

		return maxValue;
	}
	
	char inputType;
		public char getInputType() {
			return inputType;
		}
	
		public void setInputType(char inputType) {
			this.inputType = inputType;
		}

}
