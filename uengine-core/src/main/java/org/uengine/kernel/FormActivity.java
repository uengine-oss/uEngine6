package org.uengine.kernel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

// import org.metaworks.Type;
import org.uengine.contexts.FileContext;
import org.uengine.contexts.HtmlFormContext;
import org.uengine.contexts.MappingContext;
//import org.uengine.kernel.SimulatorProcessInstance;
import org.uengine.processdesigner.mapper.Transformer;
import org.uengine.processdesigner.mapper.TransformerMapping;
import org.uengine.util.UEngineUtil;

/**
 * TODO Insert type comment for FormActivity.
 * 
 * @author <a href="mailto:bigmahler@users.sourceforge.net">Jong-Uk Jeong</a>
 * @version $Id: FormActivity.java,v 1.78 2011/07/22 07:33:14 curonide Exp $
 */

public class FormActivity extends HumanActivity {
	private static final long serialVersionUID = GlobalContext.SERIALIZATION_UID;

	protected final static String SUBPROCESS_INST_ID = "instanceIdOfSubProcess";

	protected final static String SUBPROCESS_INST_ID_COMPLETED = "completedInstanceIdOfSPs";
	public final static String FILE_SYSTEM_DIR = GlobalContext.getPropertyString("filesystem.path",
			ProcessDefinitionFactory.DEFINITION_ROOT);

	// public static void metaworksCallback_changeMetadata(Type type) {
	// // FieldDescriptor fd;

	// type.setName("Form Activity");
	// type.removeFieldDescriptor("Input");
	// type.removeFieldDescriptor("Instruction");
	// type.removeFieldDescriptor("Message");
	// type.removeFieldDescriptor("MessageDefinition");
	// type.removeFieldDescriptor("Parameters");
	// type.removeFieldDescriptor("FromRole");

	// }

	public FormActivity() {
		super();
		setName("form");// test
		setTool("formHandler");
	}

	// TODO: 매핑 관련 개선
	// 하위 코드 ReceiveActivity 에서 상속받아서 사용하도록 수정
	// MappingContext mappingContext;

	// public MappingContext getMappingContext() {
	// return mappingContext;
	// }

	// public void setMappingContext(MappingContext mappingContext) {
	// this.mappingContext = mappingContext;
	// }

	ProcessVariable variableForHtmlFormContext;

	public ProcessVariable getVariableForHtmlFormContext() {
		return variableForHtmlFormContext;
	}

	public void setVariableForHtmlFormContext(
			ProcessVariable variableForHtmlFormContext) {
		this.variableForHtmlFormContext = variableForHtmlFormContext;
	}

	boolean mappingWhenSave;

	public boolean isMappingWhenSave() {
		return mappingWhenSave;
	}

	public void setMappingWhenSave(boolean mappingWhenSave) {
		this.mappingWhenSave = mappingWhenSave;
	}

	private static Method getMethod(Class src, String name) {
		Method meths[] = src.getMethods();
		for (int i = 0; i < meths.length; i++) {
			if (meths[i].getName().equals(name))
				return meths[i];
		}
		return null;
	}

	// public static List getParameterList(ProcessManagerRemote pm,
	// String formDefId) throws Exception {
	// // load up the formbase
	// String def = pm.getResource(formDefId);
	// Reader source = new StringReader(def);

	// // builder.addPackageFromDrl(source);

	// HashMap classes = new HashMap();

	// ArrayList parameterList = new ArrayList();

	// for (Iterator iter = classes.keySet().iterator(); iter.hasNext();) {
	// Class theClass = (Class) iter.next();

	// Method methods[] = theClass.getMethods();
	// String clsName = theClass.getName();

	// for (int k = 0; k < methods.length; k++) {
	// if (methods[k].getName().startsWith("set")) {
	// parameterList.add(clsName + ":"
	// + methods[k].getName().substring(3));
	// }
	// }
	// }

	// return parameterList;

	// }

	protected void afterComplete(ProcessInstance instance) throws Exception {
		onSave(instance, null);
		super.afterComplete(instance);
	}

	// protected void mappingOut(ProcessInstance instance) throws Exception {
	// // load up the HtmlFormContext
	// // HtmlFormContext formContext = (HtmlFormContext)
	// // getVariableForHtmlFormContext().get(instance, "");

