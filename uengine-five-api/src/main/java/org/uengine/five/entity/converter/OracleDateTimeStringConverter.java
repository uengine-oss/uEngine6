package org.uengine.five.entity.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Oracle VARCHAR2(14) 일시 컬럼과 Java Date를 상호 변환한다.
 * 포맷은 yyyyMMddHHmmss 를 사용한다.
 */
@Converter(autoApply = false)
public class OracleDateTimeStringConverter implements AttributeConverter<Date, String> {

    private static final ThreadLocal<SimpleDateFormat> FORMATTER =
            ThreadLocal.withInitial(() -> {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                format.setLenient(false);
                return format;
            });

    @Override
    public String convertToDatabaseColumn(Date attribute) {
        if (attribute == null) {
            return null;
        }
        return FORMATTER.get().format(attribute);
    }

    @Override
    public Date convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        String normalized = dbData.trim();
        if (normalized.isEmpty() || "00000000000000".equals(normalized)) {
            return null;
        }

        try {
            return FORMATTER.get().parse(normalized);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid Oracle datetime value: " + normalized, e);
        }
    }
}
