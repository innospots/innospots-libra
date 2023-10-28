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

package io.innospots.libra.security.logger.mapper;

import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.security.logger.entity.LoginLogEntity;
import io.innospots.libra.security.logger.model.LoginLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2021/12/13
 */
@Mapper
public interface LoginLogMapper {
    LoginLogMapper INSTANCE = Mappers.getMapper(LoginLogMapper.class);

    LoginLogEntity model2Entity(LoginLog loginLog);

    @Mapping(target = "loginTime", expression = "java(timeToString(loginLogEntity.getLoginTime()))")
    LoginLog entity2Model(LoginLogEntity loginLogEntity);

    /**
     * time to string
     *
     * @param loginTime
     * @return String
     */
    default String timeToString(LocalDateTime loginTime) {
        return DateTimeUtils.formatLocalDateTime(loginTime, null);
    }
}