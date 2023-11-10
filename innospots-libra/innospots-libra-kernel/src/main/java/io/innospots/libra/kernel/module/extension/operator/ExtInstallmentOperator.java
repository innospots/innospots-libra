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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.BeanUtils;
import io.innospots.libra.kernel.events.ExtensionEvent;
import io.innospots.libra.base.extension.ExtensionStatus;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.extension.LibraExtensionInformation;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.kernel.module.extension.dao.ExtDefinitionDao;
import io.innospots.libra.kernel.module.extension.dao.ExtInstallmentDao;
import io.innospots.libra.kernel.module.extension.entity.ExtDefinitionEntity;
import io.innospots.libra.kernel.module.extension.entity.ExtInstallmentEntity;
import io.innospots.libra.kernel.module.extension.converter.AppInstallmentConverter;
import io.innospots.libra.kernel.module.extension.model.ExtensionInstallInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/19
 */
@Slf4j
@Component
public class ExtInstallmentOperator extends ServiceImpl<ExtInstallmentDao, ExtInstallmentEntity> {

    private ExtDefinitionDao extDefinitionDao;


    public ExtInstallmentOperator(ExtDefinitionDao extDefinitionDao) {
        this.extDefinitionDao = extDefinitionDao;
    }

    public List<ExtensionInstallInfo> installedExtensions() {
        List<ExtensionInstallInfo> resList = new ArrayList<>();

        List<ExtDefinitionEntity> extDefinitionEntityList = extDefinitionDao.selectList(null);
        List<ExtInstallmentEntity> list = this.baseMapper.selectList(null);
        Map<String, ExtInstallmentEntity> installmentEntityMap = list.stream().collect(Collectors.toMap(ExtInstallmentEntity::getExtKey, item -> item));
        for (ExtDefinitionEntity extDefinitionEntity : extDefinitionEntityList) {
            ExtInstallmentEntity extInstallmentEntity;
            if (installmentEntityMap.containsKey(extDefinitionEntity.getExtKey())) {
                extInstallmentEntity = installmentEntityMap.get(extDefinitionEntity.getExtKey());
            } else {
                extInstallmentEntity = new ExtInstallmentEntity();
                extInstallmentEntity.setExtKey(extDefinitionEntity.getExtKey());
            }

            resList.add(AppInstallmentConverter.INSTANCE.entityToModel(extInstallmentEntity, extDefinitionEntity));
        }
        return resList;
    }


    public List<LibraExtensionProperties> installedExtBaseInfos() {
        List<LibraExtensionProperties> resList = new ArrayList<>();
        QueryWrapper<ExtInstallmentEntity> queryWrapper = new QueryWrapper<>();
        List<ExtInstallmentEntity> list = this.baseMapper.selectList(queryWrapper);
        List<LibraExtensionInformation> appInformations = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            List<String> appKeys = list.stream().map(ExtInstallmentEntity::getExtKey).collect(Collectors.toList());
            //List<AppDefinitionEntity> appDefinitionEntityList = appDefinitionDao.selectByAppKeys(appKeys);
            for (ExtInstallmentEntity extInstallmentEntity : list) {
                LibraExtensionProperties libraAppProperties = LibraClassPathExtPropertiesLoader.getLibraExtProperties(extInstallmentEntity.getExtKey());
                if (libraAppProperties != null) {
                    LibraExtensionProperties newAppProp = new LibraExtensionProperties();
                    BeanUtils.copyProperties(libraAppProperties, newAppProp);
                    newAppProp.fillI18n();
                    resList.add(newAppProp);
                } else {
                    log.warn("application don't exist in the LibraClassPathAppPropertiesLoader, appKey:{}", extInstallmentEntity.getExtKey());
                }
            }
        }

