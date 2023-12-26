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

package io.innospots.schedule.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.enums.MessageStatus;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/5
 */
public interface ReadyJobDao extends BaseMapper<ReadyJobEntity> {

    default List<ReadyJobEntity> selectUnReadJob(int fetchSize, List<String> groupKeys){
        QueryWrapper<ReadyJobEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ReadyJobEntity::getMessageStatus, MessageStatus.UNREAD)
                .in(CollectionUtils.isNotEmpty(groupKeys), ReadyJobEntity::getGroupKey, groupKeys)
                .orderByDesc(ReadyJobEntity::getCreatedTime)
                .last("limit " +fetchSize);
        return this.selectList(qw);
    }

    /**
     * update by
     * @param readyJobEntity
     * @return
     */
    default int updateAssignedJob(ReadyJobEntity readyJobEntity){
        UpdateWrapper<ReadyJobEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ReadyJobEntity::getMessageStatus, MessageStatus.ASSIGNED)
                .set(ReadyJobEntity::getVersion, readyJobEntity.getVersion() + 1)
                .set(ReadyJobEntity::getServerKey, readyJobEntity.getServerKey())
                .set(ReadyJobEntity::getUpdatedTime, LocalDateTime.now())
               .eq(ReadyJobEntity::getJobReadyKey, readyJobEntity.getJobReadyKey())
                .eq(ReadyJobEntity::getMessageStatus, MessageStatus.UNREAD)
                .eq(ReadyJobEntity::getVersion,readyJobEntity.getVersion());
        return this.update(null, uw);
    }

    default List<ReadyJobEntity> selectAssignJobs(String serverKey){
        if(serverKey==null){
            return Collections.emptyList();
        }
        QueryWrapper<ReadyJobEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ReadyJobEntity::getMessageStatus,MessageStatus.ASSIGNED)
                .eq(ReadyJobEntity::getServerKey, serverKey);
        return this.selectList(qw);
    }

    default int updateStatusAssignedToRead(String jobReadyKey){
        UpdateWrapper<ReadyJobEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ReadyJobEntity::getMessageStatus, MessageStatus.READ)
               .set(ReadyJobEntity::getUpdatedTime, LocalDateTime.now())
               .eq(ReadyJobEntity::getJobReadyKey, jobReadyKey)
               .eq(ReadyJobEntity::getMessageStatus, MessageStatus.ASSIGNED);
        return this.update(null,uw);
    }
}
