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
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.job.LineChainJob;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.utils.ParamParser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/2
 */
@Slf4j
public class LineChainJobListener implements IEventListener<JobExecutionEvent> {

    private final ReadJobDispatcher readJobDispatcher;

    private final JobExecutionExplorer jobExecutionExplorer;

    public LineChainJobListener(ReadJobDispatcher readJobDispatcher,
                                JobExecutionExplorer jobExecutionExplorer) {
        this.readJobDispatcher = readJobDispatcher;
        this.jobExecutionExplorer = jobExecutionExplorer;
    }

    @Override
    public Object listen(JobExecutionEvent event) {
        JobExecution jobExecution = event.jobExecution();
        if (jobExecution.getJobType() == JobType.LINE_CHAIN) {
            List<Pair<String, ExecutionStatus>> pairs = jobExecutionExplorer.subJobExecutionStatusPair(jobExecution.getExecutionId());
            List<String> chainKeys = LineChainJob.getChainJobKeys(jobExecution);
            boolean failOrRunning = pairs.stream().anyMatch(p -> p.getRight() == ExecutionStatus.FAILED || p.getRight() == ExecutionStatus.RUNNING);
            if (failOrRunning) {
                log.warn("The sub-jobs within the current chains have either failed or are still running, preventing the subsequent job in the chain from being initiated. job execution:",jobExecution.info());
                //has fail or running
                return null;
            }
            Set<String> doneJobKeys = pairs.stream().map(Pair::getLeft).collect(Collectors.toSet());
            if (chainKeys.size() == doneJobKeys.size()) {
                log.warn("all sub-jobs have been completed, job execution:{}", jobExecution.info());
                //all complete
                return null;
            }
            for (int i = 0; i < chainKeys.size(); i++) {
                String chainKey = chainKeys.get(i);
                if (!doneJobKeys.contains(chainKey)) {
                    //next job will be executed that push to queue
                    Map<String, Object> prm = ParamParser.getParamMap(jobExecution, LineChainJob.PARAM_EXECUTE_JOB_PARAMS);
                    log.info("execute next job in the chain, jobKey:{}, chainExecution:{}",chainKey,jobExecution.info());
                    readJobDispatcher.execute(jobExecution.getExecutionId(), i+1, chainKey, prm);
                    break;
                }
            }
        }
        return null;
    }
}
