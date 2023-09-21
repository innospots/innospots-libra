/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.base.model.field;

import lombok.Getter;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字段值类型
 *
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Getter
public enum FieldValueType {

    /**
     *
     */
    FIELD_CODE("fld", "refer", "field code identity", String.class, "VARCHAR",
            null, (p, v) -> v),

    STRING("string", "string", "character string", String.class, "VARCHAR",
            null, (p, v) -> v),

    INTEGER("int", "number", "integer", Integer.class, "INT",
            Pattern.compile("^[-]{0,1}([1-9]\\d{1,8})"), (p, v) -> {
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return Integer.valueOf(v.toString());
    }),

    BOOLEAN("boolean", "number", "boolean true or false", Boolean.class, "INT",
            null, (p, v) -> {
        if (v == null) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(v.toString());
    }),

    LONG("long", "number", "long", Long.class, "BIGINT",
            Pattern.compile("^[-]{0,1}([1-9]\\d{9,19})[lL]{0,1}"), (p, v) -> {
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        return Long.valueOf(v.toString());
    }),

    DATE("date", "string", "yyyy-MM-dd", LocalDate.class, "DATE",
            Pattern.compile("^([1-9]\\d{3})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[1-2][0-9]|3[0-1])"), (p, v) -> {
        Matcher m = p.matcher(v.toString());
        LocalDate localDate = null;
        while (m.find()) {
            int y = Integer.parseInt(m.group(1));
            int mh = Integer.parseInt(m.group(2));
            int d = Integer.parseInt(m.group(3));
            localDate = LocalDate.of(y, mh, d);
        }
        return localDate;
    }),

    TIME("time", "string", "HH:mm", LocalTime.class, "DATETIME",
            Pattern.compile("^(20|21|22|23|[0-1]\\d):([0-5]\\d)(?:\\:([0-5]\\d)){0,1}(?:\\.(\\d{3})){0,1}"), (p, v) -> {
        Matcher m = p.matcher(v.toString());
        LocalTime localTime = null;
        while (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            int second = 0;
            if (m.groupCount() >= 4) {
                second = Integer.parseInt(m.group(3));
            }
            int nanos = 0;
            if (m.groupCount() >= 5) {
                nanos = Integer.parseInt(m.group(4));
            }
            localTime = LocalTime.of(hour, min, second, nanos);
        }
        return localTime;
    }),
    DATE_TIME("datetime", "string", "yyyy-MM-dd HH:mm:ss", LocalDateTime.class, "DATETIME", Pattern.compile("^([1-9]\\d{3})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[1-2][0-9]|3[0-1])[ |T](20|21|22|23|[0-1]\\d):([0-5]\\d)(?:\\:([0-5]\\d)){0,1}(?:\\.(\\d{3})){0,1}"), (p, v) -> {
        Matcher m = p.matcher(v.toString());
        LocalDateTime localDateTime = null;
        while (m.find()) {
            int year = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int day = Integer.parseInt(m.group(3));
            int hour = Integer.parseInt(m.group(4));
            int min = Integer.parseInt(m.group(5));
            int second = 0;
            int nanos = 0;
            if (m.groupCount() >= 7) {
                second = Integer.parseInt(m.group(6));
            }
            if (m.groupCount() >= 8) {
                nanos = Integer.parseInt(m.group(7));
            }
            localDateTime = LocalDateTime.of(year, month, day, hour, min, second, nanos);
        }
        return localDateTime;
    }),
    DOUBLE("double", "number", "00.0",
            Double.class, "DOUBLE", Pattern.compile("^[-]{0,1}(\\d+)[.]{0,1}(\\d+)[dD]{0,1}"), (p, v) -> {
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        return Double.valueOf(v.toString());
    }),
    CURRENCY("currency", "number", "bigDecimal format",
            BigDecimal.class, "DECIMAL", Pattern.compile("^[-]{0,1}([￥$€￡￠])(\\d+)[.]{0,1}(\\d+)"), (p, v) -> {
        BigDecimal decimal = null;
        String vv = v.toString();
        Matcher m = p.matcher(vv);
        long unscaledVal = 1;
        if (vv.startsWith("-")) {
            unscaledVal = -1;
        }
        while (m.find()) {
            unscaledVal = Long.parseLong(m.group(2)) * unscaledVal;
            int ps = 0;
            if (m.groupCount() >= 4) {
                ps = Integer.parseInt(m.group(3));
            }
            decimal = BigDecimal.valueOf(unscaledVal, ps);
        }
        return decimal;
    }),
    TIMESTAMP("timestamp", "number", "millisecond",
            Long.class, "TIMESTAMP", null, (p, v) -> {
        return Long.valueOf(v.toString());
    }),
    YEAR_MONTH("ym", "number", "yyyyMM",
            Integer.class, "INT", Pattern.compile("^([1-9]\\d{3})(0[1-9]|1[0-2])"), (p, v) -> {
        LocalDate date = null;
        Matcher m = p.matcher(v.toString());
        if (m.find()) {
            int year = Integer.parseInt(m.group(1));
            int mm = Integer.parseInt(m.group(2));
            date = LocalDate.of(year, mm, 1);
        }
        return date;
    }),
    YEAR_MONTH_DATE("ymd", "number", "yyyyMMdd",
            Integer.class, "INT", Pattern.compile("^([1-9]\\d{3})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])"), (p, v) -> {
        LocalDate date = null;
        Matcher m = p.matcher(v.toString());
        if (m.find()) {
            int year = Integer.parseInt(m.group(1));
            int mm = Integer.parseInt(m.group(2));
            int day = Integer.parseInt(m.group(3));
            date = LocalDate.of(year, mm, day);
        }
        return date;
    }),
    MONTH("mm", "number", "MM",
            Integer.class, "INT", null, (p, v) -> {
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return Integer.parseInt(v.toString());
    }),
    MONTH_DATE("md", "number", "MMdd",
            Integer.class, "INT", null, (p, v) -> {
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return Integer.parseInt(v.toString());
    }),
    DAY_OF_MONTH("dm", "number", "dd",
            Integer.class, "INT", null, (p, v) -> {
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return Integer.parseInt(v.toString());
    }),
    MAP("map", "json", "",
            Map.class, "TEXT", null, (p, v) -> v),
    LIST("list", "list", "",
            List.class, "TEXT", null, (p, v) -> v),
    COLLECTION("seq", "list", "list,set, array",
            Collection.class, "TEXT", null, (p, v) -> v),
    NUMBER("number", "number", "int,double float,long",
            Number.class, "DOUBLE", null, (p, v) -> v),
    DECIMAL("decimal", "number", "decimal type",
            BigDecimal.class, "DECIMAL", null, (p, v) -> {
        try {
            DecimalFormat df = new DecimalFormat();
            Number vv = df.parse(v.toString());
            return new BigDecimal(vv.toString());
        } catch (ParseException e) {
            return BigDecimal.ZERO;
        }
    }),
    SET("set", "list", "",
            Set.class, "TEXT", null, (p, v) -> v),
    OBJECT("object", "json", "",
            Object.class, "Blob", null, (p, v) -> v),
    FILE("object", "json", "",
            File.class, "Blob", null, (p, v) -> v),

