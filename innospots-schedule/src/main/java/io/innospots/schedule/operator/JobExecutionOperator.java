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
import io.innospots.schedule.model.ExecutionFormQuery;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.JobExecutionDisplay;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.innospots.schedule.dao.JobExecutionDao.buildUpdateWrapper;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
public class JobExecutionOperator extends ServiceImpl<JobExecutionDao, JobExecutionEntity> {


    public PageBody<JobExecution> pageJobExecutions(ExecutionFormQuery formQuery
                                                    ) {
        PageBody<JobExecution> pageBody = new PageBody<>();

        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(formQuery.getJobKey() != null, JobExecutionEntity::getJobKey, formQuery.getJobKey())
                .eq(formQuery.getJobName() != null, JobExecutionEntity::getJobName, formQuery.getJobName())
                .eq(formQuery.getServerKey() != null, JobExecutionEntity::getServerKey, formQuery.getServerKey())
                .eq(formQuery.getCreatedBy() != null, JobExecutionEntity::getCreatedBy, formQuery.getCreatedBy())
                .eq(formQuery.getStatus() != null, JobExecutionEntity::getExecutionStatus, formQuery.getStatus())
                .eq(formQuery.getScopes() != null, JobExecutionEntity::getScopes, formQuery.getScopes())
                .ge(formQuery.getStartTime() != null, JobExecutionEntity::getStartTime, formQuery.getStartTime())
                .le(formQuery.getEndTime() != null, JobExecutionEntity::getEndTime, formQuery.getEndTime());
        Page<JobExecutionEntity> entityPage = new Page<>(formQuery.getPage(), formQuery.getSize());
        entityPage = this.page(entityPage, qw);
        pageBody.setTotal(entityPage.getTotal());
        pageBody.setTotalPage(entityPage.getPages());
        pageBody.setPageSize((long) formQuery.getSize());
        pageBody.setList(JobExecutionConverter.INSTANCE.entitiesToModels(entityPage.getRecords()));

        return pageBody;
    }

    public JobExecutionDisplay getJobExecution(String jobExecutionId, boolean includeSub) {
        JobExecutionDisplay jobExecutionDisplay = new JobExecutionDisplay();
        JobExecutionConverter.entityToDisplay(this.getById(jobExecutionId),jobExecutionDisplay);
        if (jobExecutionDisplay != null && jobExecutionDisplay.getJobType().isJobContainer()) {
            fillSubExecutions(jobExecutionDisplay);
        }
        return jobExecutionDisplay;
    }

    private void fillSubExecutions(JobExecutionDisplay jobExecutionDisplay) {
        if (jobExecutionDisplay.getParentExecutionId() == null) {
            return;
        }
        QueryWrapper<JobExecutionEntity> pqw = new QueryWrapper<>();
        pqw.lambda().eq(JobExecutionEntity::getParentExecutionId, jobExecutionDisplay.getExecutionId());
        List<JobExecutionEntity> subExecutions = this.list(pqw);
        List<JobExecutionDisplay> subDisplays = JobExecutionConverter.entitiesToDisplays(subExecutions);
        if (CollectionUtils.isNotEmpty(subDisplays)) {
            for (JobExecutionDisplay subDisplay : subDisplays) {
                if (subDisplay.getJobType().isJobContainer()) {
                    fillSubExecutions(subDisplay);
                }
            }
            jobExecutionDisplay.setSubExecutions(subDisplays);
        }

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
                .set(successCount != null, JobExecutionEntity::getSuccessCount, successCount)
                .set(failCount != null, JobExecutionEntity::getFailCount, failCount)
                .set(message != null, JobExecutionEntity::getMessage, message)
                .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
        return this.update(uw);
    }

    public boolean updatePercent(String jobExecutionId,
                                 Integer percent,
                                 Long subJobCount,
                                 Long successCount,
                                 Long failCount) {
        return updateJobExecution(jobExecutionId,percent,subJobCount,successCount,failCount,null,null);
    }


    public boolean updateStatus(String jobExecutionId,
                                ExecutionStatus status, String message) {
        return updateJobExecution(jobExecutionId,null,null,null,null,status,message);
    }


}
