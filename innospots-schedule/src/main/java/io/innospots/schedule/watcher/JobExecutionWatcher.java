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
import io.innospots.schedule.config.ScheduleConstant;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.events.JobExecutionEvent;
import io.innospots.schedule.events.JobQueueEvent;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.operator.JobExecutionOperator;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * schedule service watcher
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
public class JobExecutionWatcher extends AbstractWatcher {

    private JobExecutionOperator jobExecutionOperator;

    public JobExecutionWatcher(JobExecutionOperator jobExecutionOperator) {
        this.jobExecutionOperator = jobExecutionOperator;
    }

    @Override
    public int execute() {
        List<JobExecution> jobExecutions = jobExecutionOperator.fetchExecutingJobs();
        if (CollectionUtils.isNotEmpty(jobExecutions)) {
            for (JobExecution jobExecution : jobExecutions) {
                if (isTimeout(jobExecution)) {
                    jobExecutionOperator.updateTimeoutExecution(jobExecution);
                    continue;
                }
            }//end for
        }//end if

        jobExecutions = jobExecutionOperator.fetchRecentDoneJobs();
        if (CollectionUtils.isNotEmpty(jobExecutions)) {
            for (JobExecution jobExecution : jobExecutions) {
                processDoneJobs(jobExecution);
                checkParentExecution(jobExecution);
            }
        }//end if

        return checkIntervalSecond;
    }

    private void processExecutingParentJob(JobExecution jobExecution){
        if(jobExecution.getParentExecutionId()!=null){
            List<ExecutionStatus> subStatus = jobExecutionOperator.subJobExecutionStatus(jobExecution.getParentExecutionId());
            //TODO update percent
        }

    }

    private void processDoneJobs(JobExecution jobExecution) {
        processDoneParentJob(jobExecution);
        processDoneChildrenJob(jobExecution);
    }

    private void processDoneParentJob(JobExecution jobExecution) {
        if (jobExecution.getSubJobCount() != null && jobExecution.getSubJobCount() > 0) {
            if (jobExecution.getStatus() == ExecutionStatus.STOPPED ||
                    jobExecution.getStatus() == ExecutionStatus.FAILED) {
                //cancel job in the queue
                JobQueueEvent queueEvent = new JobQueueEvent(jobExecution.getExecutionId());
                queueEvent.setMessageStatus(MessageStatus.CANCEL);
                EventBusCenter.postSync(queueEvent);
                //stop sub job execution
                jobExecutionOperator.stopSubJobExecutions(jobExecution.getExecutionId());
            }
        }
    }

    private void processDoneChildrenJob(JobExecution jobExecution) {
        if (jobExecution.getParentExecutionId() != null) {
            if (jobExecution.getStatus() == ExecutionStatus.FAILED) {
                JobExecution parentJobExecution = jobExecutionOperator.jobExecution(jobExecution.getParentExecutionId());
                if(parentJobExecution.getStatus().isExecuting()){
                    jobExecutionOperator.fail(jobExecution.getParentExecutionId(), "sub job is failed.");
                }
            }
        }
    }

    private boolean isTimeout(JobExecution jobExecution) {
        Integer timeout = jobExecution.getInteger(ScheduleConstant.PARAM_TIMEOUT_SECOND);
        if (timeout == null) {
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
            JobExecution parentJobExecution = jobExecutionOperator.jobExecution(jobExecution.getParentExecutionId());
            long subCount = jobExecutionOperator.countSubJobExecutions(jobExecution.getParentExecutionId());
            if (parentJobExecution != null && jobExecution.getSubJobCount() > subCount) {
                //check parent job execution status
                EventBusCenter.postSync(new JobExecutionEvent(parentJobExecution));
            }
        }

    }
}
