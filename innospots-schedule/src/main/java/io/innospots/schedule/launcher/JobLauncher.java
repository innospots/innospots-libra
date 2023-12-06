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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.schedule.entity.ReadyQueueEntity;
import io.innospots.schedule.entity.ScheduleJobInfoEntity;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.job.JobBuilder;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
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

    private WeakHashMap<String, JobExecution> executionCache = new WeakHashMap<>();

    private ExecutorService executorService;

    public int currentJobCount() {
        return executionCache.size();
    }

    public void launch(ReadyQueueEntity readyQueueEntity) {
        Future future = executorService.submit(()-> {
            JobExecution jobExecution = start(readyQueueEntity);
            execute(jobExecution);
            end(jobExecution);
        });
    }

    protected void execute(JobExecution jobExecution) {
        try {
            ScheduleJobInfo scheduleJobInfo = scheduleJobInfoOperator.getScheduleJobInfo(jobExecution.getJobKey());
            BaseJob baseJob = JobBuilder.build(scheduleJobInfo);
            baseJob.execute(jobExecution);
            if (jobExecution.getJobType() == JobType.EXECUTE) {
                jobExecution.setEndTime(LocalDateTime.now());
                jobExecution.setExecutionStatus(ExecutionStatus.COMPLETE);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            jobExecution.setExecutionStatus(ExecutionStatus.FAILED);
            jobExecution.setMessage(ExceptionUtil.stacktraceToString(e, 1024));
        }
    }

    protected JobExecution start(ReadyQueueEntity readyQueueEntity) {
        JobExecution jobExecution = jobExecutionOperator.createJobExecution(readyQueueEntity);
        executionCache.put(jobExecution.getJobExecutionId(), jobExecution);
        return jobExecution;
    }

    protected void end(JobExecution jobExecution) {
        executionCache.remove(jobExecution.getJobExecutionId());
        jobExecutionOperator.updateJobExecution(jobExecution);
    }
}
