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

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.schedule.model.execution.TaskExecution;
import io.innospots.schedule.entity.TaskExecutionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Mapper
public interface TaskExecutionConverter extends BaseBeanConverter<TaskExecution, TaskExecutionEntity>{

    TaskExecutionConverter INSTANCE = Mappers.getMapper(TaskExecutionConverter.class);


    @Mapping(target = "taskExecutionId", ignore = true)
    @Mapping(target = "taskName", ignore = true)
    @Mapping(target = "paramContext", ignore = true)
    @Mapping(target = "detailUrl", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    void updateEntity2Model(@MappingTarget TaskExecutionEntity entity, TaskExecution taskExecution);
}
