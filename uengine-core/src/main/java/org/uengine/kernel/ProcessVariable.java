package org.uengine.kernel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.uengine.contexts.ComplexType;
import org.uengine.contexts.JavaClassDefinition;
import org.uengine.contexts.TextContext;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.modeling.resource.VersionManager;
import org.uengine.util.UEngineUtil;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Jinyoung Jang
 */
public class ProcessVariable implements java.io.Serializable, NeedArrangementToSerialize, Cloneable, Validatable {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;


	String name;
		public String getName() {
			return name;
		}
		public void setName(String value) {
			name = value;
		}
	
	FormField[] fields;
		public FormField[] getFields() {
			return fields;
		}
		public void setFields(FormField[] fields) {
			this.fields = fields;
		}

	String html;
		public String getHtml() {
			return html;
		}
		public void setHtml(String html) {
			this.html = html;
		}

	TextContext displayName = org.uengine.contexts.TextContext.createInstance();
		public TextContext getDisplayName(){
			if(displayName.getText()==null){
				TextContext result = TextContext.createInstance();
				result.setText(getName());
				return result;
			}
			
			return displayName;
		}	
		public void setDisplayName(TextContext value){
			displayName = value;
		}
		public void setDisplayName(String value) {
			if(getName()==null){
				TextContext textCtx = new TextContext();
				textCtx.setText(value);
				setDisplayName(textCtx);		
			}
			
			getDisplayName().setText(value);
		}
	
	Class type; 
		public Class getType(){
			if(type==null){
				if(getXmlBindingClassName()!=null){
					try{
						type = GlobalContext.getComponentClass(getXmlBindingClassName());
					}catch(Exception e){
						System.out.println("Warning: Binding class not found at design time.");
					}
				}
				//The other case need to be coded: dynamic compilation of XML binding classes based on the QName
			}
			
			return type;
		}
		public void setType(Class type){
			this.type = type;
		}


	boolean global;
		public boolean isGlobal() {
			return global;
		}
		public void setGlobal(boolean global) {
			this.global = global;
		}

	//@Range(options={"BPMS", "REST", "DBMS"}, values={"bpms", "rest", "dbms"})
	String persistOption;
		public String getPersistOption() {
			return persistOption;
		}
		public void setPersistOption(String persistOption) {
			this.persistOption = persistOption;
		}

	private String typeClassName;
		public String getTypeClassName() {
			return typeClassName;
		}
		public void setTypeClassName(String typeClassName) {
		this.typeClassName = typeClassName;
	}

//	transient String typeInputter;
//		@Order(3)
//		//@Range(options={"Text","Date","Complex"}, values={"java.lang.String","java.util.Date","org.uengine.contexts.ComplexType"})
//		@Face(faceClassName = "org.uengine.kernel.face.ProcessVariableTypeSelector")
//		public String getTypeInputter() {
//			return typeInputter;
//		}
//		public void setTypeInputter(String typeInputter) {
//			this.typeInputter = typeInputter;
//		}
	
		
	Role openRole;
		public Role getOpenRole() {
			return openRole;
		}
		public void setOpenRole(Role openRole) {
			this.openRole = openRole;
		}
	
	String xmlBindingClsName;
		public String getXmlBindingClassName(){
			if(getQName()!=null && xmlBindingClsName==null)
				xmlBindingClsName = org.uengine.util.UEngineUtil.QName2ClassName(getQName());
								
			return xmlBindingClsName;
		}
		public void setXmlBindingClassName(String value){
			xmlBindingClsName = value;
		}

	QName qname;
		public QName getQName(){
			return qname;
		}
		public void setQName(QName value){
			qname = value;
		}

/*	Serializable defaultValue;
		public Serializable getDefaultValue() {
			return defaultValue;
		}
		public void setDefaultValue(Serializable object) {
			defaultValue = object;
		}*/

/*	InputterAdapter inputter;
		public InputterAdapter getInputter() {
			return inputter;
		}
		public void setInputter(InputterAdapter inputter) {
			this.inputter = inputter;
		}*/

	
/*	Validator[] validators;
		public Validator[] getValidators() {
			return validators;
		}
		public void setValidators(Validator[] validators) {
			this.validators = validators;
		}*/	
	
/*	String bindingClassName;
	
		public String getBindingClassName(){
			if(getQName()!=null && bindingClassName==null)
				bindingClassName = getQName().getLocalPart();
				
			return bindingClassName;
		}
			
		public void setBindingClassName(String value){
			bindingClassName = value;
		}
*/
	
