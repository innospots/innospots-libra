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
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.quartz.JobType;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.schedule.entity.JobExecutionEntity;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.JobExecutionDisplay;
import io.innospots.schedule.model.ReadyJob;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Mapper
public interface JobExecutionConverter extends BaseBeanConverter<JobExecution, JobExecutionEntity> {

    JobExecutionConverter INSTANCE = Mappers.getMapper(JobExecutionConverter.class);

    JobExecutionEntity readyJobToJobExecution(ReadyJobEntity readyJobEntity);

    static JobExecutionEntity build(ReadyJobEntity readyJobEntity) {
        JobExecutionEntity jobExecutionEntity = INSTANCE.readyJobToJobExecution(readyJobEntity);
        jobExecutionEntity.setExecutionId(String.valueOf(InnospotsIdGenerator.generateId()));
        jobExecutionEntity.setExecutionStatus(ExecutionStatus.RUNNING.name());
        jobExecutionEntity.setStartTime(LocalDateTime.now());
        jobExecutionEntity.setPercent(0);
        return jobExecutionEntity;
    }

    static List<JobExecutionDisplay> entitiesToDisplays(List<JobExecutionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<JobExecutionDisplay> displays = new ArrayList<>();
        for (JobExecutionEntity entity : entities) {
            JobExecutionDisplay display = new JobExecutionDisplay();
            entityToDisplay(entity,display);
            displays.add(display);
        }
        return displays;
//        return entities.stream().map(JobExecutionConverter::entityToDisplay).collect(Collectors.toList());
    }


    static void entityToDisplay(JobExecutionEntity entity,JobExecutionDisplay jobExecutionDisplay){
//        JobExecutionDisplay jobExecutionDisplay = new JobExecutionDisplay();
        jobExecutionDisplay.setExecutionId(entity.getExecutionId());
        jobExecutionDisplay.setJobKey(entity.getJobKey());
        jobExecutionDisplay.setJobName(entity.getJobName());
        jobExecutionDisplay.setJobClass(entity.getJobClass());
        jobExecutionDisplay.setJobType(JobType.valueOf(entity.getJobType()));
        jobExecutionDisplay.setServerKey(entity.getServerKey());
        jobExecutionDisplay.setResourceKey(entity.getResourceKey());
        jobExecutionDisplay.setPercent(entity.getPercent());
        jobExecutionDisplay.setStartTime(entity.getStartTime());
        jobExecutionDisplay.setEndTime(entity.getEndTime());
        jobExecutionDisplay.setSelfEndTime(entity.getSelfEndTime());
        jobExecutionDisplay.setExecutionStatus(ExecutionStatus.valueOf(entity.getExecutionStatus()));
        jobExecutionDisplay.setMessage(entity.getMessage());
        jobExecutionDisplay.setDetailUri(entity.getDetailUri());
        jobExecutionDisplay.setScopes(entity.getScopes());
        jobExecutionDisplay.setCreatedBy(entity.getCreatedBy());
        jobExecutionDisplay.setContext(JobExecutionConverter.INSTANCE.jsonStrToMap(entity.getContext()));
        jobExecutionDisplay.setKeyType(entity.getKeyType());
        jobExecutionDisplay.setSubJobCount(entity.getSubJobCount());
        jobExecutionDisplay.setSuccessCount(entity.getSuccessCount());
        jobExecutionDisplay.setFailCount(entity.getFailCount());
        jobExecutionDisplay.setSequenceNumber(entity.getSequenceNumber());
        jobExecutionDisplay.setOriginExecutionId(entity.getOriginExecutionId());
        jobExecutionDisplay.setSelfEndTime(entity.getSelfEndTime());
        jobExecutionDisplay.setPercent(entity.getPercent());
//        return jobExecutionDisplay;
    }

}
