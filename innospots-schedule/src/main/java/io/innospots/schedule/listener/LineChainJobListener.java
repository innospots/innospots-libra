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

package io.innospots.schedule.listener;

import io.innospots.base.events.IEventListener;
import io.innospots.base.model.Pair;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.events.JobExecutionEvent;
import io.innospots.schedule.job.LineChainJob;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.operator.JobExecutionOperator;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/2
 */
public class LineChainJobListener implements IEventListener<JobExecutionEvent> {

    private ReadJobDispatcher readJobDispatcher;

    private JobExecutionOperator jobExecutionOperator;

    @Override
    public Object listen(JobExecutionEvent event) {
        JobExecution jobExecution = event.jobExecution();
        if (jobExecution.getJobType() == JobType.LINE_CHAIN) {
            List<Pair<String, ExecutionStatus>> pairs = jobExecutionOperator.subJobExecutionStatusPair(jobExecution.getExecutionId());
            List<String> chainKeys = LineChainJob.getChainJobKeys(jobExecution);

        }
        return null;
    }
}
