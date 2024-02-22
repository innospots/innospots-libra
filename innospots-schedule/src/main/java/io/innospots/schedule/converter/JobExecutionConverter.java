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
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.schedule.entity.JobExecutionEntity;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.JobExecutionDisplay;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Mapper
public interface JobExecutionConverter extends BaseBeanConverter<JobExecution, JobExecutionEntity>{

    JobExecutionConverter INSTANCE = Mappers.getMapper(JobExecutionConverter.class);

    JobExecutionEntity readyJobToJobExecution(ReadyJobEntity readyJobEntity);

    static JobExecutionEntity build(ReadyJobEntity readyJobEntity){
        JobExecutionEntity jobExecutionEntity = INSTANCE.readyJobToJobExecution(readyJobEntity);
        jobExecutionEntity.setExecutionId(String.valueOf(InnospotsIdGenerator.generateId()));
        jobExecutionEntity.setInstanceKey(readyJobEntity.getInstanceKey());
        return jobExecutionEntity;
    }

    List<JobExecutionDisplay> entitiesToDisplays(List<JobExecutionEntity> entities);

    JobExecutionDisplay enitityToDisplay(JobExecutionEntity entity);
}
