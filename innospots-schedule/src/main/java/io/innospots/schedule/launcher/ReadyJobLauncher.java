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
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.job.JobBuilder;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.queue.IReadyJobQueue;
import io.innospots.schedule.queue.ReadyJobDbQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class ReadyJobLauncher {

    private final JobExecutionExplorer jobExecutionExplorer;


    private final IReadyJobQueue readyJobDbQueue;

    private Map<String, JobExecution> executionCache = new ConcurrentHashMap<>();

    private ThreadTaskExecutor threadTaskExecutor;

    private Map<String, Future> threadFutures = new ConcurrentHashMap<>();


    public ReadyJobLauncher(JobExecutionExplorer jobExecutionExplorer,
                            IReadyJobQueue readyJobDbQueue, ThreadTaskExecutor threadTaskExecutor) {
        this.jobExecutionExplorer = jobExecutionExplorer;
        this.readyJobDbQueue = readyJobDbQueue;
        this.threadTaskExecutor = threadTaskExecutor;
    }


    public int runningJobCount() {
        return this.executionCache.size();
    }

    public int currentJobCount() {
        return threadTaskExecutor.getActiveCount();
    }

    public int checkRunningJobs() {
        if (this.executionCache.isEmpty()) {
            return 0;
        }
        List<JobExecution> jobExecutions = jobExecutionExplorer.selectJobExecutions(executionCache.keySet());
        if (CollectionUtils.isNotEmpty(jobExecutions)) {
            for (JobExecution jobExecution : jobExecutions) {
                ExecutionStatus status = jobExecution.getExecutionStatus();
                if (status.isStopping() || status.isDone()) {
                    this.cancelJob(jobExecution.getInstanceKey());
                }
            }
        }

        return runningJobCount();
    }

    /**
     * cancel running job, thread will be interrupted
     *
     * @param instanceKey
     */
    public void cancelJob(String instanceKey) {
        if (threadFutures.containsKey(instanceKey)) {
            threadFutures.get(instanceKey).cancel(true);
        }
    }

    public void launch(ReadyJobEntity readyJobEntity) {
        if (threadFutures.containsKey(readyJobEntity.getJobReadyKey())) {
            log.warn("Job is already running, jobReadyKey: {}", readyJobEntity.getJobReadyKey());
            return;
        }
        Future future = threadTaskExecutor.submit(() -> {
            JobExecution jobExecution = start(readyJobEntity);
            try {
                execute(jobExecution);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                jobExecution.setExecutionStatus(ExecutionStatus.FAILED);
                jobExecution.setMessage(ExceptionUtil.stacktraceToString(e, 1024));
            } finally {
                jobExecution.setSelfEndTime(LocalDateTime.now());
                end(jobExecution);
            }
        });
        threadFutures.put(readyJobEntity.getInstanceKey(), future);
    }

    protected void execute(JobExecution jobExecution) {
        BaseJob baseJob = JobBuilder.build(jobExecution);
        baseJob.prepare();
        baseJob.execute();
        if (jobExecution.getJobType() == JobType.EXECUTE) {
            jobExecution.setEndTime(LocalDateTime.now());
            jobExecution.setExecutionStatus(ExecutionStatus.COMPLETE);
        }
    }

    protected JobExecution start(ReadyJobEntity readyJobEntity) {
        log.info("Start job, jobReadyKey: {}", readyJobEntity.getJobReadyKey());
        JobExecution jobExecution = jobExecutionExplorer.createJobExecution(readyJobEntity);
        readyJobDbQueue.ackRead(readyJobEntity.getJobReadyKey());
        executionCache.put(jobExecution.getExecutionId(), jobExecution);
        return jobExecution;
    }

    protected void end(JobExecution jobExecution) {
        log.info("End job, jobExecutionId: {}", jobExecution.getExecutionId());
        executionCache.remove(jobExecution.getExecutionId());
        jobExecutionExplorer.endJobExecution(jobExecution);
        threadFutures.remove(jobExecution.getInstanceKey());
    }

}
