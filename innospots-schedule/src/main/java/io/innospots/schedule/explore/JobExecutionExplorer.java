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

package io.innospots.schedule.explore;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.base.model.Pair;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.schedule.converter.JobExecutionConverter;
import io.innospots.schedule.dao.JobExecutionDao;
import io.innospots.schedule.entity.JobExecutionEntity;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.exception.JobExecutionException;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class JobExecutionExplorer {

    private final JobExecutionDao jobExecutionDao;

    private LocalDateTime lastUpdateTime;

    public JobExecutionExplorer(JobExecutionDao jobExecutionDao) {
        this.jobExecutionDao = jobExecutionDao;
    }

    public JobExecution jobExecution(String jobExecutionId) {
        return JobExecutionConverter.INSTANCE
                .entityToModel(jobExecutionDao.selectById(jobExecutionId));
    }

    /**
     * create job execution to database
     *
     * @param readyJobEntity
     * @return
     */
    public JobExecution createJobExecution(ReadyJobEntity readyJobEntity) {
        //build job execution by ready Job entity
        JobExecutionEntity jobExecutionEntity = JobExecutionConverter.build(readyJobEntity);
        jobExecutionDao.insert(jobExecutionEntity);
        return JobExecutionConverter.INSTANCE.entityToModel(jobExecutionEntity);
    }


    public void fail(String jobExecutionId, String message) {
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.FAILED)
                .set(message != null, JobExecutionEntity::getMessage, message)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.executingStatus())
                .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
        jobExecutionDao.update(uw);
    }

    public void endJobExecution(JobExecution jobExecution) {
        JobExecutionEntity jobExecutionEntity = jobExecutionDao.selectById(jobExecution.getExecutionId());
        jobExecutionEntity.setEndTime(jobExecution.getEndTime());
        jobExecutionEntity.setFailCount(jobExecution.getFailCount());
        jobExecutionEntity.setPercent(jobExecution.getPercent());
        jobExecutionEntity.setSelfEndTime(jobExecution.getSelfEndTime());
        jobExecutionEntity.setSuccessCount(jobExecution.getSuccessCount());
        jobExecutionEntity.setMessage(jobExecution.getMessage());
        jobExecutionEntity.setUpdatedTime(LocalDateTime.now());

        if (jobExecutionEntity.getExecutionStatus() == ExecutionStatus.STOPPING) {
            jobExecutionEntity.setExecutionStatus(ExecutionStatus.STOPPED);
        } else {
            jobExecutionEntity.setExecutionStatus(jobExecution.getExecutionStatus());
        }
        jobExecutionDao.updateById(jobExecutionEntity);
    }


    public int updateJobExecution(String jobExecutionId,
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
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
        return jobExecutionDao.update(uw);
    }

    /**
     * job executions that have status is executing
     *
     * @return
     */
    public List<JobExecution> fetchExecutingJobs() {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.executingStatus());
        List<JobExecutionEntity> entities = jobExecutionDao.selectList(qw);
        return JobExecutionConverter.INSTANCE.entitiesToModels(entities);
    }

    public List<JobExecution> selectJobExecutions(Collection<String> jobExecutionIds) {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().in(JobExecutionEntity::getExecutionId, jobExecutionIds);
        return JobExecutionConverter.INSTANCE.entitiesToModels(jobExecutionDao.selectList(qw));
    }

    /**
     * job executions that have status is done
     *
     * @return
     */
    public List<JobExecution> fetchRecentDoneJobs() {
        if (lastUpdateTime == null) {
            lastUpdateTime = LocalDateTime.now().minusMinutes(1);
        }
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.doneStatus())
                .ge(JobExecutionEntity::getUpdatedTime, lastUpdateTime);
        List<JobExecutionEntity> entities = jobExecutionDao.selectList(qw);
        return JobExecutionConverter.INSTANCE.entitiesToModels(entities);
    }

    /**
     * stopping executions
     *
     * @return
     */
    public List<JobExecution> fetchStoppingJobs() {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPING);
        return JobExecutionConverter.INSTANCE.entitiesToModels(jobExecutionDao.selectList(qw));
    }

    public void updateStoppingTimeoutExecutions() {
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda()
                .set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPED)
                .eq(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPING)
                .le(JobExecutionEntity::getUpdatedTime, LocalDateTime.now().minusSeconds(180));
        int count = jobExecutionDao.update(uw);
        if (count > 0) {
            log.info("stopping execution timeout, update to stopped:{}", count);
        }
    }

    /**
     * set job execution status to failed when timeout
     *
     * @param jobExecution
     */
    public void updateTimeoutExecution(JobExecution jobExecution) {
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.FAILED)
                .set(JobExecutionEntity::getMessage, "job execute timeout")
                .eq(JobExecutionEntity::getExecutionId, jobExecution.getExecutionId());
        this.jobExecutionDao.update(uw);
    }

    public List<JobExecution> continueExecution(String jobExecutionId) {
        JobExecutionEntity entity = this.jobExecutionDao.selectById(jobExecutionId);
        if (entity.getExecutionStatus() != ExecutionStatus.STOPPED) {
            throw new JobExecutionException(this.getClass(), ResponseCode.DATA_OPERATION_ERROR, "job status is not stopped", jobExecutionId);
        }

        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getSequenceNumber, -1)
                .set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.CONTINUE_RUNNING)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
        this.jobExecutionDao.update(uw);
        List<JobExecutionEntity> entities = new ArrayList<>();
        if (entity.getSubJobCount() != null && entity.getSubJobCount() > 0) {
            //has sub job execution
            QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(JobExecutionEntity::getParentExecutionId,jobExecutionId);
            entities = this.jobExecutionDao.selectList(qw);
            if(CollectionUtils.isNotEmpty(entities)){
                this.jobExecutionDao.update(
                        new UpdateWrapper<JobExecutionEntity>().lambda()
                                .set(JobExecutionEntity::getExecutionStatus,ExecutionStatus.CONTINUE_RUNNING)
                                .eq(JobExecutionEntity::getParentExecutionId,entity.getExecutionId())
                );
            }else{
                entities.add(entity);
            }
        } else if (entity.getParentExecutionId() != null) {
            JobExecutionEntity parentExecutionEntity = this.jobExecutionDao.selectById(entity.getParentExecutionId());
            this.jobExecutionDao.update(
                    new UpdateWrapper<JobExecutionEntity>().lambda()
                            .set(JobExecutionEntity::getExecutionStatus,ExecutionStatus.CONTINUE_RUNNING)
                    .eq(JobExecutionEntity::getExecutionId,entity.getParentExecutionId())
            );
            entities.add(entity);
        }else{
            entities.add(entity);
        }
        return JobExecutionConverter.INSTANCE.entitiesToModels(entities);
    }

    /**
     * all sub job executions are completed
     *
     * @param parentExecutionId
     * @return
     */
    public Set<String> completeJobKeys(String parentExecutionId) {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .select(JobExecutionEntity::getJobKey);
        List<JobExecutionEntity> entities = jobExecutionDao.selectList(qw);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptySet();
        }

        return entities.stream().map(JobExecutionEntity::getJobKey).collect(Collectors.toSet());
    }

    /**
     * stop all sub job executions by parent execution id
     *
     * @param parentExecutionId
     * @return
     */
    public int stopSubJobExecutions(String parentExecutionId) {
        UpdateWrapper<JobExecutionEntity> qw = new UpdateWrapper<>();
        qw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPED)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.executingStatus());
        return this.jobExecutionDao.update(qw);
    }

    public long countSubJobExecutions(String parentExecutionId, ExecutionStatus... executionStatus) {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        List<ExecutionStatus> statuses = null;
        if (executionStatus != null) {
            statuses = Arrays.asList(executionStatus);
        }
        qw.lambda().eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .in(statuses != null, JobExecutionEntity::getExecutionStatus, statuses);
        return this.jobExecutionDao.selectCount(qw);
    }

    public List<ExecutionStatus> subJobExecutionStatus(String parentExecutionId) {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .select(JobExecutionEntity::getExecutionStatus, JobExecutionEntity::getExecutionId);
        List<JobExecutionEntity> entities = this.jobExecutionDao.selectList(qw);
        return entities.stream().map(JobExecutionEntity::getExecutionStatus).collect(Collectors.toList());
    }

    public List<Pair<String, ExecutionStatus>> subJobExecutionStatusPair(String parentExecutionId) {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .select(JobExecutionEntity::getExecutionStatus, JobExecutionEntity::getExecutionId);
        List<JobExecutionEntity> entities = this.jobExecutionDao.selectList(qw);
        return entities.stream().map(e -> Pair.of(e.getExecutionId(), e.getExecutionStatus())).collect(Collectors.toList());
    }


}
