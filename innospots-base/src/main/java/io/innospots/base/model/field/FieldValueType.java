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
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
        if (v instanceof LocalDate) {
            return v;
        }
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
        if (v instanceof LocalTime) {
            return v;
        }
        Matcher m = p.matcher(v.toString());
        LocalTime localTime = null;
        while (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            int second = 0;
            if (m.groupCount() >= 4) {
                String s = m.group(3);
                if (s != null) {
                    second = Integer.parseInt(s);
                }

            }
            int nanos = 0;
            if (m.groupCount() >= 5) {
                String s = m.group(4);
                if (s != null) {
                    nanos = Integer.parseInt(s);
                }
            }
            localTime = LocalTime.of(hour, min, second, nanos);
        }
        return localTime;
    }),

    DATE_TIME("datetime", "string", "yyyy-MM-dd HH:mm:ss", LocalDateTime.class, "DATETIME", Pattern.compile("^([1-9]\\d{3})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[1-2][0-9]|3[0-1])[ |T](20|21|22|23|[0-1]\\d):([0-5]\\d)(?:\\:([0-5]\\d)){0,1}(?:\\.(\\d{3})){0,1}"), (p, v) -> {
        if (v instanceof LocalDateTime) {
            return v;
        }
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
                String s = m.group(6);
                if (s != null) {
                    second = Integer.parseInt(s);
                }
            }
            if (m.groupCount() >= 8) {
                String s = m.group(7);
                if (s != null) {
                    nanos = Integer.parseInt(s);
                }
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
        if (v instanceof BigDecimal) {
            return v;
        }
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
                String s = m.group(3);
                if (s != null) {
                    ps = Integer.parseInt(s);
                }
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
        if (v instanceof LocalDate) {
            return v;
        }
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
        if (v instanceof LocalDate) {
            return v;
        }
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
        if (v instanceof BigDecimal) {
            return v;
        }
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

    public boolean isTime() {
        return this == DATE_TIME ||
                this == TIME ||
                this == YEAR_MONTH_DATE ||
                this == YEAR_MONTH ||
                this == DATE;
    }

    public boolean isMap() {
        return this.grouping.equals(MAP.grouping);
    }


    public Object normalize(Object value) {
        return converter.apply(pattern, value);
    }

    public Object normalize(Object value, String format) {
        Object val = normalize(value);
        if (val instanceof TemporalAccessor) {
            return DateTimeFormatter.ofPattern(format).format((TemporalAccessor) val);
        }

        if (format != null) {
            val = String.format(format, val);
            if (this.isNumber()) {
                return normalize(val);
            }
        }
        return val;
    }

    public Object plusTime(Object value, long amountToAdd, TemporalUnit unit) {
        value = normalize(value);
        if (value instanceof LocalDate) {
            value = ((LocalDate) value).plus(amountToAdd, unit);
        } else if (value instanceof LocalDateTime) {
            value = ((LocalDateTime) value).plus(amountToAdd, unit);
        } else if (value instanceof LocalTime) {
            value = ((LocalTime) value).plus(amountToAdd, unit);
        }
        if (value instanceof TemporalAccessor) {
            return DateTimeFormatter.ofPattern(this.description).format((TemporalAccessor) value);
        }
        return value;
    }

    public <E extends TemporalAccessor> E plusTime(Object value, long amountToAdd, TemporalUnit unit, Class<E> clazz) {
        value = normalize(value);
        if (value instanceof LocalDate) {
            value = ((LocalDate) value).plus(amountToAdd, unit);
        } else if (value instanceof LocalDateTime) {
            value = ((LocalDateTime) value).plus(amountToAdd, unit);
        } else if (value instanceof LocalTime) {
            value = ((LocalTime) value).plus(amountToAdd, unit);
        }
        return (E) value;
    }

}
