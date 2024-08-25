package io.innospots.base.model.field;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static io.innospots.base.model.field.FieldValueType.*;

public class FieldValueTypeConverter {


    public static FieldValueType getTypeByBrief(String brief) {
        return Arrays.stream(FieldValueType.values()).filter(f -> f.getBrief().equals(brief)).findFirst().orElse(null);
    }


    public static FieldValueType convertTypeByValue(Object value) {
        if (value instanceof Integer) {
            return FieldValueType.INTEGER;
        } else if (value instanceof Boolean) {
            return FieldValueType.BOOLEAN;
        } else if (value instanceof Double) {
            return FieldValueType.DOUBLE;
        } else if (isDate(value)) {
            return DATE;
        } else if (isDateTime(value)) {
            return FieldValueType.DATE_TIME;
//        } else if (value instanceof Timestamp) {
//            return FieldValueType.TIMESTAMP;
        } else if (value instanceof Long) {
            return FieldValueType.LONG;
        } else {
            // TODO 判断字符串类型的boolean值
            return FieldValueType.STRING;
        }
    }

    public static FieldValueType convertJavaTypeByValue(Object value) {
        if (value instanceof Integer) {
            return FieldValueType.INTEGER;
        } else if (value instanceof Boolean) {
            return FieldValueType.BOOLEAN;
        } else if (value instanceof Double) {
            return FieldValueType.DOUBLE;
        } else if (value instanceof BigDecimal) {
            return FieldValueType.CURRENCY;
        } else if (value instanceof Long) {
            return FieldValueType.LONG;
        } else if (value instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) value;
            if (m.containsKey("resourceId") && m.containsKey("uri")) {
                return FieldValueType.FILE;
            }
            return FieldValueType.MAP;
        } else if (value instanceof List) {
            return FieldValueType.LIST;
        } else if (value instanceof Set) {
            return FieldValueType.SET;
        } else if (isDateTime(value)) {
            return FieldValueType.DATE_TIME;
        } else if (isDate(value)) {
            return DATE;
        } else if (isTime(value)) {
            return TIME;
        } else if (value instanceof String) {
            return FieldValueType.STRING;
        } else {
            return FieldValueType.OBJECT;
        }
    }



    private static boolean patternMatch(Pattern pattern, Object value) {
        try {
            return pattern.matcher(String.valueOf(value)).matches();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isDate(Object value) {
        return patternMatch(DATE.getPattern(), value);
    }

    private static boolean isTime(Object value) {
        return patternMatch(TIME.getPattern(), value);
    }

    private static boolean isDateTime(Object value) {
        return patternMatch(DATE_TIME.getPattern(), value);
    }
}
