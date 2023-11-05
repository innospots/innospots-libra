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

package io.innospots.libra.kernel.module.todo.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.kernel.module.todo.entity.TodoTaskEntity;
import io.innospots.libra.kernel.module.todo.model.TodoTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/8
 */
@Mapper
public interface TodoTaskBeanConverter extends BaseBeanConverter<TodoTask,TodoTaskEntity> {

    TodoTaskBeanConverter INSTANCE = Mappers.getMapper(TodoTaskBeanConverter.class);


    /*
    TodoTaskEntity model2Entity(TodoTask todoTask);

    @Mapping(target = "createdTime", expression = "java(timeToString(todoTaskEntity.getCreatedTime()))")
    @Mapping(target = "updatedTime", expression = "java(timeToString(todoTaskEntity.getUpdatedTime()))")
    TodoTask entity2Model(TodoTaskEntity todoTaskEntity);
     */

    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateEntity2Model(@MappingTarget TodoTaskEntity entity, TodoTask task);

    default String timeToString(LocalDateTime time) {
        return DateTimeUtils.formatLocalDateTime(time, null);
    }



}