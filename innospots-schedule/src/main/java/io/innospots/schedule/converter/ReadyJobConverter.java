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
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.model.ReadyJob;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.utils.ParamParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/31
 */
public interface ReadyJobConverter extends BaseBeanConverter<ReadyJob, ReadyJobEntity> {

    ReadyJobConverter INSTANCE = Mappers.getMapper(ReadyJobConverter.class);

    static ReadyJobEntity build(ScheduleJobInfo scheduleJobInfo, Map<String,Object> params){
        Map<String,Object> context = new HashMap<>();
        if(MapUtils.isNotEmpty(params)){
            context.putAll(ParamParser.toValueMap(params));
        }
        if(MapUtils.isNotEmpty(scheduleJobInfo.getParams())){
            context.putAll(ParamParser.toValueMap(scheduleJobInfo.getParams()));
        }

        ReadyJobEntity readyJobEntity = new ReadyJobEntity();
        readyJobEntity.setJobReadyKey(InnospotsIdGenerator.generateIdStr());
        readyJobEntity.setContext(JSONUtils.toJsonString(context));
        readyJobEntity.setName(scheduleJobInfo.getJobName());
        readyJobEntity.setKeyType(scheduleJobInfo.getJobType().name());
        readyJobEntity.setKey(scheduleJobInfo.getJobKey());
        readyJobEntity.setSequenceNumber(1);
        readyJobEntity.setResourceKey(scheduleJobInfo.getResourceKey());
        readyJobEntity.setMessageStatus(MessageStatus.UNREAD);
        readyJobEntity.setJobClass(scheduleJobInfo.getJobClass());
        readyJobEntity.setScopes(scheduleJobInfo.getScopes());
                return readyJobEntity;
    }
}
