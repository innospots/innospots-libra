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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.connector.credential.converter.CredentialTypeConverter;
import io.innospots.base.connector.credential.dao.CredentialTypeDao;
import io.innospots.base.connector.credential.entity.CredentialTypeEntity;
import io.innospots.base.connector.credential.model.CredentialType;
import io.innospots.base.constant.PathConstant;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.base.enums.ImageResource;
import io.innospots.base.enums.ImageType;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.events.NewAvatarEvent;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.StringConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/10/29
 */
public class CredentialTypeOperator extends ServiceImpl<CredentialTypeDao, CredentialTypeEntity> {



    public CredentialType createCredentialType(CredentialType credentialType) {
        if (this.checkExist(credentialType.getName(),null)) {
            throw ResourceException.buildExistException(this.getClass(), credentialType.getName());
        }

        long codeCount = 0;
        do {
            String code = StringConverter.randomKey(8);
            credentialType.setTypeCode(code);
            QueryWrapper<CredentialTypeEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(CredentialTypeEntity::getTypeCode, code);
            codeCount = this.count(qw);
        } while (codeCount > 0);
        if(credentialType.getIcon()!=null && !ImageResource.imageResource(credentialType.getIcon()).isLink()){
            //save base64image
            EventBusCenter.postSync(new NewAvatarEvent(credentialType.getTypeCode(), ImageType.CREDENTIAL, 0, credentialType.getIcon()));
            credentialType.setIcon(PathConstant.ROOT_PATH+ImageType.CREDENTIAL+"/"+credentialType.getTypeCode());
        }
        CredentialTypeEntity entity = CredentialTypeConverter.INSTANCE.modelToEntity(credentialType);
        this.save(entity);
        return credentialType;
    }

    public CredentialType updateCredentialType(CredentialType credentialType){
        if (this.checkExist(credentialType.getName(),credentialType.getTypeCode())) {
            throw ResourceException.buildExistException(this.getClass(), credentialType.getName());
        }
        CredentialTypeEntity entity = CredentialTypeConverter.INSTANCE.modelToEntity(credentialType);
        this.updateById(entity);
        return credentialType;
    }

    public boolean deleteCredentialType(String typeCode){
        return this.removeById(typeCode);
    }

    public CredentialType getCredentialType(String credentialTypeCode) {
        CredentialTypeEntity entity = this.getById(credentialTypeCode);
        return CredentialTypeConverter.INSTANCE.entityToModel(entity);
    }

    public List<CredentialType> listCredentialTypes(String connectorName){
        QueryWrapper<CredentialTypeEntity> qw = new QueryWrapper<>();
        if(connectorName != null){
            qw.lambda().eq(CredentialTypeEntity::getConnectorName,connectorName);
        }
        return CredentialTypeConverter.INSTANCE.entitiesToModels(this.list(qw));
    }

    public PageBody<CredentialType> pageCredentialTypes(FormQuery formQuery){
        PageBody<CredentialType> pageBody = new PageBody<>();
        QueryWrapper<CredentialTypeEntity> qw = new QueryWrapper<>();
        if(StringUtils.isNotBlank(formQuery.getQueryInput())){
            qw.lambda().like(CredentialTypeEntity::getName,formQuery.getQueryInput());
        }
        if(MapUtils.isNotEmpty(formQuery.getParamMap())){
            String connector = formQuery.getParamMap().get("connectorName");
            if(connector!=null){
                qw.lambda().eq(CredentialTypeEntity::getConnectorName,connector);
            }
        }
        Page<CredentialTypeEntity> queryPage = new Page<>(formQuery.getPage(), formQuery.getSize());
        if (StringUtils.isNotBlank(formQuery.getSort())) {
            qw.orderBy(true, formQuery.getAsc(),
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, formQuery.getSort()));
        }
        queryPage = this.page(queryPage,qw);
        pageBody.setTotal(queryPage.getTotal());
        pageBody.setCurrent(queryPage.getCurrent());
        pageBody.setPageSize(queryPage.getSize());
        pageBody.setTotalPage(queryPage.getPages());
        pageBody.setList(CollectionUtils.isEmpty(queryPage.getRecords()) ? new ArrayList<>() :
                CredentialTypeConverter.INSTANCE.entitiesToModels(queryPage.getRecords()));
        return pageBody;
    }

    private boolean checkExist(String name,String code) {
        QueryWrapper<CredentialTypeEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(CredentialTypeEntity::getName, name);
        if(code!=null){
            qw.lambda().ne(CredentialTypeEntity::getTypeCode,code);
        }
        return super.count(qw) > 0;
    }

}
