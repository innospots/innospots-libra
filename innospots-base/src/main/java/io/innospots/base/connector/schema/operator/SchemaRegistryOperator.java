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

package io.innospots.base.connector.schema.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.connector.schema.model.SchemaRegistryType;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.StringConverter;
import io.innospots.base.connector.schema.converter.SchemaRegistryConverter;
import io.innospots.base.connector.schema.dao.SchemaRegistryDao;
import io.innospots.base.connector.schema.entity.SchemaRegistryEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2021-02-10
 */
public class SchemaRegistryOperator extends ServiceImpl<SchemaRegistryDao, SchemaRegistryEntity> {

    private final SchemaFieldOperator schemaFieldOperator;

    public SchemaRegistryOperator(SchemaFieldOperator schemaFieldOperator) {
        this.schemaFieldOperator = schemaFieldOperator;
    }

    public SchemaRegistryEntity createSchemaRegistry(SchemaRegistryEntity registryEntity) {
        long codeCount = 0;
        if (registryEntity.getCode() == null) {
            do {
                String code = StringConverter.randomKey(4);
                registryEntity.setCode(code);
                QueryWrapper<SchemaRegistryEntity> qw = new QueryWrapper<>();
                qw.lambda().eq(SchemaRegistryEntity::getCode, code);
                codeCount = this.count(qw);
            } while (codeCount > 0);
        } else {
            QueryWrapper<SchemaRegistryEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(SchemaRegistryEntity::getCode, registryEntity.getCode());

            if (this.count(qw) > 0) {
                throw ResourceException.buildExistException(this.getClass(), "schema registry code has exist, credential:" + registryEntity.getCredentialKey());
            }
        }
        if (registryEntity.getRegistryId() == null) {
            do {
                String registryId = StringConverter.randomKey(8);
                registryEntity.setRegistryId(registryId);
                QueryWrapper<SchemaRegistryEntity> qw = new QueryWrapper<>();
                qw.lambda().eq(SchemaRegistryEntity::getRegistryId, registryId);
                codeCount = this.count(qw);
            } while (codeCount > 0);
        } else {
            QueryWrapper<SchemaRegistryEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(SchemaRegistryEntity::getRegistryId, registryEntity.getRegistryId());

            if (this.count(qw) > 0) {
                throw ResourceException.buildExistException(this.getClass(), "schema registry id has exist, credential:" + registryEntity.getRegistryId());
            }
        }

        this.save(registryEntity);
        return registryEntity;
    }

    public SchemaRegistry createSchemaRegistry(SchemaRegistry schemaRegistry) {
        SchemaRegistryEntity entity = SchemaRegistryConverter.INSTANCE.modelToEntity(schemaRegistry);
        createSchemaRegistry(entity);
        schemaRegistry.setRegistryId(entity.getRegistryId());
        schemaRegistry.setCode(entity.getCode());
        saveSchemeField(schemaRegistry);
        return this.getSchemaRegistryById(entity.getRegistryId(), true);
    }

    public SchemaRegistry updateSchemaRegistry(SchemaRegistry schemaRegistry) {
        if (this.checkNameExist(schemaRegistry.getName(), schemaRegistry.getRegistryId())) {
            throw ResourceException.buildExistException(this.getClass(), "name exist: " + schemaRegistry.getName());
        }
        SchemaRegistryEntity entity = SchemaRegistryConverter.INSTANCE.modelToEntity(schemaRegistry);
        super.updateById(entity);
        saveSchemeField(schemaRegistry);
        return this.getSchemaRegistryById(entity.getRegistryId(), true);
    }

    public Boolean deleteSchemaRegistry(String registryId) {
        schemaFieldOperator.deleteByRegistryId(registryId);
        return super.removeById(registryId);
    }

    public Boolean deleteByCredentialKey(String credentialKey) {
        List<SchemaRegistryEntity> registryEntities = super.list(new QueryWrapper<SchemaRegistryEntity>().lambda().eq(SchemaRegistryEntity::getCredentialKey, credentialKey));
        if (CollectionUtils.isEmpty(registryEntities)) {
            return true;
        }
        Set<String> ids = registryEntities.stream().map(SchemaRegistryEntity::getRegistryId).collect(Collectors.toSet());
        boolean delete = super.remove(new QueryWrapper<SchemaRegistryEntity>().lambda().eq(SchemaRegistryEntity::getCredentialKey, credentialKey));
        schemaFieldOperator.deleteByRegistryIds(ids);
        return delete;
    }

    public SchemaRegistry getSchemaRegistryById(String registryId) {
        return getSchemaRegistryById(registryId, false);
    }

    public SchemaRegistry getSchemaRegistryById(String registryId, boolean includeField) {
        SchemaRegistryEntity entity = super.getById(registryId);
        SchemaRegistry schemaRegistry = SchemaRegistryConverter.INSTANCE.entityToModel(entity);
        if (includeField) {
            schemaRegistry.setSchemaFields(this.schemaFieldOperator.listByRegistryId(schemaRegistry.getRegistryId()));
        }
        return schemaRegistry;
    }

    public SchemaRegistry getSchemaRegistryByCode(String code) {
        return getSchemaRegistryByCode(code, false);
    }

