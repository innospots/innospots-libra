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

package io.innospots.libra.kernel.module.schema.converter;

import io.innospots.base.connector.schema.SchemaField;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.data.dataset.Variable;
import io.innospots.base.json.JSONUtils;
import io.innospots.connector.schema.entity.SchemaFieldEntity;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * @author Alfred
 * @date 2022-01-01
 */
@Mapper
public interface SchemaFieldBeanConverter extends BaseBeanConverter {

    SchemaFieldBeanConverter INSTANCE = Mappers.getMapper(SchemaFieldBeanConverter.class);

    @Mapping(defaultValue = "false", target = "pkey")
    SchemaFieldEntity modelToEntity(SchemaField schemaField);

    List<SchemaFieldEntity> modelsToEntities(List<SchemaField> schemaFields);

    SchemaField entityToModel(SchemaFieldEntity schemaFieldEntity);

    List<SchemaField> entitiesToModels(List<SchemaFieldEntity> dataFieldEntities);

    default Variable entityToVariable(SchemaFieldEntity fieldEntity) {
        Variable variable = new Variable();
        variable.setName(fieldEntity.getCode());
        variable.setDefaultValue(fieldEntity.getDefaultValue());

        Map<String, String> config = JSONUtils.toStrMap(fieldEntity.getConfig());
        if (MapUtils.isNotEmpty(config)) {
            variable.setExpression(Boolean.valueOf(String.valueOf(config.get("expression"))));
            variable.setType(config.get("type"));
            variable.setValueType(config.get("valueType"));
        }
        return variable;
    }

    List<Variable> entitiesToVariables(List<SchemaFieldEntity> entities);

}