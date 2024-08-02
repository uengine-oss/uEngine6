package org.uengine.kernel;

import java.util.*;

/**
 * @author Jinyoung Jang
 */

public class Not extends Condition{
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	Condition condition;
		public Condition getCondition() {
			return condition;
		}
		public void setCondition(Condition condition) {
			this.condition = condition;
		}

    /*
     * Jackson은 기본적으로 기본 생성자(파라미터가 없는 생성자)를 사용하여 객체를 생성합니다. 
     * 클래스에 기본 생성자가 없으면 Jackson은 객체를 생성할 수 없습니다.
     */
    public Not() {
    }
	public Not(Condition condition){
		this.condition = condition;
	}
	
	public boolean isMet(ProcessInstance instance, String scope) throws Exception{
		return !condition.isMet(instance, scope);		
	}
	
	public String toString(){
		String exp = condition.toString();
		return "not (" + exp + ")";
	}

	public ValidationContext validate(Map options){
		return condition.validate(options);		
	}


}