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

package io.innospots.libra.kernel.module.schema.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.connector.schema.SchemaField;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.libra.kernel.module.schema.converter.SchemaFieldBeanConverter;
import io.innospots.libra.kernel.module.schema.dao.SchemaFieldDao;
import io.innospots.libra.kernel.module.schema.entity.SchemaFieldEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2021-02-10
 */
@Component
public class SchemaFieldOperator extends ServiceImpl<SchemaFieldDao, SchemaFieldEntity> {

    @Transactional(rollbackFor = Exception.class)
    public List<SchemaField> createOrUpdateSchemaFieldBatch(Integer registryId, List<SchemaField> schemaFields) {
        if (CollectionUtils.isEmpty(schemaFields)) {
            return Collections.emptyList();
        }

        Set<String> codes = schemaFields.stream().map(SchemaField::getCode).collect(Collectors.toSet());
        if (codes.size() != schemaFields.size()) {
            throw ValidatorException.buildMissingException(this.getClass(), "the code of fields have duplicated");
        }

        Set<Integer> removeIds = new HashSet<>();
        QueryWrapper<SchemaFieldEntity> query = new QueryWrapper<>();
        query.lambda().eq(SchemaFieldEntity::getRegistryId, registryId);
        List<SchemaFieldEntity> entities = this.list(query);
        List<SchemaField> addFields = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(entities)) {
            Map<String, SchemaFieldEntity> entityMap = entities.stream().collect(Collectors.toMap(SchemaFieldEntity::getCode, Function.identity()));
            for (SchemaField upNewField : schemaFields) {
                upNewField.setRegistryId(registryId);
                SchemaFieldEntity entity = entityMap.get(upNewField.getCode());
                if (entity != null) {
                    //exist in db
                    if (upNewField.getFieldId() == null) {
                        // not set fieldId and then update the field
                        upNewField.setFieldId(entity.getFieldId());
                    }
                    entityMap.remove(upNewField.getCode());
                } else {
                    if (upNewField.getFieldId() != null) {
                        upNewField.setFieldId(null);
                    }
                }
                addFields.add(upNewField);
            }//end for
            if (!entityMap.isEmpty()) {
                for (Map.Entry<String, SchemaFieldEntity> entry : entityMap.entrySet()) {
                    removeIds.add(entry.getValue().getFieldId());
                }
            }
        } else {
            addFields.addAll(schemaFields);
        }

        if (!removeIds.isEmpty()) {
            this.removeByIds(removeIds);
        }

        entities = SchemaFieldBeanConverter.INSTANCE.modelsToEntities(addFields);

        super.saveOrUpdateBatch(entities);
        for (int i = 0; i < schemaFields.size(); i++) {
            Integer fieldId = entities.get(i).getFieldId();
            schemaFields.get(i).setFieldId(fieldId);
        }
        return schemaFields;
    }

    public List<SchemaField> listByRegistryId(Integer registryId) {
        QueryWrapper<SchemaFieldEntity> query = new QueryWrapper<>();
        query.lambda().eq(SchemaFieldEntity::getRegistryId, registryId);
        List<SchemaFieldEntity> entities = super.list(query);
        return SchemaFieldBeanConverter.INSTANCE.entitiesToModels(entities);
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemaField createSchemaField(SchemaField schemaField) {
        if (this.checkCodeExist(schemaField.getName(), schemaField.getRegistryId())) {
            throw ResourceException.buildExistException(this.getClass(), schemaField.getName());
        }
        SchemaFieldEntity entity = SchemaFieldBeanConverter.INSTANCE.modelToEntity(schemaField);
        super.save(entity);
        schemaField.setFieldId(entity.getFieldId());
        return schemaField;
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemaField updateSchemaField(SchemaField schemaField) {
        if (this.checkCodeExistAndExcludeOriginalCode(schemaField.getName(), schemaField.getRegistryId(), schemaField.getFieldId())) {
            throw ResourceException.buildExistException(this.getClass(), schemaField.getName());
        }
        SchemaFieldEntity entity = SchemaFieldBeanConverter.INSTANCE.modelToEntity(schemaField);
        super.updateById(entity);
        return schemaField;
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemaField upsertSchemaField(SchemaField schemaField) {
        if (schemaField.getFieldId() == null) {
            return this.createSchemaField(schemaField);
        } else {
            return this.updateSchemaField(schemaField);
        }
    }

    public List<SchemaField> parseToSchemaFields(String json) {
        // escape processing
        json = json.replaceAll("\\\\n|\\\\", "");
        Map<String, Object> jsonMap;

        try {
            jsonMap = JSONUtils.toMap(json, String.class, Object.class);
            List<SchemaField> schemaFields = new ArrayList<>();
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                SchemaField schemaField = new SchemaField();
                schemaField.setName(entry.getKey());
                schemaField.setCode(entry.getKey());
                schemaField.setComment(entry.getKey());
                schemaField.setValueType(FieldValueType.convertTypeByValue(entry.getValue()));
                schemaField.setFieldScope(FieldScope.COLUMN);
                schemaFields.add(schemaField);
            }
            return schemaFields;
        } catch (Exception e) {
            throw ValidatorException.buildInvalidException(this.getClass());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByRegistryId(Integer registryId) {
        return super.remove(new QueryWrapper<SchemaFieldEntity>().lambda().eq(SchemaFieldEntity::getRegistryId, registryId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByRegistryIds(Set<Integer> registryIds) {
        return super.remove(new QueryWrapper<SchemaFieldEntity>().lambda().in(SchemaFieldEntity::getRegistryId, registryIds));
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSchemaField(Integer fieldId) {
        return super.removeById(fieldId);
    }

    private boolean checkCodeExist(String code, Integer registryId) {
        return super.count(new QueryWrapper<SchemaFieldEntity>().lambda()
                .eq(SchemaFieldEntity::getCode, code)
                .eq(SchemaFieldEntity::getRegistryId, registryId)
        ) > 0;
    }

    private boolean checkCodeExistAndExcludeOriginalCode(String code, Integer registryId, Integer fieldId) {
        return super.count(new QueryWrapper<SchemaFieldEntity>().lambda()
                .eq(SchemaFieldEntity::getCode, code)
                .eq(SchemaFieldEntity::getRegistryId, registryId)
                .ne(SchemaFieldEntity::getFieldId, fieldId)) > 0;
    }
}
