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

    public JobExecutionWatcher(JobExecutionExplorer jobExecutionExplorer) {
        this.jobExecutionExplorer = jobExecutionExplorer;
    }

    @Override
    public int execute() {
        if (!ScheduleUtils.isExecutorLeader()) {
            return checkIntervalSecond;
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
                processDoneJobs(jobExecution);
                checkParentExecution(jobExecution);
            }
        }//end if

        return checkIntervalSecond;
    }

    /**
     * update parent job execution, percent, successCount, failCount and status
     *
     * @param jobExecution
     */
    private void processExecutingParentJob(JobExecution jobExecution) {
        if (jobExecution.getParentExecutionId() != null) {
            List<ExecutionStatus> subStatus = jobExecutionExplorer.subJobExecutionStatus(jobExecution.getParentExecutionId());
            Long successCount = subStatus.stream().filter(i -> i == ExecutionStatus.COMPLETE).count();
            Long failCount = subStatus.stream().filter(i -> i == ExecutionStatus.FAILED).count();
            Long doneCount = subStatus.stream().filter(ExecutionStatus::isDone).count();
            Integer percent = Long.bitCount(doneCount * 100 / jobExecution.getSubJobCount());
            String message = null;
            ExecutionStatus status = null;

            if (failCount > 0) {
                status = ExecutionStatus.FAILED;
                message = "sub job is failed.";
            } else if (Objects.equals(doneCount, jobExecution.getSubJobCount())) {
                status = ExecutionStatus.COMPLETE;
                message = "success";
            }

            jobExecutionExplorer.updateJobExecution(jobExecution.getExecutionId(), percent
                    , successCount, failCount, status, message);
        }

    }

    private void processDoneJobs(JobExecution jobExecution) {
        //TODO to be tested
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
     *
     * @param jobExecution
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
