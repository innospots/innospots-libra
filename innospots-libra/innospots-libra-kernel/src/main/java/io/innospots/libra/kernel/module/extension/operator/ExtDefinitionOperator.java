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

package io.innospots.libra.kernel.module.extension.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ResourceException;
import io.innospots.libra.base.extension.ExtensionStatus;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.extension.LibraExtensionInformation;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.kernel.module.extension.converter.ExtDefinitionConverter;
import io.innospots.libra.kernel.module.extension.dao.ExtDefinitionDao;
import io.innospots.libra.kernel.module.extension.entity.ExtDefinitionEntity;
import io.innospots.libra.kernel.module.extension.model.ExtensionDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Smars
 * @date 2021/12/6
 */
@Slf4j
@Component
public class ExtDefinitionOperator extends ServiceImpl<ExtDefinitionDao, ExtDefinitionEntity> {


    public ExtDefinitionOperator() {
    }


    public LibraExtensionProperties registryExtensionDefinition(String extKey) {
        LibraExtensionProperties libraAppProperties = LibraClassPathExtPropertiesLoader.loadLibraExtProperties(extKey, false);
        if (libraAppProperties == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "extKey:" + extKey);
        }
        return registryExtensionDefinition(libraAppProperties);
    }


    public LibraExtensionProperties registryExtensionDefinition(LibraExtensionProperties libraAppProperties) {
        ExtensionDefinition appDefinition = ExtDefinitionConverter.INSTANCE.propertiesToModel(libraAppProperties);
        boolean result = false;
        ExtDefinitionEntity entity = this.getBaseMapper().getLastVersion(appDefinition.getExtKey());
        if (entity == null) {
            //Judge whether the current application exists. If it does not exist, insert it directly into the database
            entity = ExtDefinitionConverter.INSTANCE.modelToEntity(appDefinition);
            entity.generateSignature();
            entity.setExtensionStatus(ExtensionStatus.AVAILABLE.name());
            result = save(entity);
        } else {
            int compare = appDefinition.getVersion().compareToIgnoreCase(entity.getExtVersion());
            if (compare > 0) {
                //If it exists, and the version is inconsistent and large, insert the update,
                entity = ExtDefinitionConverter.INSTANCE.updateEntity4Model(entity, appDefinition);
                entity.generateSignature();
                if (ExtensionStatus.valueOf(entity.getExtensionStatus()) == ExtensionStatus.EXPIRED) {
                    entity.setExtensionStatus(ExtensionStatus.AVAILABLE.name());
                }
                //result = save(entity);
                result = updateById(entity);
            /*} else if (compare == 0) {
                //If the version is consistent but the signature is inconsistent, the update will be overwritten
                String oldSignature = entity.getSignature();
                AppDefinitionConvertMapper.INSTANCE.updateEntity4Model(entity, appDefinition);
                if (!oldSignature.equals(entity.generateSignature())) {
                    log.error("update application definition:{}", appDefinition);
                    result = updateById(entity);
                } */
            } else {
                //The version is small and not updated
                throw ResourceException.buildUpdateException(this.getClass(), "appKey:" + appDefinition.getExtKey() + " version is error");
            }
        }
        if (!result) {
            //Find the fields in the menu that need to be internationalized,
            //and send write dictionary and internationalized translation events
            //sendI18nDictEvents(appProperties);
            //return appDefinition;
            throw ResourceException.buildUpdateException(this.getClass(), "appKey:" + appDefinition.getExtKey() + " registry error");
        }
        return libraAppProperties;
    }


    public List<LibraExtensionInformation> listExtensions() {
        QueryWrapper<ExtDefinitionEntity> queryWrapper = new QueryWrapper<>();
        List<ExtDefinitionEntity> list = this.getBaseMapper().selectList(queryWrapper);
        return list == null ? null : ExtDefinitionConverter.INSTANCE.entityToAppInfoList(list);
    }


}
