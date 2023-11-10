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

package io.innospots.workflow.console.converter.execution;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.console.entity.execution.FlowExecutionEntity;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.flow.FlowExecutionBase;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/18
 */
@Mapper
public interface FlowExecutionConverter extends BaseBeanConverter<FlowExecution,FlowExecutionEntity> {

    FlowExecutionConverter INSTANCE = Mappers.getMapper(FlowExecutionConverter.class);


    FlowExecutionBase entityToBaseModel(FlowExecutionEntity flowExecutionEntity);

    default Map<String, Object> modelToMap(FlowExecution flowExecution, boolean underscore) {
        FlowExecutionEntity flowExecutionEntity = INSTANCE.modelToEntity(flowExecution);
        if (flowExecution.getUpdatedTime() == null) {
            flowExecutionEntity.setCreatedTime(LocalDateTime.now());
        }
        flowExecutionEntity.setUpdatedTime(LocalDateTime.now());
        flowExecutionEntity.setUpdatedBy(CCH.authUser());
        flowExecutionEntity.setCreatedBy(CCH.authUser());
        flowExecutionEntity.setProjectId(CCH.projectId());

        //BeanUtils.fillNullValue(flowExecution.getContext(),flowExecutionEntity);
        return BeanUtils.toMap(flowExecutionEntity, underscore, true);
    }

    default FlowExecutionBase mapToBaseModel(Map<String, Object> data, boolean underscore) {
        FlowExecutionEntity entity = BeanUtils.toBean(data, FlowExecutionEntity.class, underscore);
        return entityToBaseModel(entity);
    }

    default FlowExecution mapToModel(Map<String, Object> data, boolean underscore) {
        FlowExecutionEntity entity = BeanUtils.toBean(data, FlowExecutionEntity.class, underscore);
        return entityToModel(entity);
    }
}
