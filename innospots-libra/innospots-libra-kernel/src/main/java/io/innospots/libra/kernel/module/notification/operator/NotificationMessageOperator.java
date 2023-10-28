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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.event.MessageEvent;
import io.innospots.libra.kernel.module.notification.dao.NotificationMessageDao;
import io.innospots.libra.kernel.module.notification.entity.NotificationMessageEntity;
import io.innospots.libra.kernel.module.notification.mapper.NotificationMessageMapper;
import io.innospots.libra.kernel.module.notification.model.MessageFormQuery;
import io.innospots.libra.kernel.module.notification.model.NotificationMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@Component
public class NotificationMessageOperator extends ServiceImpl<NotificationMessageDao, NotificationMessageEntity> {

    /**
     * Filter data sets by criteria
     *
     * @return
     */
    public PageBody<NotificationMessage> pageMessages(MessageFormQuery request) {
        QueryWrapper<NotificationMessageEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<NotificationMessageEntity> lambda = queryWrapper.lambda();
        if (request.getUserId() != null) {
            lambda.eq(NotificationMessageEntity::getReceiveUserId, request.getUserId());
        }
        if (StringUtils.isNotEmpty(request.getQueryInput())) {
            lambda.or().like(NotificationMessageEntity::getTitle, request.getQueryInput());
        }
        if (StringUtils.isNotBlank(request.getSort())) {
            queryWrapper.orderBy(true, request.getAsc(),
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        }

        PageBody<NotificationMessage> pageBody = new PageBody<>();
        IPage<NotificationMessageEntity> oPage = new Page<>(request.getPage(), request.getSize());
        IPage<NotificationMessageEntity> entityPage = super.page(oPage, queryWrapper);
        List<NotificationMessageEntity> entities = entityPage.getRecords();
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setTotal(entityPage.getTotal());
        pageBody.setTotalPage(entityPage.getPages());

        List<NotificationMessage> messages = entities.stream().map(NotificationMessageMapper.INSTANCE::entity2Model).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
        pageBody.setList(messages);
        return pageBody;
    }

    /**
     * count unread message
     *
     * @return
     */
    public long countUnreadMessage() {
        Integer userId = CCH.userId();
        QueryWrapper<NotificationMessageEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<NotificationMessageEntity> lambda = queryWrapper.lambda();
        lambda.eq(NotificationMessageEntity::getReadFlag, Boolean.FALSE);
        lambda.eq(NotificationMessageEntity::getReceiveUserId, userId);
        return super.count(queryWrapper);
    }

    /**
     * add message
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void record(MessageEvent messageEvent) {
        NotificationMessageEntity notificationMessageEntity = new NotificationMessageEntity();
//        messageLogEntity.setTitle(title);
//        messageLogEntity.setMessage(message);
//        messageLogEntity.setReceiveUser(receiveUser);
//        messageLogEntity.setMsgSource(msgSource);
        notificationMessageEntity.setRecordTime(LocalDateTime.now());
        super.save(notificationMessageEntity);
    }

    /**
     * batch update message read time
     *
     * @param messageIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessage(List<String> messageIds) {
        List<NotificationMessageEntity> messageEntities = super.listByIds(messageIds);
        if (CollectionUtils.isEmpty(messageEntities)) {
            throw ResourceException.buildExistException(this.getClass(), "message does not exist");
        }
        LocalDateTime readTime = LocalDateTime.now();
        messageEntities.forEach(messageEntity -> messageEntity.setReadTime(readTime));
        return this.saveOrUpdateBatch(messageEntities);
    }

    /**
     * batch delete message
     *
     * @param messageIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(List<Integer> messageIds) {
        return this.removeByIds(messageIds);
    }
}