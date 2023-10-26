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

package io.innospots.libra.kernel.module.logger.mapper;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.utils.DateTimeUtils;
import io.innospots.libra.kernel.module.logger.entity.SysOperateLogEntity;
import io.innospots.libra.kernel.module.logger.model.SysOperateLog;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2021/12/13
 */
@Mapper
public interface SysOperateLogMapper extends BaseBeanConverter {

    SysOperateLogMapper INSTANCE = Mappers.getMapper(SysOperateLogMapper.class);

    /**
     * model to db
     *
     * @param sysOperateLogEntity
     * @return
     */
    @Mapping(target = "operateTime", expression = "java(timeToString(sysOperateLogEntity.getOperateTime()))")
    SysOperateLog modelToEntity(SysOperateLogEntity sysOperateLogEntity);

    /**
     * db to model
     *
     * @param sysOperateLog
     * @return
     */
    SysOperateLogEntity entityToModel(SysOperateLog sysOperateLog);

    /**
     * time to string
     *
     * @param operateTime
     * @return String
     */
    default String timeToString(LocalDateTime operateTime) {
        return DateTimeUtils.formatLocalDateTime(operateTime, null);
    }

    @AfterMapping
    default void updateModel(@MappingTarget SysOperateLog sysOperateLog) {
        if (sysOperateLog.getOperateType() != null) {
            sysOperateLog.setOperate(sysOperateLog.getOperateType().label());
        }
    }
}
