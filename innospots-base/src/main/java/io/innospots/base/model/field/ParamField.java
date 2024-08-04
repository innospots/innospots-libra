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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Setter
@Getter
public class ParamField {

    @NotBlank(message = "Name cannot be blank")
    protected String name;

    @NotBlank(message = "Code cannot be blank")
    protected String code;

    @NotNull(message = "ValueType cannot be empty")
    protected FieldValueType valueType;

    protected Object value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected List<ParamField> subFields;

    /**
     * field comment information
     */
    protected String comment;

    /**
     * field source key
     */
    protected String parentCode;

    /**
     * value format
     */
    protected String format;


    public ParamField() {
    }

    public ParamField(@NotBlank(message = "Name cannot be blank") String name, @NotBlank(message = "Code cannot be blank") String code, @NotNull(message = "ValueType cannot be empty") FieldValueType valueType) {
        this.name = name;
        this.code = code;
        this.valueType = valueType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", valueType=").append(valueType);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParamField)) {
            return false;
        }
        ParamField that = (ParamField) o;
        return Objects.equals(name, that.name) && Objects.equals(code, that.code) && valueType == that.valueType && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code, valueType, value);
    }
}
