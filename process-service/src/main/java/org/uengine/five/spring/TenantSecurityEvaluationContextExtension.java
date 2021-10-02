package org.uengine.five.spring;

import org.springframework.data.repository.query.spi.EvaluationContextExtensionSupport;
import org.springframework.security.access.expression.SecurityExpressionRoot;

public class TenantSecurityEvaluationContextExtension extends EvaluationContextExtensionSupport {

    @Override
    public String getExtensionId() {
      return "security";
    }
  
    @Override
    public TenantSecurityExpressionRoot getRootObject() {
      return new TenantSecurityExpressionRoot(null);
    }
}