    NUMERIC("numeric", "number", "00.0", Double.class, "DOUBLE", null, (p, v) -> v),
    FRAGMENT("string", "string", "character string", String.class, "VARCHAR", null, (p, v) -> v);


    private final String description;

    private final String grouping;

    private final String brief;

    private final Class<?> clazz;

    private final String dbFieldType;

    private final Pattern pattern;

    private final BiFunction<Pattern, Object, Object> converter;


    FieldValueType(String brief, String grouping, String description,
                   Class<?> clazz, String dbFieldType, Pattern pattern, BiFunction<Pattern, Object, Object> converter) {
        this.brief = brief;
        this.grouping = grouping;
        this.description = description;
        this.clazz = clazz;
        this.dbFieldType = dbFieldType;
        this.pattern = pattern;
        this.converter = converter;
    }

    public static FieldValueType getTypeByBrief(String brief) {
        return Arrays.stream(FieldValueType.values()).filter(f -> f.brief.equals(brief)).findFirst().orElse(null);
    }

    public boolean isNumber() {
        return this == DAY_OF_MONTH ||
                this == MONTH ||
                this == MONTH_DATE ||
                this == YEAR_MONTH ||
                this == YEAR_MONTH_DATE ||
                this == DOUBLE ||
                this == LONG ||
                this == CURRENCY ||
                this == INTEGER ||
                this == BOOLEAN ||
                this == TIMESTAMP;
    }

    public boolean isMap() {
        return this.grouping.equals(MAP.grouping);
    }

    public static FieldValueType convertTypeByValue(Object value) {
        if (value instanceof Integer) {
            return FieldValueType.INTEGER;
        } else if (value instanceof Boolean) {
            return FieldValueType.BOOLEAN;
        } else if (value instanceof Double) {
            return FieldValueType.DOUBLE;
        } else if (isDate(value)) {
            return FieldValueType.DATE;
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
        } else if (isDateTime(value)) {
            return FieldValueType.DATE_TIME;
        } else if (isDate(value)) {
            return FieldValueType.DATE;
        } else if (isTime(value)) {
            return FieldValueType.TIME;
//        } else if (isTimestamp(value)) {
//            return FieldValueType.TIMESTAMP;
        } else if (value instanceof Long) {
            return FieldValueType.LONG;
        } else if (value instanceof String) {
            return FieldValueType.STRING;
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
        } else {
            return FieldValueType.OBJECT;
        }
    }

    public Object normalize(Object value) {
        return converter.apply(pattern, value);
    }

    public Object normalize(Object value, String format) {
        Object val = normalize(value);
        if (val instanceof TemporalAccessor) {
            return DateTimeFormatter.ofPattern(format).format((TemporalAccessor) val);
        }

        val = String.format(format,val);
        if(this.isNumber()){
            return normalize(val);
        }

        return val;
    }


    private static boolean isDate(Object value) {
        return patternMatch(DATE.pattern, value);
    }

    private static boolean isTime(Object value) {
        return patternMatch(TIME.pattern, value);
    }

    private static boolean isDateTime(Object value) {
        return patternMatch(DATE_TIME.pattern, value);
    }

    private static boolean patternMatch(Pattern pattern, Object value) {
        try {
            return pattern.matcher(String.valueOf(value)).matches();
        } catch (Exception e) {
            return false;
        }
    }

}