	// ParameterContext[] params =
	// getEventSynchronization().getMappingContext().getMappingElements();
	// mappingOut(instance, params);
	// // if (params != null) {
	// // for (int i = 0; i < params.length; i++) {
	// // try {
	// // ParameterContext param = params[i];

	// // Object value = null;
	// // String targetFieldName = param.getArgument().getText();

	// // if (param.getTransformerMapping() != null) {

	// // Map options = new HashMap();
	// //
	// options.put(org.uengine.processdesigner.mapper.Transformer.OPTION_KEY_OUTPUT_ARGUMENT,
	// // param.getTransformerMapping().getLinkedArgumentName());
	// //
	// options.put(org.uengine.processdesigner.mapper.Transformer.OPTION_KEY_FORM_FIELD_NAME,
	// // targetFieldName);

	// // TransformerMapping tm = param.getTransformerMapping();
	// // Transformer transformer = tm.getTransformer();

	// // value =
	// param.getTransformerMapping().getTransformer().letTransform(instance,
	// // options);
	// // System.out.println(value);
	// // instance.setBeanProperty(targetFieldName, (Serializable) value);
	// // } else {
	// // String srcVariableName = param.getVariable().getName();

	// // value = instance.getBeanProperty(srcVariableName);

	// // ProcessVariable pv =
	// // getProcessDefinition().getProcessVariable(srcVariableName);
	// // if (getVariableForHtmlFormContext().equals(pv)) { // maps only the child
	// // fields of the form
	// // // activity's target html form
	// // if (instance.getExecutionScopeContext() == null) {
	// // if (value instanceof ProcessVariableValue) {

	// // ProcessVariableValue pvv = (ProcessVariableValue) value;
	// // ExecutionScopeContext oldEsc = instance.getExecutionScopeContext();
	// // Serializable _oleExecScope = instance.getProperty("",
	// // AbstractProcessInstance.PVKEY_EXECUTION_SCOPES);
	// // do {
	// // Serializable theValue = pvv.getValue();

	// // ExecutionScopeContext esc = instance.issueNewExecutionScope(this, this,
	// // theValue != null ? theValue.toString() : "<No Name>");
	// // instance.setExecutionScopeContext(esc);

	// // boolean resolvePartNeeded = false;
	// // String variableKey = srcVariableName;
	// // if (variableKey.indexOf('.') > 0) {
	// // resolvePartNeeded = true;
	// // }

	// // if (resolvePartNeeded) {
	// // int indexOfDot = variableKey.indexOf(".");
	// // if (indexOfDot > 0) {
	// // variableKey = variableKey.substring(indexOfDot + 1);
	// // }
	// // }

	// // HtmlFormContext formContext = (HtmlFormContext) new HtmlFormContext();
	// // HashMap<String, Serializable> map = new HashMap<String, Serializable>();
	// // ArrayList<Serializable> list = new ArrayList<Serializable>();
	// // list.add(theValue);
	// // map.put(variableKey, list);
	// // formContext.setValueMap(map);
	// // instance.set(pv.name, formContext);

	// // instance.setExecutionScopeContext(oldEsc);

	// // } while (pvv.next());

	// // pvv.setCursor(0);

	// // instance.setProperty("", AbstractProcessInstance.PVKEY_EXECUTION_SCOPES,
	// // _oleExecScope);
	// // }
	// // }
	// // instance.setBeanProperty(targetFieldName, (Serializable) value);

	// // }
	// // }
	// // } catch (Exception e) {
	// // e.printStackTrace();
	// // // if (!(instance instanceof SimulatorProcessInstance)) {
	// // throw e;
	// // // }
	// // }
	// // }
	// // }
	// }

	// protected void mappingOut(ProcessInstance instance, ParameterContext[]
	// params) throws Exception {
	// if (params == null)
	// return;

	// for (ParameterContext param : params) {
	// try {
	// if (param.getTransformerMapping() != null)
	// continue;

	// String targetFieldName = param.getArgument().getText();
	// String srcVariableName = param.getVariable().getName();
	// Object value = instance.getBeanProperty(srcVariableName);

	// ProcessVariable pv =
	// getProcessDefinition().getProcessVariable(srcVariableName);
	// if (getVariableForHtmlFormContext().equals(pv)) { // maps only the child
	// fields of the form
	// // activity's target html form
	// if (instance.getExecutionScopeContext() == null) {
	// if (value instanceof ProcessVariableValue) {

