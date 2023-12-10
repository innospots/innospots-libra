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

package io.innospots.schedule.launcher;

import cn.hutool.core.exceptions.ExceptionUtil;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.job.JobBuilder;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.schedule.queue.ReadyJobDbQueue;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class JobLauncher {

    private JobExecutionOperator jobExecutionOperator;

    private ScheduleJobInfoOperator scheduleJobInfoOperator;

    private ReadyJobDbQueue readyJobDbQueue;

    private WeakHashMap<String, JobExecution> executionCache = new WeakHashMap<>();

    private ThreadTaskExecutor threadTaskExecutor;

    public int currentJobCount() {
        return threadTaskExecutor.getActiveCount();
    }

    public void launch(ReadyJobEntity readyJobEntity) {
        Future future = threadTaskExecutor.submit(()-> {
            JobExecution jobExecution = start(readyJobEntity);
            execute(jobExecution);
            end(jobExecution);
        });
    }

    protected void execute(JobExecution jobExecution) {
        try {
//            ScheduleJobInfo scheduleJobInfo = scheduleJobInfoOperator.getScheduleJobInfo(jobExecution.getKey());
            BaseJob baseJob = JobBuilder.build(jobExecution);
            baseJob.execute(jobExecution);
            if (jobExecution.getJobType() == JobType.EXECUTE) {
                jobExecution.setEndTime(LocalDateTime.now());
                jobExecution.setStatus(ExecutionStatus.COMPLETE);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            jobExecution.setStatus(ExecutionStatus.FAILED);
            jobExecution.setMessage(ExceptionUtil.stacktraceToString(e, 1024));
        }
    }

    protected JobExecution start(ReadyJobEntity readyJobEntity) {
        JobExecution jobExecution = jobExecutionOperator.createJobExecution(readyJobEntity);
        readyJobDbQueue.ackRead(readyJobEntity.getJobReadyKey());
        executionCache.put(jobExecution.getExecutionId(), jobExecution);
        return jobExecution;
    }

    protected void end(JobExecution jobExecution) {
        executionCache.remove(jobExecution.getExecutionId());
        jobExecutionOperator.updateJobExecution(jobExecution);
    }
}
