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
import io.innospots.base.connector.schema.SchemaRegistry;
import io.innospots.base.connector.schema.SchemaRegistryType;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.data.dataset.Dataset;
import io.innospots.base.data.dataset.Variable;
import io.innospots.base.json.JSONUtils;
import io.innospots.libra.kernel.module.schema.entity.SchemaRegistryEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Alfred
 * @date 2022-01-01
 */
@Mapper
public interface SchemaRegistryConverter extends BaseBeanConverter<SchemaRegistry, SchemaRegistryEntity> {

    SchemaRegistryConverter INSTANCE = Mappers.getMapper(SchemaRegistryConverter.class);


    default Dataset schemaRegistryToDataset(SchemaRegistry schemaRegistry) {
        Dataset dataset = new Dataset();
        dataset.setId(schemaRegistry.getRegistryId());
        dataset.setName(schemaRegistry.getName());
        dataset.setCredentialKey(schemaRegistry.getCredentialKey());
        dataset.setCategoryId(schemaRegistry.getCategoryId());
        dataset.setScript(String.valueOf(schemaRegistry.scriptValue("script")));
        dataset.setModel(String.valueOf(schemaRegistry.configValue("model")));

        if (CollectionUtils.isNotEmpty(schemaRegistry.getSchemaFields())) {
            List<Variable> variables = new ArrayList<>();
            for (SchemaField schemaField : schemaRegistry.getSchemaFields()) {
                Variable variable = new Variable();
                variable.setViewId(schemaField.getRegistryId() != null ? schemaField.getRegistryId().toString() : null);
                variable.setName(schemaField.getName());
                variable.setCredentialKey(String.valueOf(schemaRegistry.getCredentialKey()));
                Map<String, Object> config = JSONUtils.toMap(schemaField.getConfig());
                if (MapUtils.isNotEmpty(config)) {
                    variable.setType(String.valueOf(config.get("type")));
                    variable.setValueType(String.valueOf(config.get("valueType")));
                    variable.setFormat(String.valueOf(config.get("format")));
                    variable.setEncrypt(config.get("encrypt") == null ? null : Boolean.valueOf(String.valueOf(config.get("encrypt"))));
                    variable.setLabel(String.valueOf(config.get("label")));
                    variable.setDefaultValue(String.valueOf(config.get("defaultValue")));
                    variable.setExpression(config.get("expression") == null ? null : Boolean.valueOf(String.valueOf(config.get("expression"))));
                }
                variables.add(variable);
            }
            dataset.setVariables(variables);
        }
        dataset.setCreatedBy(schemaRegistry.getCreatedBy());
        dataset.setCreatedTime(schemaRegistry.getCreatedTime());
        dataset.setUpdatedBy(schemaRegistry.getUpdatedBy());
        dataset.setUpdatedTime(schemaRegistry.getUpdatedTime());
        return dataset;
    }

    default List<Dataset> schemaRegistriesToDatasets(List<SchemaRegistry> schemaRegistries) {
        List<Dataset> datasets = new ArrayList<>();
        for (SchemaRegistry schemaRegistry : schemaRegistries) {
            datasets.add(this.schemaRegistryToDataset(schemaRegistry));
        }
        return datasets;
    }

    default SchemaRegistry datasetToSchemaRegistry(Dataset dataset) {
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        schemaRegistry.setRegistryType(SchemaRegistryType.DATASET);
        schemaRegistry.setRegistryId(dataset.getId());
        schemaRegistry.setName(dataset.getName());
        schemaRegistry.setCredentialKey(dataset.getCredentialKey());
        schemaRegistry.setCategoryId(dataset.getCategoryId());
        schemaRegistry.addScript("script", dataset.getScript());
        schemaRegistry.addConfig("model", dataset.getModel());

        if (CollectionUtils.isNotEmpty(dataset.getVariables())) {
            List<SchemaField> schemaFields = new ArrayList<>();
            for (Variable variable : dataset.getVariables()) {
                SchemaField schemaField = new SchemaField();
                schemaField.setRegistryId(StringUtils.isEmpty(variable.getViewId()) ? null : Integer.valueOf(variable.getViewId()));
                schemaField.setName(variable.getName());
                Map<String, Object> config = new HashMap<>();
                config.put("type", variable.getType());
                config.put("valueType", variable.getValueType());
                config.put("format", variable.getFormat());
                config.put("encrypt", variable.getEncrypt());
                config.put("label", variable.getLabel());
                config.put("defaultValue", variable.getDefaultValue());
                config.put("expression", variable.getExpression());
                schemaField.setConfig(JSONUtils.toJsonString(config));

                schemaFields.add(schemaField);
            }
            schemaRegistry.setSchemaFields(schemaFields);
        }

        schemaRegistry.setCreatedBy(dataset.getCreatedBy());
        schemaRegistry.setCreatedTime(dataset.getCreatedTime());
        schemaRegistry.setUpdatedBy(dataset.getUpdatedBy());
        schemaRegistry.setUpdatedTime(dataset.getUpdatedTime());
        return schemaRegistry;
    }
}