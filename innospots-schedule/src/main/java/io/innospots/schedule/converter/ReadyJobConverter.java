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

package io.innospots.schedule.converter;

import cn.hutool.crypto.digest.MD5;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.ReadyJob;
import io.innospots.base.quartz.ScheduleJobInfo;
import io.innospots.schedule.utils.ParamParser;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/31
 */
@Mapper
public interface ReadyJobConverter extends BaseBeanConverter<ReadyJob, ReadyJobEntity> {

    ReadyJobConverter INSTANCE = Mappers.getMapper(ReadyJobConverter.class);

    static ReadyJobEntity build(ReadyJob readyJob,Map<String,Object> params) {
        ReadyJobEntity readyJobEntity = INSTANCE.modelToEntity(readyJob);
        if (readyJobEntity.getContext() == null) {
            readyJobEntity.setContext(JSONUtils.toJsonString(new HashMap<>()));
        }
        //using jobKey and context param to generate digestHex
        if (readyJob.getInstanceKey() == null) {
            String instanceKey = MD5.create().digestHex(readyJobEntity.getJobKey() + readyJobEntity.getContext());
            readyJobEntity.setInstanceKey(instanceKey);
        }
        readyJobEntity.setVersion(1);
        return readyJobEntity;
    }

    static ReadyJobEntity build(ScheduleJobInfo scheduleJobInfo, Map<String, Object> params) {
        Map<String, Object> context = new HashMap<>();
        if (MapUtils.isNotEmpty(params)) {
            context.putAll(ParamParser.toValueMap(params));
        }
        if (MapUtils.isNotEmpty(scheduleJobInfo.getParams())) {
            context.putAll(ParamParser.toValueMap(scheduleJobInfo.getParams()));
        }

        ReadyJobEntity readyJobEntity = new ReadyJobEntity();
        readyJobEntity.setJobReadyKey(InnospotsIdGenerator.generateIdStr());
        readyJobEntity.setContext(JSONUtils.toJsonString(context));
        readyJobEntity.setJobName(scheduleJobInfo.getJobName());
        readyJobEntity.setJobKey(scheduleJobInfo.getJobKey());
        readyJobEntity.setSequenceNumber(1);
        readyJobEntity.setResourceKey(scheduleJobInfo.getResourceKey());
        readyJobEntity.setMessageStatus(MessageStatus.UNREAD.name());
        readyJobEntity.setJobClass(scheduleJobInfo.getJobClass());
        readyJobEntity.setScopes(scheduleJobInfo.getScopes());
        readyJobEntity.setJobType(scheduleJobInfo.getJobType().name());
        readyJobEntity.setSubJobCount(scheduleJobInfo.getSubJobCount());
        //using jobKey and context param to generate digestHex
        readyJobEntity.setVersion(1);
        String instanceKey = MD5.create().digestHex(readyJobEntity.getJobKey() + readyJobEntity.getContext());
        readyJobEntity.setInstanceKey(instanceKey);
        return readyJobEntity;
    }


    static ReadyJob build(JobExecution jobExecution){
        ReadyJob readyJob = new ReadyJob();
        readyJob.setJobKey(jobExecution.getJobKey());
        readyJob.setJobName(jobExecution.getJobName());
        readyJob.setKeyType(jobExecution.getKeyType());
        readyJob.setContext(jobExecution.getContext());
        readyJob.setJobClass(jobExecution.getJobClass());
        readyJob.setJobType(jobExecution.getJobType());
        readyJob.setScopes(jobExecution.getScopes());
        readyJob.setExtExecutionId(jobExecution.getExtExecutionId());
        readyJob.setOriginExecutionId(jobExecution.getExecutionId());
        readyJob.setParentExecutionId(jobExecution.getParentExecutionId());
        readyJob.setMessageStatus(MessageStatus.UNREAD);
        readyJob.setResourceKey(jobExecution.getResourceKey());
        readyJob.setSequenceNumber(jobExecution.getSequenceNumber());
        readyJob.setInstanceKey(jobExecution.getInstanceKey());
        readyJob.setSubJobCount(jobExecution.getSubJobCount());
        return readyJob;
    }
}
