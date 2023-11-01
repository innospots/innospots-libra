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
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.libra.kernel.module.notification.dao.NotificationChannelDao;
import io.innospots.libra.kernel.module.notification.entity.NotificationChannelEntity;
import io.innospots.libra.kernel.module.notification.converter.NotificationChannelMapper;
import io.innospots.libra.kernel.module.notification.model.NotificationChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/26
 */
@Component
public class NotificationChannelOperator extends ServiceImpl<NotificationChannelDao, NotificationChannelEntity> {

    @Transactional(rollbackFor = Exception.class)
    public NotificationChannel createChannel(NotificationChannel notificationChannel) {
        this.checkDifferentChannelName(notificationChannel);
        NotificationChannelEntity entity = NotificationChannelMapper.INSTANCE.model2Entity(notificationChannel);
        entity.setStatus(DataStatus.ONLINE);
        super.save(entity);
        return NotificationChannelMapper.INSTANCE.entity2Model(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateChannel(NotificationChannel notificationChannel) {
        this.checkChannelExist(notificationChannel.getChannelId());
        this.checkDifferentChannelName(notificationChannel);
        NotificationChannelEntity entity = NotificationChannelMapper.INSTANCE.model2Entity(notificationChannel);
        return super.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteChannel(Integer channelId) {
        this.checkChannelExist(channelId);
        return super.removeById(channelId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Integer channelId, DataStatus dataStatus) {
        this.checkChannelExist(channelId);
        NotificationChannelEntity entity = super.getById(channelId);

        if (dataStatus != entity.getStatus()) {
            entity.setStatus(dataStatus);
            return super.updateById(entity);
        }
        return true;
    }

    public NotificationChannel getMessageChannel(Integer channelId) {
        NotificationChannelEntity entity = super.getById(channelId);
        return NotificationChannelMapper.INSTANCE.entity2Model(entity);
    }

    public List<NotificationChannel> getMessageChannels(List<Integer> messageChannelIds) {
        if (CollectionUtils.isEmpty(messageChannelIds)) {
            return Collections.emptyList();
        }
        List<NotificationChannelEntity> entities = super.listByIds(messageChannelIds);
        return entities.stream().map(NotificationChannelMapper.INSTANCE::entity2Model).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    public List<NotificationChannel> listChannels() {
        QueryWrapper<NotificationChannelEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<NotificationChannelEntity> lambda = query.lambda();
        lambda.orderByAsc(NotificationChannelEntity::getCredentialId);
        lambda.orderByDesc(NotificationChannelEntity::getUpdatedTime);
        List<NotificationChannelEntity> entities = super.list(query);
        return entities.stream().map(NotificationChannelMapper.INSTANCE::entity2Model).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    /**
     * check channel exist
     *
     * @param channelId
     * @return
     */
    private void checkChannelExist(Integer channelId) {
        NotificationChannelEntity entity = super.getById(channelId);
        if (entity == null) {
            throw ResourceException.buildExistException(this.getClass(), "message does not exist", channelId);
        }
    }

    /**
     * check different channel have the same name
     *
     * @param notificationChannel
     */
    private void checkDifferentChannelName(NotificationChannel notificationChannel) {
        QueryWrapper<NotificationChannelEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<NotificationChannelEntity> lambda = queryWrapper.lambda();
        lambda.eq(NotificationChannelEntity::getChannelName, notificationChannel.getChannelName());
        if (notificationChannel.getChannelId() != null) {
            lambda.ne(NotificationChannelEntity::getChannelId, notificationChannel.getChannelId());
        }
        long count = super.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "channel name", notificationChannel.getChannelName());
        }
    }
}
