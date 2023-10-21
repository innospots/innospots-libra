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

package io.innospots.connector.schema.operator;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.crypto.EncryptType;
import io.innospots.base.crypto.EncryptorBuilder;
import io.innospots.base.crypto.IEncryptor;
import io.innospots.base.data.schema.AppCredentialInfo;
import io.innospots.base.data.schema.SimpleAppCredential;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.PageBody;
import io.innospots.base.store.CacheStoreManager;
import io.innospots.base.utils.StringConverter;
import io.innospots.connector.schema.dao.AppCredentialDao;
import io.innospots.connector.schema.entity.AppCredentialEntity;
import io.innospots.connector.schema.mapper.CredentialConvertMapper;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.base.operator.SystemTempCacheOperator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2023/1/19
 */
public class AppCredentialOperator extends ServiceImpl<AppCredentialDao, AppCredentialEntity> {

    private final IEncryptor encryptor;

    private final SchemaRegistryOperator schemaRegistryOperator;

    public AppCredentialOperator(
            AuthProperties authProperties,
            SchemaRegistryOperator schemaRegistryOperator
    ) {
        this.encryptor = EncryptorBuilder.build(EncryptType.BLOWFISH, authProperties.getSecretKey());
        this.schemaRegistryOperator = schemaRegistryOperator;
    }


