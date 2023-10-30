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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.connector.schema.SchemaField;
import io.innospots.base.connector.schema.SchemaRegistry;
import io.innospots.base.connector.schema.SchemaRegistryType;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.StringConverter;
import io.innospots.libra.kernel.module.schema.converter.SchemaRegistryBeanConverter;
import io.innospots.libra.kernel.module.schema.dao.SchemaRegistryDao;
import io.innospots.libra.kernel.module.schema.entity.SchemaRegistryEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

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
        this.save(registryEntity);
        return registryEntity;
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemaRegistry createSchemaRegistry(SchemaRegistry schemaRegistry) {
        SchemaRegistryEntity entity = SchemaRegistryBeanConverter.INSTANCE.modelToEntity(schemaRegistry);
        createSchemaRegistry(entity);
        schemaRegistry.setRegistryId(entity.getRegistryId());
        schemaRegistry.setCode(entity.getCode());
        saveSchemeField(schemaRegistry);
        return this.getSchemaRegistryById(entity.getRegistryId(), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemaRegistry updateSchemaRegistry(SchemaRegistry schemaRegistry) {
        if (this.checkNameExistAndExcludeOriginalName(schemaRegistry.getName(), schemaRegistry.getRegistryId())) {
            throw ResourceException.buildExistException(this.getClass(), "name exist: " + schemaRegistry.getName());
        }
        SchemaRegistryEntity entity = SchemaRegistryBeanConverter.INSTANCE.modelToEntity(schemaRegistry);
        super.updateById(entity);
        saveSchemeField(schemaRegistry);
        return this.getSchemaRegistryById(entity.getRegistryId(), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSchemaRegistry(Integer registryId) {
        schemaFieldOperator.deleteByRegistryId(registryId);
        return super.removeById(registryId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByCredentialKey(String credentialKey) {
        List<SchemaRegistryEntity> registryEntities = super.list(new QueryWrapper<SchemaRegistryEntity>().lambda().eq(SchemaRegistryEntity::getCredentialKey, credentialKey));
        if (CollectionUtils.isEmpty(registryEntities)) {
            return true;
        }
        Set<Integer> ids = registryEntities.stream().map(SchemaRegistryEntity::getRegistryId).collect(Collectors.toSet());
        boolean delete = super.remove(new QueryWrapper<SchemaRegistryEntity>().lambda().eq(SchemaRegistryEntity::getCredentialKey, credentialKey));
        schemaFieldOperator.deleteByRegistryIds(ids);
        return delete;
    }

    public SchemaRegistry getSchemaRegistryById(Integer registryId) {
        return getSchemaRegistryById(registryId, false);
    }

    public SchemaRegistry getSchemaRegistryById(Integer registryId, boolean includeField) {
        SchemaRegistryEntity entity = super.getById(registryId);
        SchemaRegistry schemaRegistry = SchemaRegistryBeanConverter.INSTANCE.entityToModel(entity);
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
        SchemaRegistry schemaRegistry = SchemaRegistryBeanConverter.INSTANCE.entityToModel(entity);
        if (includeField) {
            schemaRegistry.setSchemaFields(this.schemaFieldOperator.listByRegistryId(schemaRegistry.getRegistryId()));
        }
        return schemaRegistry;
    }

    public List<SchemaRegistry> listSchemaRegistries(String credentialKey) {
        QueryWrapper<SchemaRegistryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SchemaRegistryEntity::getCredentialKey, credentialKey);
        List<SchemaRegistryEntity> entities = super.list(queryWrapper);
        return CollectionUtils.isEmpty(entities) ? Collections.emptyList() : SchemaRegistryBeanConverter.INSTANCE.entitiesToModels(entities);
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
        return CollectionUtils.isEmpty(entities) ? Collections.emptyList() : SchemaRegistryBeanConverter.INSTANCE.entitiesToModels(entities);
    }

    public PageBody<SchemaRegistry> pageSchemaRegistries(String queryCode, String sortField, Integer categoryId, SchemaRegistryType registryType, Integer page, Integer size) {
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

        Page<SchemaRegistryEntity> entities = super.page(PageDTO.of(page, size), queryWrapper);
        List<SchemaRegistry> schemaRegistries = SchemaRegistryBeanConverter.INSTANCE.entitiesToModels(entities.getRecords());

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

    public boolean checkNameExistAndExcludeOriginalName(String name, Integer registryId) {
        return super.count(new QueryWrapper<SchemaRegistryEntity>().lambda()
                .eq(SchemaRegistryEntity::getName, name)
                .ne(SchemaRegistryEntity::getRegistryId, registryId)) > 0;
    }

    public boolean checkNameExist(String name, String credentialKey) {
        return super.count(new QueryWrapper<SchemaRegistryEntity>().lambda()
                .eq(SchemaRegistryEntity::getName, name)
                .ne(SchemaRegistryEntity::getCredentialKey, credentialKey)) > 0;
    }

    public List<SchemaRegistry> listByRegistryIds(Set<String> keys) {
        List<SchemaRegistryEntity> entities = super.listByIds(keys);
        return SchemaRegistryBeanConverter.INSTANCE.entitiesToModels(entities);
    }
}
