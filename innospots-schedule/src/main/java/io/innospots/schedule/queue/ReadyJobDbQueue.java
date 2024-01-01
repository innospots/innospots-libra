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

package io.innospots.schedule.queue;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.base.utils.ServiceActionHolder;
import io.innospots.schedule.converter.ReadyJobConverter;
import io.innospots.schedule.dao.ReadyJobDao;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.model.ReadyJob;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ready job queue store in database table
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/4
 */
@Slf4j
public class ReadyJobDbQueue implements IReadyJobQueue {

    private final ScheduleJobInfoOperator scheduleJobInfoOperator;

    private final ReadyJobDao readyJobDao;

    public ReadyJobDbQueue(ScheduleJobInfoOperator scheduleJobInfoOperator, ReadyJobDao readyJobDao) {
        this.scheduleJobInfoOperator = scheduleJobInfoOperator;
        this.readyJobDao = readyJobDao;
    }

    /**
     * fetch ready execute jobs , msg status is undead , order by created time asc
     *
     * @param fetchSize fetch size
     * @return
     */
    @Override
    public List<ReadyJobEntity> poll(int fetchSize, List<String> groupKeys) {
        //select unread job
        List<ReadyJobEntity> readies = readyJobDao.selectUnReadJob(fetchSize, groupKeys);
        if (CollectionUtils.isEmpty(readies)) {
            return Collections.emptyList();
        }
        //update job status to assigned, and assign to current server
        for (ReadyJobEntity readyJobEntity : readies) {
            if (readies.size() > 1) {
                //Avoid overly concentrated preemption of assigned jobs
                try {
                    TimeUnit.MILLISECONDS.sleep(RandomUtils.nextInt(1, readies.size()));
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
            readyJobEntity.setServerKey(ServiceActionHolder.getServerKey());
            //Update ready job with optimistic locks
            int count = readyJobDao.updateAssignedJob(readyJobEntity);
        }
        //update msg status to read and serverKey
        return readyJobDao.selectAssignJobs(ServiceActionHolder.getServerKey());
    }

    @Override
    public void ackRead(String jobReadyKey) {
        readyJobDao.updateStatusAssignedToRead(jobReadyKey);
    }

    @Override
    public void push(String jobKey, Map<String,Object> params){
        ScheduleJobInfo scheduleJobInfo = scheduleJobInfoOperator.getScheduleJobInfo(jobKey);
        ReadyJobEntity readyJobEntity = ReadyJobConverter.build(scheduleJobInfo,params);
        readyJobDao.insert(readyJobEntity);
    }

    @Override
    public void push(String parentExecutionId, Integer sequenceNumber, String jobKey, Map<String, Object> params) {
        ScheduleJobInfo scheduleJobInfo = scheduleJobInfoOperator.getScheduleJobInfo(jobKey);
        ReadyJobEntity readyJobEntity = ReadyJobConverter.build(scheduleJobInfo,params);
        readyJobEntity.setSequenceNumber(sequenceNumber);
        readyJobEntity.setParentExecutionId(parentExecutionId);
        readyJobDao.insert(readyJobEntity);
    }

    @Override
    public void push(String jobKey) {
        push(jobKey,null);
    }

    @Override
    public void push(ReadyJob readyJob) {
        if(readyJob.getJobReadyKey()==null){
            readyJob.setJobReadyKey(InnospotsIdGenerator.generateIdStr());
        }
        ReadyJobEntity readyJobEntity = ReadyJobConverter.INSTANCE.modelToEntity(readyJob);
        readyJobEntity.setVersion(1);
        readyJobDao.insert(readyJobEntity);
    }

    @Override
    public int resetAssignTimeoutJob(int timeoutSecond) {
        UpdateWrapper<ReadyJobEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ReadyJobEntity::getMessageStatus, MessageStatus.UNREAD)
                .set(ReadyJobEntity::getServerKey,null)
                .eq(ReadyJobEntity::getMessageStatus,MessageStatus.ASSIGNED)
                .lt(ReadyJobEntity::getUpdatedTime, LocalDateTime.now().minusSeconds(timeoutSecond));
        return readyJobDao.update(uw);
    }


    /**
     * cancel assign and unread job
     * @param jobKey
     * @return
     */
    @Override
    public int cancelJob(String jobKey) {
        UpdateWrapper<ReadyJobEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ReadyJobEntity::getMessageStatus, MessageStatus.CANCEL)
                .in(ReadyJobEntity::getMessageStatus,MessageStatus.ASSIGNED,MessageStatus.UNREAD)
               .eq(ReadyJobEntity::getJobReadyKey,jobKey);
        return readyJobDao.update(uw);
    }

    /**
     * cancel job by parentExecutionId
     * @param parentExecutionId
     * @return
     */
    @Override
    public int cancelJobByParentExecutionId(String parentExecutionId) {
        if (parentExecutionId!= null) {
            UpdateWrapper<ReadyJobEntity> uw = new UpdateWrapper<>();
            uw.lambda().set(ReadyJobEntity::getMessageStatus,MessageStatus.CANCEL)
                    .eq(ReadyJobEntity::getParentExecutionId,parentExecutionId)
                   .in(ReadyJobEntity::getMessageStatus,MessageStatus.ASSIGNED,MessageStatus.UNREAD);
            return readyJobDao.update(uw);
        }
        return 0;
    }

}