    public AppCredentialInfo getCredential(Integer credentialId) {
        AppCredentialEntity entity = super.getById(credentialId);
        if (entity == null) {
            return null;
        }
        return CredentialConvertMapper.INSTANCE.entityToModel(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppCredentialInfo createCredential(AppCredentialInfo credential) {
        if (this.checkNameExist(credential.getName(), credential.getConfigCode())) {
            throw ResourceException.buildExistException(this.getClass(), credential.getName());
        }
        if (credential.getCode() != null) {
            if (this.checkCodeExist(credential.getCode(), credential.getConfigCode())) {
                throw ResourceException.buildExistException(this.getClass(), credential.getCode());
            }
        }
        long codeCount = 0;

        do {
            String code = StringConverter.randomKey(4);
            credential.setCode(code);
            QueryWrapper<AppCredentialEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(AppCredentialEntity::getCode, code);
            codeCount = this.count(qw);
        } while (codeCount > 0);
        this.authedValuesProcess(credential);
        AppCredentialEntity entity = CredentialConvertMapper.INSTANCE.modelToEntity(credential);
        super.save(entity);
        return this.getCredential(entity.getCredentialId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AppCredentialInfo updateCredential(AppCredentialInfo appCredentialInfo) {
        if (this.checkNameExistAndExcludeOriginalName(appCredentialInfo.getName(), appCredentialInfo.getConfigCode(), appCredentialInfo.getCredentialId())) {
            throw ResourceException.buildExistException(this.getClass(), appCredentialInfo.getName());
        }
        this.authedValuesProcess(appCredentialInfo);
        AppCredentialEntity entity = CredentialConvertMapper.INSTANCE.modelToEntity(appCredentialInfo);
        super.updateById(entity);
        return this.getCredential(entity.getCredentialId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCredential(Integer credentialId) {
        boolean delete = super.removeById(credentialId);
        schemaRegistryOperator.deleteByCredentialId(credentialId);
        return delete;
    }


    public AppCredentialInfo getCredentialByCode(String credentialCode) {
        QueryWrapper<AppCredentialEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(AppCredentialEntity::getCode, credentialCode);
        AppCredentialEntity appCredentialEntity = this.getOne(qw);
        return CredentialConvertMapper.INSTANCE.entityToModel(appCredentialEntity);
    }

    /**
     * list app credentials
     * @param appNodeCode
     * @return
     */
    public List<SimpleAppCredential> listSimpleAppCredentials(String appNodeCode,String configCode){
        QueryWrapper<AppCredentialEntity> query = new QueryWrapper<>();
        if(appNodeCode!=null){
            query.lambda().eq(AppCredentialEntity::getAppNodeCode,appNodeCode);
        }
        if(configCode!=null){
            query.lambda().eq(AppCredentialEntity::getConfigCode, configCode);
        }

        List<AppCredentialEntity> entities = this.list(query);
        List<SimpleAppCredential> simpleAppCredentials = new ArrayList<>();
        for (AppCredentialEntity entity : entities) {
            SimpleAppCredential simpleAppCredential = CredentialConvertMapper.INSTANCE.entityToSimpleModel(entity);
            simpleAppCredentials.add(simpleAppCredential);
        }
        return simpleAppCredentials;
    }

    public List<AppCredentialInfo> listCredentials(String queryInput, String configCode, String connector, String sort) {
        QueryWrapper<AppCredentialEntity> query = new QueryWrapper<>();

        if (StringUtils.isNotBlank(sort)) {
            query.orderByDesc(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sort));
        }

        if (StringUtils.isNotBlank(configCode)) {
            query.lambda().eq(AppCredentialEntity::getConfigCode, configCode);
        } else if (StringUtils.isNotBlank(connector)) {
            query.lambda().eq(AppCredentialEntity::getConnectorName, connector);
        }
        if (StringUtils.isNotBlank(queryInput)) {
            query.lambda().like(AppCredentialEntity::getName, queryInput)
                    .or().like(AppCredentialEntity::getCode, queryInput);
        }

        List<AppCredentialEntity> entities = super.list(query);
        return CredentialConvertMapper.INSTANCE.entitiesToModels(entities);
    }

    public PageBody<AppCredentialInfo> pageCredentials(String queryInput, String configCode, String connector, String sort, int page, int size) {
        QueryWrapper<AppCredentialEntity> query = new QueryWrapper<>();

        if (sort != null) {
            query.orderByDesc(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sort));
        }


        if (StringUtils.isNotBlank(configCode)) {
            query.lambda().eq(AppCredentialEntity::getConfigCode, configCode);
        } else if (connector != null) {
            query.lambda().eq(AppCredentialEntity::getConnectorName, connector);
        }

        if (StringUtils.isNotBlank(queryInput)) {
            query.lambda().like(AppCredentialEntity::getName, queryInput).or()
                    .like(AppCredentialEntity::getCode, queryInput);
        }

        IPage<AppCredentialEntity> entityPage = super.page(PageDTO.of(page, size), query);
        List<AppCredentialInfo> appCredentialInfoList = CredentialConvertMapper.INSTANCE.entitiesToModels(entityPage.getRecords());

        PageBody<AppCredentialInfo> pageBody = new PageBody<>();
        pageBody.setList(appCredentialInfoList);
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setTotal(entityPage.getTotal());
        return pageBody;

    }


    public List<AppCredentialInfo> listCredentials() {
        return this.listCredentials(null, null, null, null);
    }


    public boolean checkNameExist(String name, String code) {
        return super.count(new QueryWrapper<AppCredentialEntity>().lambda()
                .eq(AppCredentialEntity::getName, name)
                .eq(AppCredentialEntity::getCode, code)) > 0;
    }

    public boolean checkCodeExist(String code, String configCode) {
        return super.count(new QueryWrapper<AppCredentialEntity>().lambda()
                .eq(AppCredentialEntity::getCode, code)
                .eq(AppCredentialEntity::getConfigCode, configCode)) > 0;
    }

    public boolean checkNameExistAndExcludeOriginalName(String name, String configCode, Integer credentialId) {
        return super.count(new QueryWrapper<AppCredentialEntity>().lambda()
                .eq(AppCredentialEntity::getName, name)
                .eq(AppCredentialEntity::getConfigCode, configCode)
                .ne(AppCredentialEntity::getCredentialId, credentialId)) > 0;
    }


    private void authedValuesProcess(AppCredentialInfo credential) {
        if (!"oauth2-auth-api".equals(credential.getConfigCode())) {
            return;
        }
//
//        if (MapUtils.isEmpty(credential.getFormValues())) {
//            return;
//        }

        this.decryptFormValues(credential);
        String clientId = String.valueOf(credential.getFormValues().get("client_id"));

        String cacheKey = clientId + "_" + credential.getAppNodeCode();
        String jsonToken = CacheStoreManager.get(cacheKey);
        if(jsonToken!=null){
            Map<String,Object> tokens = JSONUtils.toMap(jsonToken);
            credential.getFormValues().putAll(tokens);
        }
        String formValuesStr = encryptor.encode(JSONUtils.toJsonString(credential.getFormValues()));
        credential.setEncryptFormValues(formValuesStr);
        credential.getFormValues().clear();
        CacheStoreManager.remove(cacheKey);
    }

    private void decryptFormValues(AppCredentialInfo appCredentialInfo) {
        if (appCredentialInfo == null) {
            return;
        }
        if (StringUtils.isBlank(appCredentialInfo.getEncryptFormValues())) {
            return;
        }
        String formValuesStr = encryptor.decode(appCredentialInfo.getEncryptFormValues());
        appCredentialInfo.setFormValues(JSONUtils.toMap(formValuesStr));
    }
}
