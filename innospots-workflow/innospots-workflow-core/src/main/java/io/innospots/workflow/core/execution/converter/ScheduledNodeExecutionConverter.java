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

package io.innospots.workflow.core.execution.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.execution.entity.ScheduledNodeExecutionEntity;
import io.innospots.workflow.core.execution.scheduled.ScheduledNodeExecution;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/5
 */
@Mapper
public interface ScheduledNodeExecutionConverter extends BaseBeanConverter<ScheduledNodeExecution,ScheduledNodeExecutionEntity> {


    ScheduledNodeExecutionConverter INSTANCE = Mappers.getMapper(ScheduledNodeExecutionConverter.class);


    default Map<String, Object> modelToMap(ScheduledNodeExecution scheduledNodeExecution, boolean underscore) {
        ScheduledNodeExecutionEntity scheduledNodeExecutionEntity = INSTANCE.modelToEntity(scheduledNodeExecution);
        if (scheduledNodeExecution.getUpdatedTime() == null) {
            scheduledNodeExecutionEntity.setCreatedTime(LocalDateTime.now());
        }
        scheduledNodeExecutionEntity.setUpdatedTime(LocalDateTime.now());
        scheduledNodeExecutionEntity.setUpdatedBy(CCH.authUser());
        scheduledNodeExecutionEntity.setCreatedBy(CCH.authUser());
        scheduledNodeExecutionEntity.setProjectId(CCH.projectId());
        if (scheduledNodeExecutionEntity.getMessage() != null && scheduledNodeExecutionEntity.getMessage().length() > 2048) {
            scheduledNodeExecutionEntity.setMessage(scheduledNodeExecutionEntity.getMessage().substring(0, 2048));
        }
        return BeanUtils.toMap(scheduledNodeExecutionEntity, underscore, true);
    }

    default ScheduledNodeExecution mapToModel(Map<String, Object> data, boolean underscore) {
        ScheduledNodeExecutionEntity entity = BeanUtils.toBean(data, ScheduledNodeExecutionEntity.class, underscore);
        return INSTANCE.entityToModel(entity);
    }

}