    public SchemaRegistry getSchemaRegistryByCode(String code, boolean includeField) {
        QueryWrapper<SchemaRegistryEntity> query = new QueryWrapper<>();
        query.lambda().eq(SchemaRegistryEntity::getCode, code);
        SchemaRegistryEntity entity = this.getOne(query);
        SchemaRegistry schemaRegistry = SchemaRegistryConverter.INSTANCE.entityToModel(entity);
        if (includeField) {
            schemaRegistry.setSchemaFields(this.schemaFieldOperator.listByRegistryId(schemaRegistry.getRegistryId()));
        }
        return schemaRegistry;
    }

    public List<SchemaRegistry> listAppSchemaRegistries(String appKey) {
        QueryWrapper<SchemaRegistryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SchemaRegistryEntity::getAppKey, appKey);
        List<SchemaRegistryEntity> entities = super.list(queryWrapper);
        return CollectionUtils.isEmpty(entities) ? Collections.emptyList() : SchemaRegistryConverter.INSTANCE.entitiesToModels(entities);
    }

    public List<SchemaRegistry> listSchemaRegistries(String credentialKey) {
        QueryWrapper<SchemaRegistryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SchemaRegistryEntity::getCredentialKey, credentialKey);
        List<SchemaRegistryEntity> entities = super.list(queryWrapper);
        return CollectionUtils.isEmpty(entities) ? Collections.emptyList() : SchemaRegistryConverter.INSTANCE.entitiesToModels(entities);
    }

    public List<SchemaRegistry> listSchemaRegistries(String queryCode, String sortField, Integer categoryId, SchemaRegistryType registryType) {
        QueryWrapper<SchemaRegistryEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(queryCode)) {
            queryWrapper.lambda().like(SchemaRegistryEntity::getCode, queryCode)
                    .or().like(SchemaRegistryEntity::getName, queryCode);
        }
        if (StringUtils.isNotEmpty(sortField)) {
            queryWrapper.orderByDesc(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sortField));
        }
        if (categoryId != null) {
            queryWrapper.lambda().eq(SchemaRegistryEntity::getCategoryId, categoryId);
        }
        if (registryType != null) {
            queryWrapper.lambda().eq(SchemaRegistryEntity::getRegistryType, registryType);
        }

        List<SchemaRegistryEntity> entities = super.list(queryWrapper);
        return CollectionUtils.isEmpty(entities) ? Collections.emptyList() : SchemaRegistryConverter.INSTANCE.entitiesToModels(entities);
    }

    public PageBody<SchemaRegistry> pageSchemaRegistries(String name, String sortField,
                                                         String appKey,String scope,
                                                         String credentialKey,
                                                         Integer categoryId, SchemaRegistryType registryType,
                                                         Integer page, Integer size) {
        QueryWrapper<SchemaRegistryEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            queryWrapper.lambda().or().like(SchemaRegistryEntity::getName, name);
        }
        if (StringUtils.isNotEmpty(sortField)) {
            queryWrapper.orderByDesc(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sortField));
        }
        if (StringUtils.isNotEmpty(appKey)) {
            queryWrapper.lambda().eq(SchemaRegistryEntity::getAppKey, appKey);
        }
        if (StringUtils.isNotEmpty(scope)) {
            queryWrapper.lambda().eq(SchemaRegistryEntity::getScope, scope);
        }
        if (StringUtils.isNotEmpty(credentialKey)) {
            queryWrapper.lambda().eq(SchemaRegistryEntity::getCredentialKey, credentialKey);
        }
        if (categoryId != null) {
            queryWrapper.lambda().eq(SchemaRegistryEntity::getCategoryId, categoryId);
        }
        if (registryType != null) {
            queryWrapper.lambda().eq(SchemaRegistryEntity::getRegistryType, registryType);
        }

        Page<SchemaRegistryEntity> entities = super.page(PageDTO.of(page, size), queryWrapper);
        List<SchemaRegistry> schemaRegistries = SchemaRegistryConverter.INSTANCE.entitiesToModels(entities.getRecords());

        PageBody<SchemaRegistry> pageBody = new PageBody<>();
        pageBody.setList(schemaRegistries);
        pageBody.setPageSize(entities.getSize());
        pageBody.setCurrent(entities.getCurrent());
        pageBody.setTotal(entities.getTotal());

        return pageBody;
    }

    private void saveSchemeField(SchemaRegistry schemaRegistry) {
        if (CollectionUtils.isNotEmpty(schemaRegistry.getSchemaFields())) {
            for (SchemaField schemaField : schemaRegistry.getSchemaFields()) {
                schemaField.setRegistryId(schemaRegistry.getRegistryId());
                schemaField.setRegistryCode(schemaRegistry.getCode());
                schemaField.setComment(StringUtils.isBlank(schemaField.getComment()) ? schemaField.getName() : schemaField.getComment());
            }
            List<SchemaField> schemaFields = schemaFieldOperator.createOrUpdateSchemaFieldBatch(schemaRegistry.getRegistryId(), schemaRegistry.getSchemaFields());
            schemaRegistry.setSchemaFields(schemaFields);
        }
    }

    public boolean checkNameExist(String name, String registryId) {
        QueryWrapper<SchemaRegistryEntity> qw = new QueryWrapper<SchemaRegistryEntity>();
        qw.lambda().eq(SchemaRegistryEntity::getName, name);
        if(registryId!=null){
            qw.lambda().ne(SchemaRegistryEntity::getRegistryId,registryId);
        }
        return super.count(qw) > 0;

    }


    public List<SchemaRegistry> listByRegistryIds(Set<Integer> keys) {
        List<SchemaRegistryEntity> entities = super.listByIds(keys);
        return SchemaRegistryConverter.INSTANCE.entitiesToModels(entities);
    }
}
