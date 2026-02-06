package org.uengine.five.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Oracle NUMBER(1) 컬럼과 Java boolean 매핑.
 * 0/1만 사용하면 JPQL·네이티브 쿼리 결과 매핑 시 ORA-00932(BINARY/TIMESTAMP 불일치) 방지.
 */
@Converter(autoApply = false)
public class OracleBooleanConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean value) {
        if (value == null) return null;
        return value ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return false;
        return dbData != 0;
    }
}
