package org.uengine.five.audit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 설정(uengine.audit)에 따라 사용할 AuditSink 빈 구성.
 * sink=none(기본)이면 JPA/테이블 없이 동작하여 코어 엔진에서 감사 테이블이 필수가 아님.
 */
@Configuration
public class AuditConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "auditSink")
    public AuditSink auditSink(AuditProperties auditProperties,
                               ObjectProvider<JpaAuditSink> jpaAuditSinkProvider,
                               FileAuditSink fileAuditSink,
                               CompositeAuditSink compositeAuditSink) {
        String sinkType = auditProperties != null ? auditProperties.getSink() : AuditProperties.SINK_NONE;
        if (sinkType == null) sinkType = AuditProperties.SINK_NONE;

        switch (sinkType.toLowerCase()) {
            case AuditProperties.SINK_NONE:
                return new NoOpAuditSink();
            case AuditProperties.SINK_FILE:
                return fileAuditSink;
            case AuditProperties.SINK_COMPOSITE:
                if (auditProperties.getSinkNames() != null) {
                    for (String name : auditProperties.getSinkNames()) {
                        String n = name.trim();
                        if (AuditProperties.SINK_JPA.equalsIgnoreCase(n)) {
                            JpaAuditSink jpa = jpaAuditSinkProvider.getIfAvailable();
                            if (jpa != null) compositeAuditSink.addSink(jpa);
                        } else if (AuditProperties.SINK_FILE.equalsIgnoreCase(n)) {
                            compositeAuditSink.addSink(fileAuditSink);
                        }
                    }
                }
                return compositeAuditSink;
            case AuditProperties.SINK_JPA:
            default:
                JpaAuditSink jpa = jpaAuditSinkProvider.getIfAvailable();
                return jpa != null ? jpa : new NoOpAuditSink();
        }
    }
}
