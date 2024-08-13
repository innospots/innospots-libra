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
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.converter.JobExecutionConverter;
import io.innospots.schedule.dao.JobExecutionDao;
import io.innospots.schedule.entity.JobExecutionEntity;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.exception.JobExecutionException;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.innospots.schedule.dao.JobExecutionDao.buildUpdateWrapper;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class JobExecutionExplorer {

    private final JobExecutionDao jobExecutionDao;


    public JobExecutionExplorer(JobExecutionDao jobExecutionDao) {
        this.jobExecutionDao = jobExecutionDao;
    }

    public JobExecution jobExecution(String jobExecutionId) {
        return JobExecutionConverter.INSTANCE
                .entityToModel(jobExecutionDao.selectById(jobExecutionId));
    }


    /**
     * stopping the job, and waiting the job execution update to stopped
     * with a given ID and message.
     */
    public int stop(String jobExecutionId, String message) {
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPING)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .set(JobExecutionEntity::getMessage, message)
                .in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.executingStatus())
                .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
        this.jobExecutionDao.update(uw);
        int count = 0;
        JobExecutionEntity entity = null;
        do {
            //waiting stop executing job in the executor service
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            count++;
            entity = jobExecutionDao.selectById(jobExecutionId);
            ExecutionStatus executionStatus = ExecutionStatus.valueOf(entity.getExecutionStatus());
            if (executionStatus.isDone()) {
                break;
            }
        } while (count < 3 && ExecutionStatus.valueOf(entity.getExecutionStatus()).isStopping());
        if (count >= 3 || ExecutionStatus.valueOf(entity.getExecutionStatus()).isStopping()) {
            log.warn("job can't be set stopped, manual set stopped,{}", jobExecutionId);
            this.jobExecutionDao.update(buildUpdateWrapper(jobExecutionId, ExecutionStatus.STOPPED, message));
            return 0;
        }
        return 1;
    }

    /**
     * manual update complete status
     *
     * @param jobExecutionId
     * @param message
     */
    public int complete(String jobExecutionId, String message) {
        return this.jobExecutionDao.update(buildUpdateWrapper(jobExecutionId, ExecutionStatus.COMPLETE, message));
    }

    public int fail(String jobExecutionId, String message) {
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.FAILED)
                .set(message != null, JobExecutionEntity::getMessage, message)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.executingStatus())
                .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
        return jobExecutionDao.update(uw);
    }

    /**
     * continue to execute the job, if the status is stopped
     *
     * @param jobExecutionId
     * @return
     */
    public List<JobExecution> retryExecution(String jobExecutionId) {
        JobExecutionEntity entity = this.jobExecutionDao.selectById(jobExecutionId);
        if (ExecutionStatus.valueOf(entity.getExecutionStatus()) != ExecutionStatus.FAILED) {
            throw new JobExecutionException(this.getClass(), ResponseCode.DATA_OPERATION_ERROR, "job can be executed in failed status. ", jobExecutionId);
        }
        List<JobExecutionEntity> entities = new ArrayList<>();
        if (JobType.valueOf(entity.getJobType()).isJobContainer()) {
            UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
            uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.RETRY_RUNNING)
                    .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                    .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
            this.jobExecutionDao.update(uw);

            //has sub job execution
            QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(JobExecutionEntity::getParentExecutionId, jobExecutionId)
                    .in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPED.name(), ExecutionStatus.FAILED.name());
            //sub job will retry to execute
            entities = this.jobExecutionDao.selectList(qw);
            if (CollectionUtils.isNotEmpty(entities)) {
                List<String> subs = entities.stream().
                        map(JobExecutionEntity::getExecutionId).collect(Collectors.toList());
                //sub job executions
                for (JobExecutionEntity jobExecutionEntity : entities) {
                    if (ExecutionStatus.valueOf(jobExecutionEntity.getExecutionStatus()) == ExecutionStatus.FAILED) {
                        this.jobExecutionDao.update(
                                new UpdateWrapper<JobExecutionEntity>().lambda()
                                        .set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.RETRYING)
                                        .eq(JobExecutionEntity::getExecutionId, jobExecutionEntity.getExecutionId()));
                    } else if (ExecutionStatus.valueOf(jobExecutionEntity.getExecutionStatus()) == ExecutionStatus.STOPPED) {
                        this.jobExecutionDao.update(
                                new UpdateWrapper<JobExecutionEntity>().lambda()
                                        .set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.CONTINUING)
                                        .eq(JobExecutionEntity::getExecutionId, jobExecutionEntity.getExecutionId()));
                    }
                }
                log.info("retry to execute sub job executions: {}", subs);
            }
        } else {
            //execute sub job execution directly, only execute selected sub job execution
            UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
            uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.RETRYING)
                    .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                    .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
            this.jobExecutionDao.update(uw);
            entities.add(entity);
            log.info("continue to execute job execution: {}", entity.getExecutionId());
        }
        return JobExecutionConverter.INSTANCE.entitiesToModels(entities);
    }

    /**
     * continue to execute the job, if the status is stopped
     *
     * @param jobExecutionId
     * @return
     */
    public List<JobExecution> continueExecution(String jobExecutionId) {
        JobExecutionEntity entity = this.jobExecutionDao.selectById(jobExecutionId);
        if (ExecutionStatus.valueOf(entity.getExecutionStatus()) != ExecutionStatus.STOPPED) {
            throw new JobExecutionException(this.getClass(), ResponseCode.DATA_OPERATION_ERROR, "job can be executed in stopped status. ", jobExecutionId);
        }
        List<JobExecutionEntity> entities = new ArrayList<>();
        if (JobType.valueOf(entity.getJobType()).isJobContainer()) {
            UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
            uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.CONTINUE_RUNNING)
                    .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                    .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
            this.jobExecutionDao.update(uw);

            //has sub job execution
            QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
            qw.lambda().eq(JobExecutionEntity::getParentExecutionId, jobExecutionId)
                    .eq(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPED);
            //sub job will continue to execute
            entities = this.jobExecutionDao.selectList(qw);
            if (CollectionUtils.isNotEmpty(entities)) {
                List<String> subs = entities.stream().
                        map(JobExecutionEntity::getExecutionId).collect(Collectors.toList());
                //sub job executions
                this.jobExecutionDao.update(
                        new UpdateWrapper<JobExecutionEntity>().lambda()
                                .set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.CONTINUING)
                                .eq(JobExecutionEntity::getExecutionId, subs));
                log.info("continue to execute sub job executions: {}", subs);
            }
        } else {
            //execute sub job execution directly, only execute selected sub job execution
            UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
            uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.CONTINUING)
                    .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                    .eq(JobExecutionEntity::getExecutionId, jobExecutionId);
            this.jobExecutionDao.update(uw);
            entities.add(entity);
            log.info("continue to execute job execution: {}", entity.getExecutionId());
        }
        return JobExecutionConverter.INSTANCE.entitiesToModels(entities);
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
        if (jobExecutionEntity.getOriginExecutionId() != null) {
            JobExecutionEntity originJobExecutionEntity = jobExecutionDao.selectOne(new QueryWrapper<JobExecutionEntity>()
                    .lambda().eq(JobExecutionEntity::getExecutionId, jobExecutionEntity.getOriginExecutionId()));
            if (ExecutionStatus.valueOf(originJobExecutionEntity.getExecutionStatus()) == ExecutionStatus.CONTINUING) {
                return createContinueJobExecution(originJobExecutionEntity);
            } else if (ExecutionStatus.valueOf(originJobExecutionEntity.getExecutionStatus()) == ExecutionStatus.RETRYING) {
                return createRetryJobExecution(jobExecutionEntity);
            }
        } else {
            jobExecutionDao.insert(jobExecutionEntity);
        }
        return JobExecutionConverter.INSTANCE.entityToModel(jobExecutionEntity);
    }

    /**
     * set original job execution status to continue running
     *
     * @param originJobExecutionEntity
     * @return
     */
    private JobExecution createContinueJobExecution(JobExecutionEntity originJobExecutionEntity) {
        //continue execute
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.CONTINUE_RUNNING)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .eq(JobExecutionEntity::getExecutionId, originJobExecutionEntity.getExecutionId());
        jobExecutionDao.update(uw);
        return JobExecutionConverter.INSTANCE.entityToModel(originJobExecutionEntity);
    }

    private JobExecution createRetryJobExecution(JobExecutionEntity jobExecutionEntity) {
        jobExecutionEntity.setExecutionStatus(ExecutionStatus.RETRY_RUNNING.name());
        //retry execute
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getSequenceNumber, -1*jobExecutionEntity.getSequenceNumber())
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .eq(JobExecutionEntity::getExecutionId, jobExecutionEntity.getOriginExecutionId());
        jobExecutionDao.insert(jobExecutionEntity);
        return JobExecutionConverter.INSTANCE.entityToModel(jobExecutionEntity);
    }

    @Transactional
    public void endJobExecution(JobExecution jobExecution) {
        JobExecutionEntity jobExecutionEntity = jobExecutionDao.selectById(jobExecution.getExecutionId());

        jobExecutionEntity.setEndTime(jobExecution.getEndTime());
        jobExecutionEntity.setFailCount(jobExecution.getFailCount());
        jobExecutionEntity.setPercent(jobExecution.getPercent());
        jobExecutionEntity.setSelfEndTime(jobExecution.getSelfEndTime());
        jobExecutionEntity.setSuccessCount(jobExecution.getSuccessCount());
        jobExecutionEntity.setMessage(jobExecution.getMessage());

        if (ExecutionStatus.valueOf(jobExecutionEntity.getExecutionStatus()) == ExecutionStatus.STOPPING) {
            jobExecutionEntity.setExecutionStatus(ExecutionStatus.STOPPED.name());
        } else {
            jobExecutionEntity.setExecutionStatus(jobExecution.getExecutionStatus().name());
        }
        int cnt = jobExecutionDao.updateById(jobExecutionEntity);
        log.debug("update result:{}, executionId:{}",cnt,jobExecution.getExecutionId());
    }

    public int updateJobExecution(JobExecution jobExecution){
        JobExecutionEntity jobExecutionEntity = JobExecutionConverter.INSTANCE.modelToEntity(jobExecution);
        return jobExecutionDao.updateById(jobExecutionEntity);
    }

    public int updateJobExecution(String jobExecutionId,
                                  Integer percent,
                                  Long successCount,
                                  Long failCount,
                                  LocalDateTime endTime,
                                  ExecutionStatus status, String message) {
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(status != null, JobExecutionEntity::getExecutionStatus, status)
                .set(percent != null, JobExecutionEntity::getPercent, percent)
                .set(JobExecutionEntity::getSuccessCount, successCount)
                .set(JobExecutionEntity::getFailCount, failCount)
                .set(message != null, JobExecutionEntity::getMessage, message)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .set(endTime!=null,JobExecutionEntity::getEndTime, endTime)
                .set(endTime!=null,JobExecutionEntity::getSelfEndTime, endTime)
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
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.doneStatus())
                .ge(JobExecutionEntity::getUpdatedTime, LocalDateTime.now().minusMinutes(1));
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
     */
    public void stopSubJobExecutions(String parentExecutionId) {
        UpdateWrapper<JobExecutionEntity> qw = new UpdateWrapper<>();
        qw.lambda().set(JobExecutionEntity::getExecutionStatus, ExecutionStatus.STOPPED)
                .set(JobExecutionEntity::getUpdatedTime, LocalDateTime.now())
                .eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .in(JobExecutionEntity::getExecutionStatus, ExecutionStatus.executingStatus());
        this.jobExecutionDao.update(qw);
    }

    public long countSubJobExecutions(String parentExecutionId, ExecutionStatus... executionStatus) {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        List<ExecutionStatus> statuses = null;
        if (ArrayUtils.isNotEmpty(executionStatus)) {
            statuses = Arrays.asList(executionStatus);
        }
        qw.lambda().eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .ge(JobExecutionEntity::getSequenceNumber, 0)
                .in(statuses != null, JobExecutionEntity::getExecutionStatus, statuses);
        return this.jobExecutionDao.selectCount(qw);
    }

    public List<ExecutionStatus> subJobExecutionStatus(String parentExecutionId) {
        List<JobExecutionEntity> entities = parentExecutions(parentExecutionId);
        return entities.stream().map(JobExecutionEntity::getExecutionStatus).map(ExecutionStatus::valueOf).collect(Collectors.toList());
    }

    public List<Pair<String, ExecutionStatus>> subJobExecutionStatusPair(String parentExecutionId) {
        List<JobExecutionEntity> entities = parentExecutions(parentExecutionId);
        return entities.stream().map(e -> Pair.of(e.getJobKey(), ExecutionStatus.valueOf(e.getExecutionStatus()))).collect(Collectors.toList());
    }

    private List<JobExecutionEntity> parentExecutions(String parentExecutionId) {
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(JobExecutionEntity::getParentExecutionId, parentExecutionId)
                .ge(JobExecutionEntity::getSequenceNumber, 0)
                .select(JobExecutionEntity::getExecutionStatus, JobExecutionEntity::getExecutionId,JobExecutionEntity::getJobKey);
        return this.jobExecutionDao.selectList(qw);
    }


}