        return resList;
    }


    public ExtInstallmentEntity saveInstallInfo(ExtInstallmentEntity extInstallmentEntity) {


        int flag = 0;
        if (extInstallmentEntity.getExtInstallmentId() == null) {
            extInstallmentEntity.setInstallTime(LocalDateTime.now());
            extInstallmentEntity.setExtensionStatus(ExtensionStatus.INSTALLED);
            //TODO judge appSource
            //appInstallmentEntity.setAppSource(AppSource.MARKET);
            flag = this.baseMapper.insert(extInstallmentEntity);
        } else {
            //appInstallmentEntity.setInstallVersion(appDefinitionEntity.getAppVersion());
            flag = this.baseMapper.updateById(extInstallmentEntity);
        }

        if (flag != 1) {
            //    appInstallInfo = AppInstallmentConvertMapper.INSTANCE.entityToModel(appInstallmentEntity,appDefinitionEntity);
            //}else{
            throw ResourceException.buildInstallException(this.getClass(), "AppInstallInfo", extInstallmentEntity.getExtKey());
        }

        return extInstallmentEntity;
    }


    public Boolean disabled(String extKey) {
        ExtInstallmentEntity extInstallmentEntity = this.getOne(new QueryWrapper<ExtInstallmentEntity>().lambda().eq(ExtInstallmentEntity::getExtKey, extKey));
        if (extInstallmentEntity == null) {
            log.error("extension disabled error, extKey {} is not exits", extKey);
            throw ResourceException.buildNotExistException(this.getClass(), "extKey", extKey);
        }
        if (extInstallmentEntity.getExtensionStatus() != null && extInstallmentEntity.getExtensionStatus().canDisabled()) {
            UpdateWrapper<ExtInstallmentEntity> wrapper = new UpdateWrapper<>();
            wrapper.lambda().eq(ExtInstallmentEntity::getExtInstallmentId, extInstallmentEntity.getExtInstallmentId())
                    .set(ExtInstallmentEntity::getExtensionStatus, ExtensionStatus.DISABLED);
            this.baseMapper.update(null, wrapper);
        } else {
            log.error("extension disabled error, extKey:{} status:{}", extKey, extInstallmentEntity.getExtensionStatus());
            throw ResourceException.buildUpdateException(this.getClass(), "extKey", extKey);
        }

        //send appEvent notice status change
        EventBusCenter.getInstance().post(new ExtensionEvent(extInstallmentEntity.getExtKey(), "", ExtensionStatus.DISABLED));

        return true;
    }

    public Boolean enabled(String extKey) {
        ExtInstallmentEntity extInstallmentEntity = this.getOne(new QueryWrapper<ExtInstallmentEntity>().lambda().eq(ExtInstallmentEntity::getExtKey, extKey));
        if (extInstallmentEntity == null) {
            log.error("extension available error, extKey {} is not exits", extKey);
            throw ResourceException.buildNotExistException(this.getClass(), "extKey", extKey);
        }
        if (extInstallmentEntity.getExtensionStatus() != null && extInstallmentEntity.getExtensionStatus().canAvailable()) {
            UpdateWrapper<ExtInstallmentEntity> wrapper = new UpdateWrapper<>();
            wrapper.lambda().eq(ExtInstallmentEntity::getExtInstallmentId, extInstallmentEntity.getExtInstallmentId())
                    .set(ExtInstallmentEntity::getExtensionStatus, ExtensionStatus.ENABLED);
            this.baseMapper.update(null, wrapper);
        } else {
            log.error("extension available error, extKey:{} status:{}", extKey, extInstallmentEntity.getExtensionStatus());
            throw ResourceException.buildUpdateException(this.getClass(), "extKey", extKey);
        }

        //send appEvent notice status change
        EventBusCenter.getInstance().post((new ExtensionEvent(extInstallmentEntity.getExtKey(), "", ExtensionStatus.ENABLED)));

        return true;
    }

    public ExtInstallmentEntity selectByExtKey(String extKey) {
        return this.getOne(new QueryWrapper<ExtInstallmentEntity>().lambda().eq(ExtInstallmentEntity::getExtKey, extKey));
    }

}
