package com.defaultcompany.organization;

import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.RoleMapping;
import org.uengine.util.UEngineUtil;

import java.lang.reflect.Method;
import java.util.Map;

public class DefaultCompanyRoleMapping extends RoleMapping{
	
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	final static String EXT_PROP_KEY_NateOnMessengerId = "EXT_PROP_KEY_NATEON_ID";

	@Override
	protected String doFill() throws Exception {
		if (GlobalContext.isDesignTime()) return null;

		String endpoint = getEndpoint();
		if (!UEngineUtil.isNotEmpty(endpoint)) return null;

		try {
			Map<String, Object> user = getUserByIdViaIamServices(endpoint);
			if (user == null) return endpoint;

			String username = asString(user.get("username"));

			return username;
		} catch (Exception ignore) {
			return endpoint;
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getUserByIdViaIamServices(String userId) {
		try {
			Class<?> iamServicesClass = Class.forName("org.uengine.five.service.IAMServices");
			Method getDefault = iamServicesClass.getMethod("getDefault");
			Object iamService = getDefault.invoke(null);
			if (iamService == null) return null;

			Method getUserById = iamService.getClass().getMethod("getUserById", String.class);
			Object result = getUserById.invoke(iamService, userId);
			return (Map<String, Object>) result;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
}