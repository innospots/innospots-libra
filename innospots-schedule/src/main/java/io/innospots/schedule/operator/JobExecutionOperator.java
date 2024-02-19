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
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.schedule.converter.JobExecutionConverter;
import io.innospots.schedule.dao.JobExecutionDao;
import io.innospots.schedule.entity.JobExecutionEntity;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.JobExecutionDisplay;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
public class JobExecutionOperator extends ServiceImpl<JobExecutionDao, JobExecutionEntity> {


    public PageBody<JobExecution> pageJobExecutions(long size, long page,
                                                    LocalDateTime startTime,
                                                    LocalDateTime endTime,
                                                    ExecutionStatus status,
                                                    String jobKey,
                                                    String jobName,
                                                    String serverKey,
                                                    String createdBy
                                                    ) {
        PageBody<JobExecution> pageBody = new PageBody<>();

        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(jobKey!=null,JobExecutionEntity::getJobKey, jobKey)
                .eq(jobName!=null,JobExecutionEntity::getJobName, jobName)
                .eq(serverKey!=null,JobExecutionEntity::getServerKey, serverKey)
                .eq(createdBy!=null,JobExecutionEntity::getCreatedBy, createdBy)
                .eq(status!=null,JobExecutionEntity::getExecutionStatus, status)
                .ge(startTime!=null,JobExecutionEntity::getStartTime, startTime)
                .le(endTime!=null,JobExecutionEntity::getEndTime, endTime);
        Page<JobExecutionEntity> entityPage = new Page<>(page,size);
        entityPage = this.page(entityPage,qw);
        pageBody.setTotal(entityPage.getTotal());
        pageBody.setTotalPage(entityPage.getPages());
        pageBody.setPageSize(size);
        pageBody.setList(JobExecutionConverter.INSTANCE.entitiesToModels(entityPage.getRecords()));

        return pageBody;
    }

    public List<JobExecutionDisplay> listJobExecutions(String jobExecutionId) {

        return null;
    }

    public JobExecution jobExecution(String jobExecutionId) {
        return JobExecutionConverter.INSTANCE
                .entityToModel(this.getById(jobExecutionId));
    }


    public void updateJobExecution(JobExecution jobExecution) {
        this.updateById(JobExecutionConverter.INSTANCE.modelToEntity(jobExecution));
    }


    public boolean updateJobExecution(String jobExecutionId,
                                  Integer percent,
                                  Long subJobCount,
                                  Long successCount,
                                  Long failCount,
                                  ExecutionStatus status, String message) {
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(status != null, JobExecutionEntity::getExecutionStatus, status)
                .set(percent != null, JobExecutionEntity::getPercent, percent)
                .set(subJobCount != null, JobExecutionEntity::getSubJobCount, subJobCount)
                .set(JobExecutionEntity::getSuccessCount, successCount)
                .set(JobExecutionEntity::getFailCount, failCount)
                .set(message != null, JobExecutionEntity::getMessage, message)
                .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
        return this.update(uw);
    }


}
