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

package io.innospots.libra.kernel.module.credential.operator;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.crypto.EncryptType;
import io.innospots.base.crypto.EncryptorBuilder;
import io.innospots.base.crypto.IEncryptor;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.StringConverter;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.kernel.module.credential.converter.CredentialInfoConverter;
import io.innospots.libra.kernel.module.credential.dao.CredentialInfoDao;
import io.innospots.libra.kernel.module.credential.entity.CredentialInfoEntity;
import io.innospots.libra.kernel.module.credential.entity.CredentialTypeEntity;
import io.innospots.libra.kernel.module.credential.model.CredentialInfo;
import io.innospots.libra.kernel.module.credential.model.SimpleCredentialInfo;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2023/1/19
 */
public class CredentialInfoOperator extends ServiceImpl<CredentialInfoDao, CredentialInfoEntity> {

    private final IEncryptor encryptor;

    public CredentialInfoOperator(AuthProperties authProperties) {
        this.encryptor = EncryptorBuilder.build(EncryptType.BLOWFISH, authProperties.getSecretKey());
    }

    public CredentialInfo getCredential(String credentialKey) {
        CredentialInfoEntity entity = super.getById(credentialKey);
        if (entity == null) {
            return null;
        }
        CredentialInfo credentialInfo = CredentialInfoConverter.INSTANCE.entityToModel(entity);
        //decryptFormValues(credentialInfo);
        return credentialInfo;

    }

    @Transactional(rollbackFor = Exception.class)
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
        //this.authedValuesProcess(credential);
        CredentialInfoEntity entity = CredentialInfoConverter.INSTANCE.modelToEntity(credential);
        super.save(entity);
        return this.getCredential(entity.getCredentialKey());
    }

    @Transactional(rollbackFor = Exception.class)
    public CredentialInfo updateCredential(CredentialInfo credentialInfo) {
        if (this.checkExist(credentialInfo.getName(), credentialInfo.getCredentialKey())) {
            throw ResourceException.buildExistException(this.getClass(), credentialInfo.getName());
        }
//        this.authedValuesProcess(credentialInfo);
        CredentialInfoEntity entity = CredentialInfoConverter.INSTANCE.modelToEntity(credentialInfo);
        super.updateById(entity);
        return this.getCredential(entity.getCredentialKey());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCredential(String credentialKey) {
        return  super.removeById(credentialKey);
    }

    /**
     * list credentials
     * @param credentialTypeCode
     * @return
     */
    public List<SimpleCredentialInfo> listSimpleAppCredentials(String credentialTypeCode){
        QueryWrapper<CredentialInfoEntity> query = new QueryWrapper<>();
        if(credentialTypeCode!=null){
            query.lambda().eq(CredentialInfoEntity::getCredentialTypeCode,credentialTypeCode);
        }
        List<CredentialInfoEntity> entities = this.list(query);
        Set<String> typeCodes = entities.stream().map(CredentialInfoEntity::getCredentialTypeCode).collect(Collectors.toSet());
        Map<String,CredentialTypeEntity> types = findCredentialTypes(typeCodes);
        List<SimpleCredentialInfo> simpleAppCredentials = new ArrayList<>();

        for (CredentialInfoEntity entity : entities) {
            CredentialTypeEntity credentialTypeEntity = types.get(entity.getCredentialTypeCode());
            SimpleCredentialInfo simpleAppCredential = CredentialInfoConverter.INSTANCE.entityToSimpleModel(entity,credentialTypeEntity);
            simpleAppCredentials.add(simpleAppCredential);
        }

        return simpleAppCredentials;
    }


    public PageBody<SimpleCredentialInfo> pageCredentials(FormQuery formQuery) {
        QueryWrapper<CredentialInfoEntity> query = new QueryWrapper<>();

        if (formQuery.getSort() != null) {
            query.orderByDesc(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, formQuery.getSort()));
        }


        if (StringUtils.isNotBlank(formQuery.getQueryInput())) {
            query.lambda().like(CredentialInfoEntity::getName, formQuery.getQueryInput());
        }
        if(MapUtils.isNotEmpty(formQuery.getParams())){
            String credentialTypeCode = formQuery.getParams().get("credentialTypeCode");
            if(credentialTypeCode!=null){
                query.lambda().eq(CredentialInfoEntity::getCredentialTypeCode,credentialTypeCode);
            }
        }

        IPage<CredentialInfoEntity> entityPage = super.page(PageDTO.of(formQuery.getPage(), formQuery.getSize()), query);
        List<SimpleCredentialInfo> credentialInfos = new ArrayList<>();
        Set<String> typeCodes = entityPage.getRecords().stream().map(CredentialInfoEntity::getCredentialTypeCode).collect(Collectors.toSet());
        Map<String,CredentialTypeEntity> types = findCredentialTypes(typeCodes);
        for (CredentialInfoEntity entity : entityPage.getRecords()) {
            CredentialTypeEntity credentialTypeEntity = types.get(entity.getCredentialTypeCode());
            SimpleCredentialInfo simpleCredentialInfo = CredentialInfoConverter.INSTANCE.entityToSimpleModel(entity,credentialTypeEntity);
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
        qw.lambda().eq(CredentialInfoEntity::getName,name);
        if(credentialKey!=null){
            qw.lambda().ne(CredentialInfoEntity::getCredentialKey,credentialKey);
        }
        return super.count(qw) > 0;
    }


    private void authedValuesProcess(CredentialInfo credential) {

        this.decryptFormValues(credential);
        /*
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

         */
    }

    private Map<String, CredentialTypeEntity> findCredentialTypes(Set<String> typeCode){
        return null;
    }

    private void decryptFormValues(CredentialInfo credentialInfo) {
        if (credentialInfo == null) {
            return;
        }
        if (StringUtils.isBlank(credentialInfo.getEncryptFormValues())) {
            return;
        }
        String formValuesStr = encryptor.decode(credentialInfo.getEncryptFormValues());
        credentialInfo.setFormValues(JSONUtils.toMap(formValuesStr));
    }
}