	boolean askWhenInit = false;
		public boolean isAskWhenInit() {
			return askWhenInit;
		}
		public void setAskWhenInit(boolean b) {
			askWhenInit = b;
		}

	Object defaultValue = null;
		public Object getDefaultValue() {
			if((getType()==ComplexType.class) && (defaultValue instanceof String))
				return null;

			if(defaultValue instanceof String && !UEngineUtil.isNotEmpty((String) defaultValue)) return null;

			return defaultValue;
		}
		public void setDefaultValue(Object object) {
			defaultValue = object;
		}

	String defaultValueInString;
		public String getDefaultValueInString() {
			return defaultValueInString;
		}
		public void setDefaultValueInString(String defaultValueInString) {
			this.defaultValueInString = defaultValueInString;
		}


	boolean isVolatile;
		public boolean isVolatile() {
			return isVolatile;
		}
		public void setVolatile(boolean isVolatile) {
			this.isVolatile = isVolatile;
		}
		
		
	public boolean equals(Object obj){
		if(obj !=null && getName()!=null)
			return getName().equals(((ProcessVariable)obj).getName());
			
		return false;
	}
	
	public ProcessVariable(Object[] settings){
		org.uengine.util.UEngineUtil.initializeProperties(this, settings);
	}
	public ProcessVariable(){
		this.setDefaultValue(new String());

//		this.setUuid(UUID.randomUUID().toString());
	}

	//review: The return object of this method is only for scripting users to indicate certain process variable
	public static ProcessVariable forName(String varName){	
		//review: fly-weight pattern needed
		ProcessVariable pv = new ProcessVariable();
		pv.setName(varName);
		
		return pv;
	}

	public void set(ProcessInstance instance, String scope, String key, Serializable value) throws Exception{

		if(isGlobal()){
			if(!instance.isRoot()) { //void recursive
				instance.getRootProcessInstance().set(scope, getName(), value);
				return;
			}
		}

		
		if(getType()!=null && (value==null || !value.getClass().isAssignableFrom(getType()))){		
			if(value instanceof String){
				String strValue = (String)value;
				if(getType()==Integer.class){
					try{
System.out.println("ProcessVariable:: converting from String to Integer");
						value = new Integer(Integer.parseInt(strValue));
					}catch(Exception e){
					}
				}//review: there are more cases can be converted from string.
			}
		}
				
		instance.set(scope, getName(), value);
	}
	
	public Serializable get(ProcessInstance instance, String scope, String key) throws Exception{

		if(isGlobal()){
			if(!instance.isRoot()) //void recursive
				return instance.getRootProcessInstance().get(scope, getName());
		}

		
		if(instance==null ) return (Serializable)getDefaultValue();
		
		Serializable theValue = instance.get(scope, getName());

		return theValue;
		
	}
	
	public ProcessVariableValue getMultiple(ProcessInstance instance, String scope, String key) throws Exception{

		if(getName()==null)
			throw new IllegalArgumentException("Process variable name is empty. fail to get process variable value.");

		if(isGlobal()){
			if(!instance.isRoot()) //void recursive
				return getMultiple(instance.getRootProcessInstance(), scope, key);
		}

		

		return instance.getMultiple(scope, getName());
	}
	
	public ProcessVariableValue getMultiple(ProcessInstance instance, String scope) throws Exception{
		return getMultiple(instance, scope, null);
	}

	public Serializable get(ProcessInstance instance, String scope) throws Exception{
		return get(instance, scope, null);
	}
	
	public void set(ProcessInstance instance, String scope, Serializable value) throws Exception{

		if(getName()==null)
			throw new IllegalStateException("Processvariable should have its name. this variable name is null");

		set(instance, scope, null, value);
	}

	
	public String toString() {
		String dispName = getDisplayName().toString(); 
		
		if(dispName!=null)
			return dispName;
		
		return super.toString();
	}
	
