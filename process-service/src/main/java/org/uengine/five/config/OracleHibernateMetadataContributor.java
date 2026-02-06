package org.uengine.five.config;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * H2/Oracle 공통: JPQL의 regexp_like를 REGEXP_LIKE SQL 함수로 변환.
 * Oracle에서 REGEXP_LIKE(...)=1 이 ORA-00907(누락된 우괄호)를 일으키므로,
 * (REGEXP_LIKE(?1, ?2)) 형태로 괄호로 감싸서 등록.
 */
public class OracleHibernateMetadataContributor implements MetadataBuilderContributor {

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySqlFunction(
                "regexp_like",
                new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "(REGEXP_LIKE(?1, ?2))")
        );
    }
}
