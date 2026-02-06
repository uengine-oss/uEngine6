package org.uengine.five.config;

import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * Oracle12c + REGEXP_LIKE 함수 등록.
 * (REGEXP_LIKE(?1, ?2)) 형태로 등록해 (regexp_like(...))=true 시 ORA-00907 방지.
 */
public class Oracle12cDialectWithRegexp extends Oracle12cDialect {

    public Oracle12cDialectWithRegexp() {
        super();
        registerFunction("regexp_like", new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "(REGEXP_LIKE(?1, ?2))"));
        // UDF for JPQL function('REGEXP_LIKE_YN', ...) = 1 (DDL: oracle/scripts/init.sql)
        registerFunction("REGEXP_LIKE_YN", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "REGEXP_LIKE_YN(?1, ?2)"));
        registerFunction("regexp_like_yn", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "REGEXP_LIKE_YN(?1, ?2)"));
    }
}
