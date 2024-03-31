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

package io.innospots.schedule.watcher;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.watcher.AbstractWatcher;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.events.JobExecutionEvent;
import io.innospots.schedule.events.JobQueueEvent;
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.utils.ScheduleUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * schedule service watcher
 * watch job execution status
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
@Slf4j
public class JobExecutionWatcher extends AbstractWatcher {

    private final JobExecutionExplorer jobExecutionExplorer;

    private final Cache<String, String> doneJobExecutionCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    public JobExecutionWatcher(JobExecutionExplorer jobExecutionExplorer) {
        this.jobExecutionExplorer = jobExecutionExplorer;
    }

    @Override
    public int execute() {
        if (!ScheduleUtils.isExecutorLeader()) {
            return checkIntervalMills;
        }

        // fetch executing jobs
        List<JobExecution> jobExecutions = jobExecutionExplorer.fetchExecutingJobs();
        if (CollectionUtils.isNotEmpty(jobExecutions)) {
            for (JobExecution jobExecution : jobExecutions) {
                if (isTimeout(jobExecution)) {
                    //update time out job execution
                    jobExecutionExplorer.updateTimeoutExecution(jobExecution);
                    continue;
                }
                processExecutingParentJob(jobExecution);
            }//end for
        }//end if

        jobExecutions = jobExecutionExplorer.fetchRecentDoneJobs();
        if (CollectionUtils.isNotEmpty(jobExecutions)) {
            for (JobExecution jobExecution : jobExecutions) {
                String cachedExecutionId = doneJobExecutionCache.getIfPresent(jobExecution.getExecutionId());
                if (cachedExecutionId != null) {
                    continue;
                }
                processDoneJobs(jobExecution);
                checkParentExecution(jobExecution);
                doneJobExecutionCache.put(jobExecution.getExecutionId(), jobExecution.getExecutionId());
            }
        }//end if

        return checkIntervalMills;
    }

    /**
     * update parent job execution, percent, successCount, failCount and status
     * only update container job execution
     *
     * @param jobExecution
     */
    private void processExecutingParentJob(JobExecution jobExecution) {
        if (jobExecution.getJobType().isJobContainer()) {
            //fetch sub job execution status
            List<ExecutionStatus> subStatus = jobExecutionExplorer.subJobExecutionStatus(jobExecution.getExecutionId());
            Long successCount = subStatus.stream().filter(i -> i == ExecutionStatus.COMPLETE).count();
            Long failCount = subStatus.stream().filter(i -> i == ExecutionStatus.FAILED).count();
            Long doneCount = subStatus.stream().filter(ExecutionStatus::isDone).count();
            Long subCount = jobExecution.getSubJobCount() != null && jobExecution.getSubJobCount() > 0 ? jobExecution.getSubJobCount() : 0;
            Long executingCount = subStatus.stream().filter(ExecutionStatus::isExecuting).count();

            Long percent = subCount > 0 ? (doneCount * 100 / subCount) : 0;
            String message = null;
            ExecutionStatus status = null;
            LocalDateTime endTime = jobExecution.getEndTime();
            jobExecution.setSuccessCount(successCount);
            jobExecution.setFailCount(failCount);
            jobExecution.setPercent(percent.intValue());

            if (failCount > 0) {
                status = ExecutionStatus.FAILED;
                message = "sub job is failed";
                log.info("container job execution is failed: {}", jobExecution.info());
                endTime = endTime != null ? endTime : LocalDateTime.now();
                jobExecution.setExecutionStatus(status);
                jobExecution.setEndTime(endTime);
                jobExecution.setMessage(message);
            } else if (Objects.equals(doneCount, jobExecution.getSubJobCount())) {
                status = ExecutionStatus.COMPLETE;
                message = "all sub jobs are complete";
                endTime = endTime != null ? endTime : LocalDateTime.now();
                jobExecution.setExecutionStatus(status);
                jobExecution.setEndTime(endTime);
                jobExecution.setMessage(message);
                log.info("container job execution is complete: {}", jobExecution.info());
            }else if(executingCount == 0 && !Objects.equals(doneCount,subCount)){
                //check parent job execution status.
                //the parent job will check job flow execute status, and control next job execution.
                EventBusCenter.postSync(new JobExecutionEvent(jobExecution));
            }

            jobExecutionExplorer.updateJobExecution(jobExecution.getExecutionId(), percent.intValue()
                    , successCount, failCount,endTime, status, message);
        }

    }

    /**
     * @param jobExecution parent job execution
     */
    private void processDoneJobs(JobExecution jobExecution) {
        //processDoneChildrenJob(jobExecution);
        processDoneParentJob(jobExecution);
    }

    private void processDoneParentJob(JobExecution jobExecution) {
        if (jobExecution.getSubJobCount() != null && jobExecution.getSubJobCount() > 0) {
            //if parent job execution status is stopped or failed，then clear sub job in the queue,and stop sub job execution in executing
            if (jobExecution.getExecutionStatus() == ExecutionStatus.STOPPED ||
                    jobExecution.getExecutionStatus() == ExecutionStatus.FAILED) {
                //cancel job in the queue
                JobQueueEvent queueEvent = new JobQueueEvent(jobExecution.getExecutionId());
                queueEvent.setMessageStatus(MessageStatus.CANCEL);
                EventBusCenter.postSync(queueEvent);
                //stop sub job execution
                jobExecutionExplorer.stopSubJobExecutions(jobExecution.getExecutionId());
            }
        }
    }

    /**
     * set parent job execution status is failed if sub job execution status is failed
     *
     * @param jobExecution
     */
    private void processDoneChildrenJob(JobExecution jobExecution) {
        if (jobExecution.getParentExecutionId() != null) {
            //if sub job execution status is failed,then fail parent job execution
            if (jobExecution.getExecutionStatus() == ExecutionStatus.FAILED) {
                JobExecution parentJobExecution = jobExecutionExplorer.jobExecution(jobExecution.getParentExecutionId());
                if (parentJobExecution.getExecutionStatus().isExecuting()) {
                    jobExecutionExplorer.fail(jobExecution.getParentExecutionId(), "sub job is failed.");
                }
            }
        }
    }

    private boolean isTimeout(JobExecution jobExecution) {
        Integer timeout = jobExecution.getInteger(ScheduleUtils.PARAM_TIMEOUT_SECOND);
        if (timeout == null) {
            return false;
        }
        if (jobExecution.getJobType().isJobContainer()) {
            return false;
        }
        LocalDateTime timeoutTime = jobExecution.getStartTime().plusSeconds(timeout);
        return LocalDateTime.now().isAfter(timeoutTime);
    }

    /**
     * check parent job execution and fire execution event
     * ensure that the job execution event processing is idempotent and cannot be executed or processed repeatedly
     *
     * @param jobExecution sub job execution
     */
    private void checkParentExecution(JobExecution jobExecution) {
        if (jobExecution.getParentExecutionId() != null) {
            JobExecution parentJobExecution = jobExecutionExplorer.jobExecution(jobExecution.getParentExecutionId());
            if (parentJobExecution.getExecutionStatus().isExecuting()) {
                long subCount = jobExecutionExplorer.countSubJobExecutions(jobExecution.getParentExecutionId());
                if (parentJobExecution.getSubJobCount() > subCount) {
                    //check parent job execution status.
                    //the parent job will check job flow execute status, and control next job execution.
                    EventBusCenter.postSync(new JobExecutionEvent(parentJobExecution));
                }
            }
        }
    }
}
