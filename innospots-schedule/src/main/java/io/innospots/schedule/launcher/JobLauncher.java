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
import io.innospots.schedule.entity.ReadyQueueEntity;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.job.JobBuilder;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.operator.JobExecutionOperator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class JobLauncher {

    private JobExecutionOperator jobExecutionOperator;

    public int currentJobCount(){
        return 0;
    }

    public void launch(ReadyQueueEntity readyQueueEntity){
        JobExecution jobExecution = start(readyQueueEntity);
        execute(jobExecution);
        end(jobExecution);
    }

    protected void execute(JobExecution jobExecution){
        try{
            BaseJob baseJob = JobBuilder.build(jobExecution);
            baseJob.execute(jobExecution);
            jobExecution.setEndTime(LocalDateTime.now());
            jobExecution.setExecutionStatus(ExecutionStatus.COMPLETE);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            jobExecution.setExecutionStatus(ExecutionStatus.FAILED);
            jobExecution.setMessage(ExceptionUtil.stacktraceToString(e,1024));
        }
    }

    protected JobExecution start(ReadyQueueEntity readyQueueEntity){
        return jobExecutionOperator.createJobExecution(readyQueueEntity);
    }

    protected void end(JobExecution jobExecution){
        jobExecutionOperator.updateJobExecution(jobExecution);
    }
}