	// boolean resolvePartNeeded = false;
	// String variableKey = srcVariableName;

	// if (variableKey.indexOf('.') > 0) {
	// resolvePartNeeded = true;
	// }

	// HtmlFormContext formContext = new HtmlFormContext();
	// if (resolvePartNeeded) {
	// String firstPart = variableKey.substring(0, variableKey.indexOf('.'));
	// int indexOfDot = variableKey.indexOf(".");
	// if (indexOfDot > 0) {
	// variableKey = variableKey.substring(indexOfDot + 1);
	// }
	// formContext = (HtmlFormContext) instance.getBeanProperty(firstPart);
	// }

	// ProcessVariableValue pvv = (ProcessVariableValue) value;
	// ExecutionScopeContext oldEsc = instance.getExecutionScopeContext();
	// Serializable _oleExecScope = instance.getProperty("",
	// AbstractProcessInstance.PVKEY_EXECUTION_SCOPES);
	// pvv.setCursor(0);
	// do {
	// Serializable theValue = pvv.getValue();
	// HtmlFormContext formContextCopy = new HtmlFormContext();
	// formContextCopy.setValueMap(formContext.getValueMap());

	// ExecutionScopeContext esc = instance.issueNewExecutionScope(this, this,
	// theValue != null ? theValue.toString() : "<No Name>");
	// instance.setExecutionScopeContext(esc);
	// ArrayList<Serializable> list = new ArrayList<Serializable>();
	// list.add(theValue);
	// formContextCopy.getValueMap().put(variableKey, list);
	// instance.set(pv.name, formContextCopy);

	// instance.setExecutionScopeContext(oldEsc);

	// } while (pvv.next());

	// pvv.setCursor(0);

	// instance.setProperty("", AbstractProcessInstance.PVKEY_EXECUTION_SCOPES,
	// _oleExecScope);
	// }
	// }
	// instance.setBeanProperty(targetFieldName, (Serializable) value);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw e;
	// }
	// }

	// super.mappingOut(instance, params);
	// }

