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

package io.innospots.libra.kernel.module.notification.mapper;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.ParamField;
import io.innospots.libra.kernel.module.notification.entity.NotificationChannelEntity;
import io.innospots.libra.kernel.module.notification.model.NotificationChannel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/26
 */
@Mapper
public interface NotificationChannelMapper extends BaseBeanConverter {

    NotificationChannelMapper INSTANCE = Mappers.getMapper(NotificationChannelMapper.class);

    @Mapping(target = "params", expression = "java(jsonStringToList(entity.getParams()))")
    NotificationChannel entity2Model(NotificationChannelEntity entity);

    @Mapping(target = "params", expression = "java(jsonListToString(channel.getParams()))")
    NotificationChannelEntity model2Entity(NotificationChannel channel);

    /**
     * json string to ParamField of list
     *
     * @param jsonStr
     * @return List<String>
     */
    default List<ParamField> jsonStringToList(String jsonStr) {
        return JSONUtils.toList(jsonStr, ParamField.class);
    }

    /**
     * ParamField of list to json string
     *
     * @param list
     * @return String
     */
    default String jsonListToString(List<ParamField> list) {
        return JSONUtils.toJsonString(list);
    }
}