	public void afterDeserialization() {

//		if(getDefaultValue()==null && getDefaultValueInString()!=null){
//			setDefaultValue(getDefaultValueInString());
//		}

		try {
			if (UEngineUtil.isNotEmpty(getTypeClassName())) {

				if(getTypeClassName().indexOf("/") > 0 || getTypeClassName().indexOf("#") > 0){
					setType(ComplexType.class);
				}else {
					setType(Thread.currentThread().getContextClassLoader().loadClass(getTypeClassName()));
				}
			}
		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}

		if(getDefaultValue()!=null && getDefaultValue() instanceof NeedArrangementToSerialize){
			((NeedArrangementToSerialize)getDefaultValue()).afterDeserialization();
		}

		setName(getName());

	}

	public void beforeSerialization() {

		if(getDefaultValue()!=null && getDefaultValue() instanceof NeedArrangementToSerialize){
			((NeedArrangementToSerialize) getDefaultValue()).beforeSerialization();
		}

		if(getDefaultValueInString()!=null){
		    setDefaultValue(getDefaultValueInString());
		}

		if(getType()!=null) {  //When a class class is serialized, Stackoverflow maybe occur.

			if(getTypeClassName()==null)
				setTypeClassName(getType().getName());

			setType(null);
		}
	}
	
	public static Object evaluate(Object val, ProcessInstance instance) throws Exception{
		if(val instanceof ProcessVariable){
			val = ((ProcessVariable)val).get(instance, "", null);
		}		
		return val;
	}
	
	boolean isDatabaseSynchronized;
		public boolean isDatabaseSynchronized() {
			return isDatabaseSynchronized;
		}
		public void setDatabaseSynchronized(boolean isDatabaseSynchronized) {
			this.isDatabaseSynchronized = isDatabaseSynchronized;
		}
	
//	String validationScript;
//		public String getValidationScript() {
//			return validationScript;
//		}
//		public void setValidationScript(String validationScript) {
//			this.validationScript = validationScript;
//		}
		
//	public String validateValue(Object value, ProcessInstance instance, ProcessDefinition definition){
//		BSFManager manager = new BSFManager();
//		manager.setClassLoader(this.getClass().getClassLoader());
//	
//		try {
//			manager.declareBean("instance", instance, ProcessInstance.class);
//			manager.declareBean("definition", definition, ProcessDefinition.class);
//			manager.declareBean("value", value, Object.class);
//			
//			BSFEngine engine = manager.loadScriptingEngine("javascript");
//				
////			String result = (String)engine.eval("my_class.my_generated_method",0,0,"function getVal(){\n"+ getValidationScript() + "}\ngetVal();");
////			return result;
//		} catch (BSFException e) {
//			e.printStackTrace();
//			
//			return null;
//		}
//	}
		
	public boolean shouldAccessValueInSpecializedWay(ProcessInstance instance){
		return (isGlobal() && !instance.isRoot()) || isDatabaseSynchronized();
	}
		
	public Object clone(){
		//TODO [tuning point]: Object cloning with serialization. it will be called by ProcessManagerBean.getProcessDefintionXX method.
		try{
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ObjectOutputStream ow = new ObjectOutputStream(bao);
			ow.writeObject(this);
			ByteArrayInputStream bio = new ByteArrayInputStream(bao.toByteArray());			
			ObjectInputStream oi = new ObjectInputStream(bio);
			
			ProcessVariable clonedOne =  (ProcessVariable) oi.readObject();
			clonedOne.setOpenRole(null);

			return clonedOne;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public ValidationContext validate(Map options) {

		ValidationContext validationContext = new ValidationContext();
		//validationContext.set


		return null;
	}

//	@ServiceMethod(payload = {"uuid"})
//	public void delete(@AutowiredFromClient ProcessVariablePanel processVariablePanel){
//		for(ProcessVariable processVariable : processVariablePanel.getProcessVariableList()){
//			if(Objects.equals(this.getUuid(), processVariable.getUuid())){
//				processVariablePanel.getProcessVariableList().remove(processVariable);
//				break;
//			}
//		}
//		wrapReturn(processVariablePanel);
//	}
}
