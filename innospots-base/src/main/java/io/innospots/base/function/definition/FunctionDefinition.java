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

package io.innospots.base.function.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.model.field.FieldValueType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * function definition
 *
 * @author Smars
 * @date 2021/8/22
 */
@Schema(title = "function definition")
@Setter
@Getter
public class FunctionDefinition {


    private Integer functionId;

    @NotBlank
    private String cateType;

    @NotBlank
    private String cateName;

    @NotBlank
    @Max(16)
    private String name;

    @Max(128)
    private String description;

    @NotBlank
    private String returnType;

    private String paramTypes;

    @JsonIgnore
    private List<FieldValueType> paramFieldTypes = new ArrayList<>();

    @JsonIgnore
    private List<Boolean> musts = new ArrayList<>();

    @NotBlank
    @Max(2048)
    private String expression;

    @NotBlank
    private String functionType;


    public FunctionDefinitionEntity toEntity() {
        FunctionDefinitionEntity functionDefinitionEntity = new FunctionDefinitionEntity();
        functionDefinitionEntity.setFunctionType(this.functionType);
        functionDefinitionEntity.setCateName(cateName);
        functionDefinitionEntity.setCateType(cateType);
        functionDefinitionEntity.setDescription(description);
        functionDefinitionEntity.setExpression(expression);
        functionDefinitionEntity.setName(name);
        functionDefinitionEntity.setReturnType(returnType);
        functionDefinitionEntity.setParamTypes(paramTypes);
        functionDefinitionEntity.setFunctionId(functionId);
        return functionDefinitionEntity;
    }

    public void addParamType(FieldValueType valueType, boolean must) {
        paramFieldTypes.add(valueType);
        musts.add(must);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("cateType='").append(cateType).append('\'');
        sb.append(", cateName='").append(cateName).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", returnType=").append(returnType);
        sb.append(", paramsType=").append(paramFieldTypes);
        sb.append(", musts=").append(musts);
//        sb.append(", aviatorExp='").append(aviatorExp).append('\'');
//        sb.append(", javaExp='").append(javaExp).append('\'');
//        sb.append(", sqlExp='").append(sqlExp).append('\'');
//        sb.append(", flinkExp='").append(flinkExp).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
