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

package io.innospots.schedule.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.schedule.converter.ScheduleJobInfoConverter;
import io.innospots.schedule.dao.ScheduleJobInfoDao;
import io.innospots.schedule.entity.ScheduleJobInfoEntity;
import io.innospots.schedule.model.ScheduleJobInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/5
 */
public class ScheduleJobInfoOperator extends ServiceImpl<ScheduleJobInfoDao, ScheduleJobInfoEntity> {

    private LocalDateTime lastUpdateTime;

    /**
     * fetch online and schedule jobs, according recent update time
     * @return
     */
    public List<ScheduleJobInfo> fetchQuartzTimeJob() {
        QueryWrapper<ScheduleJobInfoEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ScheduleJobInfoEntity::getJobStatus, DataStatus.ONLINE)
                .eq(ScheduleJobInfoEntity::getScheduleMode, ScheduleMode.SCHEDULED)
                .orderByDesc(ScheduleJobInfoEntity::getUpdatedTime)
                .ge(lastUpdateTime!=null, ScheduleJobInfoEntity::getUpdatedTime, lastUpdateTime);
        List<ScheduleJobInfoEntity> entities = this.list(qw);
        if(CollectionUtils.isEmpty(entities)){
            return Collections.emptyList();
        }
        lastUpdateTime = LocalDateTime.now();
        return ScheduleJobInfoConverter.INSTANCE.entitiesToModels(entities);
    }

    public ScheduleJobInfoEntity createScheduleJobInfo(ScheduleJobInfoEntity scheduleJobInfoEntity) {
        return null;
    }

    public ScheduleJobInfo getScheduleJobInfo(String jobKey) {
        ScheduleJobInfoEntity scheduleJobInfoEntity = this.getById(jobKey);
        if(scheduleJobInfoEntity==null){
            throw ResourceException.buildNotExistException(this.getClass(),"job not exist, jobKey:" + jobKey);
        }
        return ScheduleJobInfoConverter.INSTANCE.entityToModel(scheduleJobInfoEntity);
    }

    public ScheduleJobInfoEntity updateScheduleJobInfo(ScheduleJobInfoEntity scheduleJobInfoEntity) {
        return null;
    }

}
