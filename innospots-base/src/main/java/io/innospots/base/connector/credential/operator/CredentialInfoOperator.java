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

package io.innospots.base.connector.credential.operator;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.credential.model.CredentialInfo;
import io.innospots.base.connector.credential.converter.CredentialInfoConverter;
import io.innospots.base.connector.credential.dao.CredentialInfoDao;
import io.innospots.base.connector.credential.dao.CredentialTypeDao;
import io.innospots.base.connector.credential.entity.CredentialInfoEntity;
import io.innospots.base.connector.credential.entity.CredentialTypeEntity;
import io.innospots.base.connector.credential.model.SimpleCredentialInfo;
import io.innospots.base.connector.meta.ConnectionMinderSchema;
import io.innospots.base.connector.meta.ConnectionMinderSchemaLoader;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.crypto.IEncryptor;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.base.enums.ConnectType;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2023/1/19
 */
@Slf4j
public class CredentialInfoOperator extends ServiceImpl<CredentialInfoDao, CredentialInfoEntity> {

    private final CredentialTypeDao credentialTypeDao;

    private final IEncryptor encryptor;

    public CredentialInfoOperator(CredentialTypeDao credentialTypeDao, IEncryptor encryptor) {
        this.credentialTypeDao = credentialTypeDao;
        this.encryptor = encryptor;
    }

    public CredentialInfo getCredential(String credentialKey) {
        CredentialInfoEntity entity = super.getById(credentialKey);
        if (entity == null) {
            return null;
        }
        CredentialTypeEntity credentialTypeEntity = findCredentialType(entity.getCredentialTypeCode());
        CredentialInfo model = CredentialInfoConverter.INSTANCE.entityToModel(entity);
        if (credentialTypeEntity != null) {
            model.setConnectorName(credentialTypeEntity.getConnectorName());
        }
        return model;
    }

    public CredentialInfo createCredential(CredentialInfo credential) {
        if (this.checkExist(credential.getName(), null)) {
            throw ResourceException.buildExistException(this.getClass(), credential.getName());
        }
        long codeCount = 0;

        do {
            String key = StringConverter.randomKey(8);
            credential.setCredentialKey(key);
            QueryWrapper<CredentialInfoEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(CredentialInfoEntity::getCredentialKey, key);
            codeCount = this.count(qw);
        } while (codeCount > 0);
        fillMinderCredentialInfo(credential);
        CredentialInfoEntity entity = CredentialInfoConverter.INSTANCE.modelToEntity(credential);
        super.save(entity);
        return this.getCredential(entity.getCredentialKey());
    }

    public CredentialInfo updateCredential(CredentialInfo credentialInfo) {
        if (this.checkExist(credentialInfo.getName(), credentialInfo.getCredentialKey())) {
            throw ResourceException.buildExistException(this.getClass(), credentialInfo.getName());
        }
        fillMinderCredentialInfo(credentialInfo);
        CredentialInfoEntity entity = CredentialInfoConverter.INSTANCE.modelToEntity(credentialInfo);
        super.updateById(entity);
        return this.getCredential(entity.getCredentialKey());
    }

    private void fillMinderCredentialInfo(CredentialInfo credentialInfo) {
        CredentialTypeEntity credentialTypeEntity = this.findCredentialType(credentialInfo.getCredentialTypeCode());
        if (credentialTypeEntity == null) {
            return;
        }
        credentialInfo = decryptFormValues(credentialInfo);
        IDataConnectionMinder connectionMinder = DataConnectionMinderManager.newMinderInstance(credentialTypeEntity.getConnectorName(), credentialTypeEntity.getAuthOption());
        if (connectionMinder == null) {
            log.warn("credential type not found, credentialKey:{}", credentialInfo.getCredentialKey());
            return;
        }
        connectionMinder.fillCredentialInfo(credentialInfo);
        encryptFormValues(credentialInfo);
    }

    public Boolean deleteCredential(String credentialKey) {
        return super.removeById(credentialKey);
    }

    public List<CredentialInfo> listCredentialInfos(Set<String> credentialKeys) {
        List<CredentialInfoEntity> entities = this.listByIds(credentialKeys);
        return CredentialInfoConverter.INSTANCE.entitiesToModels(entities);
    }

