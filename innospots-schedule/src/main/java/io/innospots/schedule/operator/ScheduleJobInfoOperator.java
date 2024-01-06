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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.schedule.converter.ScheduleJobInfoConverter;
import io.innospots.schedule.dao.ScheduleJobInfoDao;
import io.innospots.schedule.entity.ScheduleJobInfoEntity;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.model.ScheduleJobInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/5
 */
public class ScheduleJobInfoOperator extends ServiceImpl<ScheduleJobInfoDao, ScheduleJobInfoEntity> {


    public ScheduleJobInfo createScheduleJobInfo(ScheduleJobInfo scheduleJobInfo) {
        ScheduleJobInfoEntity scheduleJobInfoEntity = ScheduleJobInfoConverter.INSTANCE.modelToEntity(scheduleJobInfo);
        this.save(scheduleJobInfoEntity);
        return ScheduleJobInfoConverter.INSTANCE.entityToModel(scheduleJobInfoEntity);
    }

    public ScheduleJobInfo getScheduleJobInfo(String jobKey) {
        ScheduleJobInfoEntity scheduleJobInfoEntity = this.getById(jobKey);
        if(scheduleJobInfoEntity==null){
            throw ResourceException.buildNotExistException(this.getClass(),"job not exist, jobKey:" + jobKey);
        }
        return ScheduleJobInfoConverter.INSTANCE.entityToModel(scheduleJobInfoEntity);
    }

    public boolean updateScheduleJobStatus(String jobKey, DataStatus jobStatus) {
        ScheduleJobInfoEntity scheduleJobInfoEntity = this.getById(jobKey);
        if(scheduleJobInfoEntity==null){
            throw ResourceException.buildNotExistException(this.getClass(),"job not exist, jobKey:" + jobKey);
        }
        UpdateWrapper<ScheduleJobInfoEntity> uw = new UpdateWrapper<>();
        uw.lambda().eq(ScheduleJobInfoEntity::getJobKey, jobKey)
                .set(ScheduleJobInfoEntity::getJobStatus, jobStatus);
        return this.update(uw);
    }

    public boolean deleteScheduleJobInfo(String jobKey) {
        ScheduleJobInfoEntity scheduleJobInfoEntity = this.getById(jobKey);
        if(scheduleJobInfoEntity==null){
            throw ResourceException.buildNotExistException(this.getClass(),"job not exist, jobKey:" + jobKey);
        }
        return this.removeById(jobKey);
    }

    public ScheduleJobInfo updateScheduleJobInfo(ScheduleJobInfo scheduleJobInfo) {
        this.updateById(ScheduleJobInfoConverter.INSTANCE.modelToEntity(scheduleJobInfo));
        return scheduleJobInfo;
    }

    public PageBody<ScheduleJobInfo> pageScheduleJobInfo(int page, int pageSize, JobType jobType, DataStatus jobStatus, ScheduleMode scheduleMode) {
        Page<ScheduleJobInfoEntity> pageScheduleJobInfo = this.page(new Page<>(page, pageSize), new QueryWrapper<ScheduleJobInfoEntity>()
                .lambda()
                .eq(jobType!=null,ScheduleJobInfoEntity::getJobType, jobType)
                .eq(jobStatus!=null,ScheduleJobInfoEntity::getJobStatus, jobStatus)
                .eq(scheduleMode!=null,ScheduleJobInfoEntity::getScheduleMode, scheduleMode));

        PageBody<ScheduleJobInfo> pageBody = new PageBody<>();
        pageBody.setTotal(pageScheduleJobInfo.getTotal());
        pageBody.setPageSize(pageScheduleJobInfo.getSize());
        pageBody.setList(ScheduleJobInfoConverter.INSTANCE.entitiesToModels(pageScheduleJobInfo.getRecords()));
        pageBody.setCurrent(pageScheduleJobInfo.getCurrent());
        pageBody.setTotalPage(pageScheduleJobInfo.getPages());

        return pageBody;
    }

}
