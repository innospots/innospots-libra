/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.base.connector.schema.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.model.field.BaseField;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author Raydian
 * @date 2020/12/19
 */
@Getter
@Setter
@Schema(title = "schema field")
public class SchemaField extends BaseField {

    @NotNull(message = "registryId cannot be empty")
    private String registryId;

    @NotBlank(message = "registryCode cannot be blank")
    private String registryCode;

    /**
     * field type
     */
    private FieldScope fieldScope;

    /**
     * is primary key
     */
    private Boolean pkey = false;

    private String config;

    /**
     * default value
     */
    private String defaultValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected List<SchemaField> subFields;

    public SchemaField fill(String code, String name,
                            FieldValueType valueType){
        return this.fill(code, name, valueType,null);
    }

    public SchemaField fill(String code, String name, FieldValueType valueType,FieldScope fieldScope){
        this.code = code;
        this.name = name;
        this.valueType = valueType;
        this.fieldScope =fieldScope;
        return this;
    }


    public static SchemaField convert(ParamField paramField){
        SchemaField schemaField = new SchemaField();
        schemaField.setFieldScope(paramField.getFieldScope());
        schemaField.setValueType(paramField.getValueType());
        schemaField.setCode(paramField.getCode());
        schemaField.setName(paramField.getName());
        schemaField.setComment(paramField.getComment());
        if(paramField.getSubFields()!=null){
            schemaField.setSubFields(paramField.getSubFields()
                    .stream().map(SchemaField::convert).toList());
        }

        schemaField.setDefaultValue(paramField.getValue()!=null?paramField.getValue().toString():null);
        return schemaField;
    }
}