    /**
     * list credentials
     *
     * @param credentialTypeCode
     * @return
     */
    public List<SimpleCredentialInfo> listSimpleCredentials(String credentialTypeCode, ConnectType connectType) {
        QueryWrapper<CredentialInfoEntity> query = new QueryWrapper<>();
        query.lambda().orderByDesc(CredentialInfoEntity::getCreatedTime);
        if (StringUtils.isNotEmpty(credentialTypeCode)) {
            query.lambda().eq(CredentialInfoEntity::getCredentialTypeCode, credentialTypeCode);
        }
        Map<String, CredentialTypeEntity> types = null;
        if (connectType != null) {
            List<ConnectionMinderSchema> minderSchemas = ConnectionMinderSchemaLoader.connectionMinderSchemas(connectType);
            QueryWrapper<CredentialTypeEntity> typeQueryWrapper = new QueryWrapper<>();
            typeQueryWrapper.lambda().in(CredentialTypeEntity::getConnectorName, minderSchemas.stream().map(ConnectionMinderSchema::getName).collect(Collectors.toSet()));
            List<CredentialTypeEntity> typeEntities = credentialTypeDao.selectList(typeQueryWrapper);
            query.lambda().in(CredentialInfoEntity::getCredentialTypeCode, typeEntities.stream().map(CredentialTypeEntity::getTypeCode).collect(Collectors.toSet()));
            types = typeEntities.stream().collect(Collectors.toMap(CredentialTypeEntity::getTypeCode, Function.identity()));
        }

        List<CredentialInfoEntity> entities = this.list(query);
        if (types == null && CollectionUtils.isNotEmpty(entities)) {
            Set<String> typeCodes = entities.stream().map(CredentialInfoEntity::getCredentialTypeCode).collect(Collectors.toSet());
            types = findCredentialTypes(typeCodes);
        }

        List<SimpleCredentialInfo> simpleAppCredentials = new ArrayList<>();

        for (CredentialInfoEntity entity : entities) {
            CredentialTypeEntity credentialTypeEntity = types.get(entity.getCredentialTypeCode());
            SimpleCredentialInfo simpleAppCredential = CredentialInfoConverter.INSTANCE.entityToSimpleModel(entity, credentialTypeEntity);
            simpleAppCredentials.add(simpleAppCredential);
        }

        return simpleAppCredentials;
    }


    public PageBody<SimpleCredentialInfo> pageCredentials(FormQuery formQuery) {
        QueryWrapper<CredentialInfoEntity> query = new QueryWrapper<>();

        if (StringUtils.isNotBlank(formQuery.getSort())) {
            query.orderByDesc(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, formQuery.getSort()));
        } else {
            query.lambda().orderByDesc(CredentialInfoEntity::getCreatedTime);
        }


        if (StringUtils.isNotBlank(formQuery.getQueryInput())) {
            query.lambda().like(CredentialInfoEntity::getName, formQuery.getQueryInput());
        }
        if (MapUtils.isNotEmpty(formQuery.getParamMap())) {
            String credentialTypeCode = formQuery.getParamMap().get("credentialTypeCode");
            if (credentialTypeCode != null) {
                query.lambda().eq(CredentialInfoEntity::getCredentialTypeCode, credentialTypeCode);
            }
        }

        IPage<CredentialInfoEntity> entityPage = super.page(PageDTO.of(formQuery.getPage(), formQuery.getSize()), query);
        List<SimpleCredentialInfo> credentialInfos = new ArrayList<>();
        Set<String> typeCodes = entityPage.getRecords().stream().map(CredentialInfoEntity::getCredentialTypeCode).collect(Collectors.toSet());

        Map<String, CredentialTypeEntity> types = null;
        if (CollectionUtils.isNotEmpty(typeCodes)) {
            types = findCredentialTypes(typeCodes);
        }
        for (CredentialInfoEntity entity : entityPage.getRecords()) {
            CredentialTypeEntity credentialTypeEntity = types == null ? null : types.get(entity.getCredentialTypeCode());
            SimpleCredentialInfo simpleCredentialInfo = CredentialInfoConverter.INSTANCE.entityToSimpleModel(entity, credentialTypeEntity);
            credentialInfos.add(simpleCredentialInfo);
        }

        PageBody<SimpleCredentialInfo> pageBody = new PageBody<>();
        pageBody.setList(credentialInfos);
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setTotal(entityPage.getTotal());
        return pageBody;

    }


    public boolean checkExist(String name, String credentialKey) {
        QueryWrapper<CredentialInfoEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(CredentialInfoEntity::getName, name);
        if (credentialKey != null) {
            qw.lambda().ne(CredentialInfoEntity::getCredentialKey, credentialKey);
        }
        return super.count(qw) > 0;
    }


    private Map<String, CredentialTypeEntity> findCredentialTypes(Set<String> typeCode) {
        List<CredentialTypeEntity> typeEntities = credentialTypeDao.selectBatchIds(typeCode);
        if (CollectionUtils.isEmpty(typeEntities)) {
            return Collections.emptyMap();
        }
        return typeEntities.stream().collect(Collectors.toMap(CredentialTypeEntity::getTypeCode, Function.identity()));
    }

    private CredentialTypeEntity findCredentialType(String typeCode) {
        return credentialTypeDao.selectById(typeCode);
    }


    public CredentialInfo encryptFormValues(CredentialInfo credentialInfo) {
        if (MapUtils.isEmpty(credentialInfo.getFormValues())) {
            return credentialInfo;
        }
        String jsonStr = JSONUtils.toJsonString(credentialInfo.getFormValues());
        credentialInfo.setEncryptFormValues(encryptor.encode(jsonStr));

        return credentialInfo;
    }

    public CredentialInfo decryptFormValues(CredentialInfo credentialInfo) {
        if (credentialInfo == null) {
            return null;
        }
        try {
            if (StringUtils.isBlank(credentialInfo.getEncryptFormValues())) {
                return credentialInfo;
            }

            String formValuesStr = encryptor.decode(credentialInfo.getEncryptFormValues());
            credentialInfo.setFormValues(JSONUtils.toMap(formValuesStr));
        } catch (Exception e) {
            throw AuthenticationException.buildDecryptException(this.getClass(), "form values");
        }
        return credentialInfo;
    }

}
