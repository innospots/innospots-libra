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

package io.innospots.libra.kernel.module.notification.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.libra.kernel.module.notification.dao.NotificationSettingDao;
import io.innospots.libra.kernel.module.notification.entity.NotificationSettingEntity;
import io.innospots.libra.kernel.module.notification.event.NotificationDefinitionLoader;
import io.innospots.libra.kernel.module.notification.event.NotificationGroup;
import io.innospots.libra.kernel.module.notification.event.NotificationModule;
import io.innospots.libra.kernel.module.notification.converter.NotificationSettingConverter;
import io.innospots.libra.kernel.module.notification.model.NotificationSetting;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/26
 */
@Component
public class NotificationSettingOperator extends ServiceImpl<NotificationSettingDao, NotificationSettingEntity> {

    public List<NotificationSetting> listMessageSettings() {
        QueryWrapper<NotificationSettingEntity> query = new QueryWrapper<>();
        List<NotificationSettingEntity> entities = super.list(query);
        List<NotificationSetting> notificationSettings = new ArrayList<>();
        Map<String, NotificationGroup> groupMap = NotificationDefinitionLoader.load();
        for (NotificationSettingEntity entity : entities) {
            NotificationSetting notificationSetting = NotificationSettingConverter.INSTANCE.entityToModel(entity);
            NotificationGroup notificationGroup = groupMap.get(entity.getExtKey());
            if (notificationGroup != null) {
                notificationSetting.setExtName(notificationGroup.getExtName());
                NotificationModule notificationModule = notificationGroup.getModules().stream().filter(module -> module.getModuleKey().equals(entity.getModuleKey())).findFirst().orElse(null);
                if (notificationModule != null) {
                    notificationSetting.setModuleName(notificationModule.getModuleName());
                }
            }
            notificationSettings.add(notificationSetting);
        }//end for

        return notificationSettings;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMessageSetting(List<NotificationSetting> notificationSettings) {
        QueryWrapper<NotificationSettingEntity> query = new QueryWrapper<>();
        List<NotificationSettingEntity> savedSettingEntities = this.list(query);
        Map<String, NotificationSettingEntity> savedEventMap = new HashMap<>();
        for (NotificationSettingEntity savedSettingEntity : savedSettingEntities) {
            savedEventMap.put(savedSettingEntity.key(), savedSettingEntity);
        }
        List<NotificationSettingEntity> newSettings = new ArrayList<>(notificationSettings.size());
        List<NotificationSettingEntity> updateSettings = new ArrayList<>(notificationSettings.size());
        List<Integer> removedSettings = new ArrayList<>(10);

        for (NotificationSetting setting : notificationSettings) {
            NotificationSettingEntity entity = NotificationSettingConverter.INSTANCE.modelToEntity(setting);
            NotificationSettingEntity savedEntity = savedEventMap.get(entity.key());
            if (savedEntity == null) {
                newSettings.add(entity);
            } else {
                entity.setSettingId(savedEntity.getSettingId());
                updateSettings.add(entity);
            }
            savedEventMap.remove(entity.key());
        }//end for

        for (NotificationSettingEntity settingEntity : savedEventMap.values()) {
            removedSettings.add(settingEntity.getSettingId());
        }
        boolean flag = false;
        if (!newSettings.isEmpty()) {
            flag = this.saveBatch(newSettings);
        }

        if (!updateSettings.isEmpty()) {
            flag = this.updateBatchById(updateSettings) || flag;
        }

        if (!removedSettings.isEmpty()) {
            flag = this.removeByIds(removedSettings) || flag;
        }

        return flag;
    }

    public NotificationSetting getMessageSettingByEventCode(String eventCode) {
        QueryWrapper<NotificationSettingEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<NotificationSettingEntity> lambda = query.lambda();
        lambda.eq(NotificationSettingEntity::getEventCode, eventCode);
        NotificationSettingEntity entity = super.getOne(query);
        return NotificationSettingConverter.INSTANCE.entityToModel(entity);
    }
}