	@Override
	public Map<String, Object> getMappingInValues(ProcessInstance instance)
			throws Exception {
		Map<String, Object> mappingInValues = new HashMap();
		if (getEventSynchronization().getMappingContext() == null)
			return mappingInValues;

		ParameterContext[] params = getEventSynchronization().getMappingContext().getMappingElements();
		HtmlFormContext formData = new HtmlFormContext();
		Object value = null;
		if (params == null)
			return mappingInValues;

		for (ParameterContext param : params) {
			try {

				String targetFieldName = param.getArgument().getText();
				String key = null;
				String firstPart = null;
				boolean resolvePartNeeded = targetFieldName.indexOf('.') > 0;
				if (resolvePartNeeded) {
					firstPart = resolvePartNeeded ? targetFieldName.substring(0,
							targetFieldName.indexOf('.'))
							: targetFieldName;
					key = targetFieldName.substring(targetFieldName.indexOf('.') + 1);
				} else {
					key = targetFieldName;
					firstPart = targetFieldName;
				}

				if (param.getTransformerMapping() != null) {

					Map options = new HashMap();
					options.put(org.uengine.processdesigner.mapper.Transformer.OPTION_KEY_OUTPUT_ARGUMENT,
							param.getTransformerMapping().getLinkedArgumentName());
					options.put(org.uengine.processdesigner.mapper.Transformer.OPTION_KEY_FORM_FIELD_NAME,
							targetFieldName);

					TransformerMapping tm = param.getTransformerMapping();
					Transformer transformer = tm.getTransformer();
					value = transformer.letTransform(instance, options, null);
				} else {
					String srcVariableName = param.getVariable().getName();
					value = instance.getBeanProperty(srcVariableName);
				}

				if (value instanceof HtmlFormContext) {
					formData = (HtmlFormContext) value;
				} else {
					if (value instanceof ProcessVariableValue) {
						ProcessVariableValue pvv = (ProcessVariableValue) value;
						pvv.beforeFirst();
						ArrayList<Serializable> list = new ArrayList<Serializable>();
						do {
							Serializable pvvValue = pvv.getValue();
							if (pvvValue instanceof HtmlFormContext) {
								HtmlFormContext formContext = (HtmlFormContext) pvvValue;
								list.add((Serializable) formContext.getValueMap());
							} else {
								list.add(pvvValue);
							}
						} while (pvv.next());
						formData.setBeanProperty(key, list);
					} else {
						formData.setBeanProperty(key, value);
					}
				}
				Object formValueInMulti = formData.getBeanProperty(firstPart);
				if (formValueInMulti != null) {
					if (formValueInMulti instanceof Map) {
						formData.setValueMap((Map) formValueInMulti);
					}
				}

				mappingInValues.put(firstPart, formData);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		return mappingInValues;
	}

	public void saveWorkItem(ProcessInstance instance, ResultPayload payload) throws Exception {
		onSave(instance, null);
		if (isMappingWhenSave()) {
			// mappingOut(instance, payload);
		}

		super.saveWorkItem(instance, payload);
	}

	public String getParameter(Map parameterMap, String key) {
		String[] paramPair = (String[]) parameterMap.get(key);
		if (paramPair != null && paramPair.length > 0)
			return paramPair[0];
		else
			return null;
	}

	public Map getMappedResult(ProcessInstance instance) throws Exception {

		if (getParentActivity() instanceof AtomicHumanActivity) {
			((AtomicHumanActivity) getParentActivity()).executePreActivities(instance);
		}

		if ("true".equals(GlobalContext
				.getPropertyString("org.uengine.kernel.formactivity.run_select_activities_before_formactivity"))) {
			// TODO run again the before data gathering activities located previously
		}

		boolean isMapping = true;
		Map mappedResult = new HashMap();
		HtmlFormContext formContext = instance == null
				? (HtmlFormContext) (getVariableForHtmlFormContext().getDefaultValue())
				: (HtmlFormContext) (getVariableForHtmlFormContext().get(instance, ""));

		if (formContext == null) {
			return mappedResult;
		}

		if (formContext.getFilePath() != null) {
			isMapping = false;
			if (formContext.getValueMap() == null) {
				formContext.loadValueMap();
			}
			mappedResult.putAll(formContext.getValueMap());
		}

		String status = instance.getStatus(getTracingTag());

		if (Activity.STATUS_READY.equals(status) ||
				Activity.STATUS_RUNNING.equals(status) ||
				Activity.STATUS_TIMEOUT.equals(status)) {

			// MappingContext mappingContext = getMappingContext();
			ParameterContext[] params = getParameters();// getVariableBindings();

			if (params != null && instance != null) {
				// String script = "";
				String objName = null;
				Serializable objValue = null;
				for (int i = 0; i < params.length; i++) {
					ParameterContext param = params[i];

					String targetFormField = param.getArgument().getText();

					targetFormField = targetFormField.replace('.', '@');
					String[] targetFormFieldName = targetFormField.split("@");

					if (getVariableForHtmlFormContext().getName().equals(targetFormFieldName[0])) {
						objName = targetFormFieldName[1];

						if (param.getTransformerMapping() != null) {

							Map options = new HashMap();
							options.put(org.uengine.processdesigner.mapper.Transformer.OPTION_KEY_OUTPUT_ARGUMENT,
									param.getTransformerMapping().getLinkedArgumentName());
							options.put(org.uengine.processdesigner.mapper.Transformer.OPTION_KEY_FORM_FIELD_NAME,
									objName);

							objValue = (Serializable) param.getTransformerMapping().getTransformer()
									.letTransform(instance, options, null);

						} else {

							String sourceProcessVariable = param.getVariable().getName();

							if (sourceProcessVariable.startsWith("["))
								objValue = (Serializable) instance.getBeanProperty(sourceProcessVariable);
							else {
								ProcessVariableValue pvv = instance.getMultiple("", sourceProcessVariable);
								pvv.beforeFirst();
								if (pvv.size() > 1) {
									Object values[] = new String[pvv.size()];
									int j = 0;

									do {
										Object objTmp = pvv.getValue();
										StringBuffer strTmpValue = new StringBuffer();

										if (objTmp != null) {
											if (objTmp.getClass().isArray()) {
												for (String strTmp : (String[]) objTmp) {
													strTmpValue.append(strTmp).append(";");
												}
											} else {
												strTmpValue.append(pvv.getValue().toString());
											}
										}

										values[j++] = strTmpValue.toString();
										// values[j++] = pvv.getValue();
									} while (pvv.next());

									objValue = values;
								} else {

									objValue = pvv.getValue();
								}
							}
						}
						if (!mappedResult.containsKey(objName.toLowerCase()) || isMapping) {
							mappedResult.put(objName.toLowerCase(), objValue);
						}
					}
				}
			}
		}

		return mappedResult;
	}

	// static public Map createParameterMapFromRequest(HttpServletRequest request)
	// throws Exception {
	// return createParameterMapFromRequest(false, request);
	// }

	// static public Map createParameterMapFromRequest(boolean isSimulate,
	// HttpServletRequest request) throws Exception {
	// if (isSimulate && request == null) {
	// return null;
	// }
	// return new HashMap(request.getParameterMap());
	// }

	protected void onSave(ProcessInstance instance, Map parameterMap_) throws Exception {
		boolean isSimulation = false; // instance instanceof SimulatorProcessInstance;
		Map parameterMap = null;

		// try {
		// parameterMap = (Map)
		// instance.getProcessTransactionContext().getProcessManager().getGenericContext()
		// .get("parameterMap");
		// } catch (Exception e) {
		// }

		// if (parameterMap == null) {
		// if (parameterMap_ == null) {
		// parameterMap = createParameterMapFromRequest(isSimulation,
		// (HttpServletRequest)
		// instance.getProcessTransactionContext().getServletRequest());
		// } else {
		// parameterMap = parameterMap_;
		// }
		// }

		HashMap valueMap = new HashMap();
		if (parameterMap != null && parameterMap.size() > 0) {

			String fileSystemDir = FormActivity.FILE_SYSTEM_DIR;
			String lastChar = fileSystemDir.substring(fileSystemDir.length() - 1, fileSystemDir.length());

			if (!"/".equals(lastChar) && !"\\".equals(lastChar)) {
				fileSystemDir += "/";
			}

			String tempDir = "temp" + "/";
			String outPath = null;
			FileContext fc = null;
			Iterator interator = parameterMap.keySet().iterator();
			for (int i = 0; i < parameterMap.size(); i++) {
				String key = (String) interator.next();
				Object valueObj = parameterMap.get(key);
				if (valueObj instanceof String[]) {
					String[] value = (String[]) parameterMap.get(key);
					// File Move
					if (value != null) {
						for (int j = 0; j < value.length; j++) {
							if (value[j].contains("<org.uengine.contexts.FileContext>")
									&& value[j].contains("<path>" + tempDir)) {
								fc = (FileContext) GlobalContext.deserialize(value[j], FileContext.class);
								outPath = fc.getPath().replace(tempDir, "");
								new File(fileSystemDir + fc.getPath()).renameTo(new File(fileSystemDir + outPath));
								fc.setPath(outPath);
								value[j] = GlobalContext.serialize(fc, FileContext.class);
							}
						}
						parameterMap.put(key, value);
					}

					if (value.length > 1) {
						valueMap.put(key.toLowerCase(), value);
					} else if (value.length > 0) {
						valueMap.put(key.toLowerCase(), value[0]);
					}
				} else {
					valueMap.put(key.toLowerCase(), valueObj);
				}
			}
		}

		if (isSimulation) {
			HtmlFormContext newFormCtx = new HtmlFormContext();
			newFormCtx.setValueMap(valueMap);

			getVariableForHtmlFormContext().set(instance, "", newFormCtx);
			return;
		}

		String filePath = FILE_SYSTEM_DIR + UEngineUtil.getCalendarDir();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS", Locale.KOREA);
		String fileName = filePath + "/" + instance.getInstanceId() + "_" + sdf.format(new Date());
		boolean isHtmlSave = "true".equals(GlobalContext.getPropertyString("formactivity.save.html", "false"));

		// Form data save to xml
		// saveFormVariableXML(instance, valueMap, fileName + ".xml");
		// snapshot save to html
		// if (isHtmlSave) {
		// saveFormHTML(instance, valueMap, fileName + ".html");
		// }

	}

	public void saveFormVariableXML(ProcessInstance instance, Map valueMap, String filePath) throws Exception {

		File newFile = new File(filePath);
		File dir = newFile.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		FileOutputStream fos = new FileOutputStream(newFile);
		GlobalContext.serialize(valueMap, fos, HashMap.class);
		fos.close();

		HtmlFormContext formDefInfo = (HtmlFormContext) getVariableForHtmlFormContext().getDefaultValue();
		String[] formDefID = formDefInfo.getFormDefId().split("@");

		HtmlFormContext newFormCtx = new HtmlFormContext();
		newFormCtx.setFilePath(filePath.substring(FILE_SYSTEM_DIR.length()));
		newFormCtx.setFormDefId(formDefID[0] + "@" + formDefID[1]);
		newFormCtx.setValueMap(valueMap);

		if (GlobalContext.logLevelIsDebug && instance != null) {
			instance.addDebugInfo("Form administration url",
					GlobalContext.WEB_CONTEXT_ROOT + "/processmanager/viewFormDefinition.jsp?objectDefinitionId="
							+ formDefID[0] + "&processDefinitionVersionID=" + formDefID[1]);
			instance.addDebugInfo("Form data XML path",
					new File(FILE_SYSTEM_DIR + newFormCtx.getFilePath()).getAbsolutePath());
			instance.addDebugInfo("");
		}

		getVariableForHtmlFormContext().set(instance, "", newFormCtx);
	}

	// private void saveFormHTML(ProcessInstance instance, Map valueMap, String
	// filePath) throws Exception {
	// HtmlFormContext formCtx = (HtmlFormContext)
	// getVariableForHtmlFormContext().get(instance, "");
	// String[] formDefID = formCtx.getFormDefId().split("@");
	// HttpServlet servlet = (HttpServlet)
	// instance.getProcessTransactionContext().getProcessManager()
	// .getGenericContext().get("servlet");
	// HttpServletResponse response = (HttpServletResponse)
	// instance.getProcessTransactionContext()
	// .getServletResponse();
	// ServletRequest request =
	// instance.getProcessTransactionContext().getServletRequest();

	// if (request != null) {
	// request.setAttribute("mappingResult", valueMap);
	// request.setAttribute("instance", instance);
	// request.setAttribute("formActivity", this);
	// request.setAttribute("loggedRoleMapping",
	// instance.getProcessTransactionContext().getProcessManager()
	// .getGenericContext().get(HumanActivity.GENERICCONTEXT_CURR_LOGGED_ROLEMAPPING));
	// request.setAttribute("pm", (new
	// ProcessManagerFactoryBean()).getProcessManagerForReadOnly());

	// final StringWriter sw = new StringWriter();
	// ServletContext servletContext = servlet.getServletContext();

	// boolean isJBoss = "JBOSS".equals(GlobalContext.getPropertyString("was.type",
	// "TOMCAT"));
	// String webRoot = isJBoss ? GlobalContext.WEB_CONTEXT_ROOT : "";
	// // standalone_formDefinition.jsp?formdefinition_url=

	// RequestDispatcher dis = servletContext.getRequestDispatcher(webRoot +
	// "/wih/wihDefaultTemplate/header.jsp");
	// dis.include(request, new HttpServletResponseWrapper(response) {
	// public PrintWriter getWriter() throws IOException {
	// return new PrintWriter(sw);
	// }
	// });

	// String formFileName = formDefID[1];
	// String cachedFormRoot = "/wih/formHandler/cachedForms/";
	// File contextDir = new File(request.getRealPath(cachedFormRoot));
	// FormUtil.copyToContext(contextDir, formFileName);
	// dis = servletContext
	// .getRequestDispatcher(webRoot + "/wih/formHandler/cachedForms/" +
	// formFileName + "_formview.jsp");
	// dis.include(request, new HttpServletResponseWrapper(response) {
	// public PrintWriter getWriter() throws IOException {
	// return new PrintWriter(sw);
	// }
	// });

	// /**************************
	// * Append Tag Div
	// **************************/
	// String Tags = request.getParameter("tags");
	// if (UEngineUtil.isNotEmpty(Tags)) {
	// StringBuffer buff = sw.getBuffer();

	// buff.append("<div id='tags'>");
	// buff.append(Tags.replaceAll(";", ",").substring(0, Tags.length() - 1));
	// buff.append("</div>");
	// }

	// sw.flush();
	// sw.close();

	// File newFile = new File(filePath);
	// File dir = newFile.getParentFile();
	// if (!dir.exists()) {
	// dir.mkdirs();
	// }

	// final OutputStreamWriter osw = new OutputStreamWriter(new
	// FileOutputStream(newFile),
	// GlobalContext.ENCODING);
	// osw.write(sw.toString());
	// osw.close();

	// }
	// }

	// public String getFormDefinitionVersionId(ProcessInstance instance,
	// ProcessManagerRemote pm) throws Exception {

	// HtmlFormContext formContext = instance == null
	// ? (HtmlFormContext) (getVariableForHtmlFormContext().getDefaultValue())
	// : (HtmlFormContext) (getVariableForHtmlFormContext().get(instance, ""));
	// String formDefId = formContext.getFormDefId();

	// return ProcessDefinition.getDefinitionVersionId(pm, formDefId,
	// ProcessDefinition.VERSIONSELECTOPTION_CURRENT_PROD_VER,
	// getProcessDefinition());
	// }

	// public String getFormDefinitionPath(ProcessInstance instance,
	// ProcessManagerRemote pm) throws Exception {
	// String formDefinitionVersionId = getFormDefinitionVersionId(instance, pm);

	// // return
	// //
	// ProcessDefinitionFactory.getInstance(instance.getProcessTransactionContext()).getResourcePath(formDefinitionVersionId);
	// return ProcessDefinitionFactory.DEFINITION_ROOT + formDefinitionVersionId +
	// ".form";
	// }

	/*
	 * public void onSave(ProcessInstance instance, HttpServletRequest request)
	 * throws Exception{
	 * Enumeration enumeration = request.getParameterNames();
	 * HashMap valueMap = new HashMap();
	 * 
	 * for(;enumeration.hasMoreElements();){
	 * String key = (String)enumeration.nextElement();
	 * String value = request.getParameter(key);
	 * 
	 * valueMap.put(key, value);
	 * }
	 * SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS",
	 * Locale.KOREA);
	 * 
	 * String filePath = GlobalContext.getPropertyString(
	 * "server.definition.path","./uengine/definition/")
	 * + UEngineUtil.getCalendarDir();
	 * File dirToCreate = new File(filePath);
	 * dirToCreate.mkdirs();
	 * 
	 * String fileName = sdf.format(new Date()) + ".xml";
	 * File newFile = new File(filePath+"/"+fileName);
	 * FileOutputStream fos = new FileOutputStream(newFile);
	 * GlobalContext.serialize(valueMap, fos, HashMap.class);
	 * fos.close();
	 * 
	 * HtmlFormContext formDefInfo =
	 * (HtmlFormContext)getVariableForHtmlFormContext().getDefaultValue();
	 * formDefInfo.setFilePath(newFile.getAbsolutePath());
	 * formDefInfo.setFormDefId(formDefInfo.getFormDefId());
	 * 
	 * getVariableForHtmlFormContext().set(instance, "", formDefInfo);
	 * }
	 */

	public String getTool(ProcessInstance instance) {
		HtmlFormContext formContext;
		try {
			formContext = null;
			for (ProcessVariable v : instance.getProcessDefinition().getProcessVariables()) {
				if (v.getDefaultValue() instanceof HtmlFormContext) {
					HtmlFormContext defaultValue = (HtmlFormContext) v.getDefaultValue();
					if (defaultValue.getFormDefId() != null
							&& v.name.equals(getVariableForHtmlFormContext().getName())) {
						formContext = defaultValue;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (formContext != null) {
			return "formHandler:" + formContext.getFormDefId();
		}
		return "formHandler";
	}

	protected void executeActivity(ProcessInstance instance) throws Exception {
		// if (instance instanceof SimulatorProcessInstance) {
		// onReceive(instance, null);
		// return;
		// }
		mappingIn(instance);

		super.executeActivity(instance);
	}

	protected void mappingIn(ProcessInstance instance) throws Exception {
		if (instance instanceof DefaultProcessInstance) {
			Map variables = ((DefaultProcessInstance) instance).getVariables();
			Map test = variables;
		}
	}

	public ValidationContext validate(Map options) {
		// TODO Auto-generated method stub
		ValidationContext superVC = super.validate(options);

		if (getVariableForHtmlFormContext() == null)
			superVC.add(getActivityLabel() + "Variable For HTMLContext should not be null");

		return superVC;
	}

}
