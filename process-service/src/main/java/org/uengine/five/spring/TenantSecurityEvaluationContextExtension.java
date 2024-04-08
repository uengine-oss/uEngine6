package org.uengine.five.spring;

import org.springframework.data.spel.spi.EvaluationContextExtension;

public class TenantSecurityEvaluationContextExtension implements EvaluationContextExtension {

  @Override
  public String getExtensionId() {
    return "security";
  }

  @Override
  public TenantSecurityExpressionRoot getRootObject() {
    return new TenantSecurityExpressionRoot(null);
  }
}