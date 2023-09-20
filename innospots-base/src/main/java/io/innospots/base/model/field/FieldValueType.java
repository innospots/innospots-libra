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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
    FIELD_CODE("fld", "refer", "field code identity", String.class, "VARCHAR",null),
    STRING("string", "string", "character string", String.class, "VARCHAR",null),
    INTEGER("int", "number", "integer", Integer.class, "INT",new String[]{"^[-]{0,1}([1-9]\\d{1,8})"}),
    BOOLEAN("boolean", "number", "boolean true or false", Boolean.class, "INT",null),
    LONG("long", "number", "long", Long.class, "BIGINT",new String[]{"^[-]{0,1}([1-9]\\d{9,19})[lL]{0,1}"}),
    DATE("date", "string", "yyyy-MM-dd", LocalDate.class, "DATE",new String[]{"^([1-9]\\d{3})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[1-2][0-9]|3[0-1])"}),
    TIME("time", "string", "HH:mm", LocalTime.class, "DATETIME",new String[]{"^(20|21|22|23|[0-1]\\d):([0-5]\\d)(?:\\:([0-5]\\d)){0,1}(?:\\.(\\d{3})){0,1}"}),
    DATE_TIME("datetime", "string", "yyyy-MM-dd HH:mm:ss", LocalDateTime.class, "DATETIME",new String[]{"^([1-9]\\d{3})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[1-2][0-9]|3[0-1])[ |T](20|21|22|23|[0-1]\\d):([0-5]\\d)(?:\\:([0-5]\\d)){0,1}(?:\\.(\\d{3})){0,1}"}),
    DOUBLE("double", "number", "00.0", Double.class, "DOUBLE",new String[]{"^[-]{0,1}(\\d+)[.]{0,1}(\\d+)[dD]{0,1}"}),
    CURRENCY("currency", "number", "bigDecimal format", BigDecimal.class, "DECIMAL",new String[]{"^[-]{0,1}([￥$€￡￠])(\\d+)[.]{0,1}(\\d+)"}),
    TIMESTAMP("timestamp", "number", "millisecond", Long.class, "TIMESTAMP",null),
    YEAR_MONTH("ym", "number", "yyyyMM", Integer.class, "INT",new String[]{"^([1-9]\\d{3})(0[1-9]|1[0-2])"}),
    YEAR_MONTH_DATE("ymd", "number", "yyyyMMdd", Integer.class, "INT",new String[]{"^([1-9]\\d{3})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])"}),
    MONTH("mm", "number", "MM", Integer.class, "INT",null),
    MONTH_DATE("md", "number", "MMdd", Integer.class, "INT",null),
    DAY_OF_MONTH("dm", "number", "dd", Integer.class, "INT",null),
    MAP("map", "json", "", Map.class, "TEXT",null),
    LIST("list", "list", "", List.class, "TEXT",null),
    COLLECTION("seq", "list", "list,set, array", Collection.class, "TEXT",null),
    NUMBER("number", "number", "int,double float,long", Number.class, "DOUBLE",null),
    DECIMAL("decimal", "number", "decimal type", BigDecimal.class, "DECIMAL",null),
    SET("set", "list", "", Set.class, "TEXT",null),
    OBJECT("object", "json", "", Object.class, "Blob",null),
    FILE("object", "json", "", File.class, "Blob",null),

    // TODO dataview variable use
    NUMERIC("numeric", "number", "00.0", Double.class, "DOUBLE",null), // TODO
    FRAGMENT("string", "string", "character string", String.class, "VARCHAR",null);


    private String description;

    private String grouping;

    private String brief;

    private Class<?> clazz;

    private String dbFieldType;

    private String[] patterns;


    FieldValueType(String brief, String grouping, String description, Class<?> clazz, String dbFieldType,String[] patterns) {
        this.brief = brief;
        this.grouping = grouping;
        this.description = description;
        this.clazz = clazz;
        this.dbFieldType = dbFieldType;
        this.patterns = patterns;
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
            Map<String,Object> m = (Map<String, Object>) value;
            if(m.containsKey("resourceId") && m.containsKey("uri")){
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

    private static boolean isDate(Object value) {
        return isDateString("^[1-9]\\d{3}([-/.])(0[1-9]|1[0-2])([-/.])(0[1-9]|[1-2][0-9]|3[0-1])", value);
    }

    private static boolean isTime(Object value) {
        return isDateString("^(20|21|22|23|[0-1]\\d):[0-5]\\d(:[0-5]\\d){0,1}", value);
    }

    private static boolean isDateTime(Object value) {
        return isDateString("^([1-9]\\d{3})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[1-2][0-9]|3[0-1])[ |T](20|21|22|23|[0-1]\\d):([0-5]\\d)(?:\\:([0-5]\\d)){0,1}(?:\\.(\\d{3})){0,1}", value);
    }

    private static boolean isDateString(String format, Object value) {
        try {
            return String.valueOf(value).matches(format);
        } catch (Exception e) {
            return false;
        }
    }


}
