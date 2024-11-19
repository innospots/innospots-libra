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

    private ExtDefinitionEntity getLastVersion(String extKey) {
        QueryWrapper<ExtDefinitionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ExtDefinitionEntity::getExtKey, extKey)
                .orderByDesc(ExtDefinitionEntity::getExtVersion);
        return this.getOne(qw, false);
    }


    public LibraExtensionProperties registryExtensionDefinition(LibraExtensionProperties libraExtensionProperties) {
        ExtensionDefinition extDefinition = ExtDefinitionConverter.INSTANCE.propertiesToModel(libraExtensionProperties);
        boolean result = true;
        ExtDefinitionEntity entity = getLastVersion(extDefinition.getExtKey());
        if (entity == null) {
            //Judge whether the current application exists. If it does not exist, insert it directly into the database
            entity = ExtDefinitionConverter.INSTANCE.modelToEntity(extDefinition);
            entity.generateSignature();
            entity.setExtensionStatus(ExtensionStatus.AVAILABLE.name());
            log.info("save new extension: {}",libraExtensionProperties);
            result = save(entity);
        } else {
            int compare = extDefinition.getVersion().compareToIgnoreCase(entity.getExtVersion());
            if (compare > 0) {
                //If it exists, and the version is inconsistent and large, insert the update,
                entity = ExtDefinitionConverter.INSTANCE.updateEntity4Model(entity, extDefinition);
                entity.generateSignature();
                if (ExtensionStatus.valueOf(entity.getExtensionStatus()) == ExtensionStatus.EXPIRED) {
                    entity.setExtensionStatus(ExtensionStatus.AVAILABLE.name());
                }
                log.info("update new version extension:{}",entity);
                result = updateById(entity);
            } else if (compare == 0) {
                //If the version is consistent but the signature is inconsistent, the update will be overwritten
                String oldSignature = entity.getSignature();
                ExtDefinitionConverter.INSTANCE.updateEntity4Model(entity, extDefinition);
                if (!oldSignature.equals(entity.generateSignature())) {
                    log.warn("the meta has been changed, update extension definition:{}", extDefinition);
                    result = updateById(entity);
                }
            } else {
                //The version is small and not updated
                log.warn("the version of extension is smaller than database, new version:{} ,old version:{}", extDefinition.getVersion(), entity.getExtVersion());
            }
        }

        if (!result) {
            throw ResourceException.buildUpdateException(this.getClass(), "extKey:" + extDefinition.getExtKey() + " registry error");
        }
        return libraExtensionProperties;
    }


    public List<LibraExtensionInformation> listExtensions() {
        QueryWrapper<ExtDefinitionEntity> queryWrapper = new QueryWrapper<>();
        List<ExtDefinitionEntity> list = this.getBaseMapper().selectList(queryWrapper);
        return list == null ? null : ExtDefinitionConverter.INSTANCE.entityToAppInfoList(list);
    }


}
