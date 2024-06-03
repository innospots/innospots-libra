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

package io.innospots.schedule.dispatch;

import io.innospots.schedule.converter.ReadyJobConverter;
import io.innospots.schedule.dao.ReadyJobDao;
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.ReadyJob;
import io.innospots.schedule.queue.IReadyJobQueue;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
public class ReadJobDispatcher {


    private final IReadyJobQueue readyJobQueue;

    private final JobExecutionExplorer jobExecutionExplorer;

    private final ReadyJobDao readyJobDao;

    public ReadJobDispatcher(IReadyJobQueue readyJobQueue, JobExecutionExplorer jobExecutionExplorer,ReadyJobDao readyJobDao) {
        this.readyJobQueue = readyJobQueue;
        this.jobExecutionExplorer = jobExecutionExplorer;
        this.readyJobDao = readyJobDao;
    }

    public int stop(String jobExecutionId, String message) {
        return jobExecutionExplorer.stop(jobExecutionId, message);
    };

    public int continueDispatch(String jobExecutionId) {
        List<JobExecution> continueExecutions = jobExecutionExplorer.continueExecution(jobExecutionId);
        if (CollectionUtils.isNotEmpty(continueExecutions)) {
            for (JobExecution execution : continueExecutions) {
                ReadyJob readyJob = ReadyJobConverter.build(execution);
                dispatch(readyJob);
            }
            return continueExecutions.size();
        }
        return 0;
    }

    public int retryDispatch(String jobExecutionId) {
        List<JobExecution> retryExecutions = jobExecutionExplorer.retryExecution(jobExecutionId);
        if (CollectionUtils.isNotEmpty(retryExecutions)) {
            for (JobExecution execution : retryExecutions) {
                ReadyJob readyJob = ReadyJobConverter.build(execution);
                dispatch(readyJob);
            }
            return retryExecutions.size();
        }

        return 0;
    }

    /**
     * execute job by jobKey, and set external params
     *
     * @param jobKey
     * @param params
     */
    public void dispatch(String jobKey, Map<String, Object> params) {
        readyJobQueue.push(jobKey, params);
    }

    public void dispatch(String parentExecutionId, Integer sequenceNumber, String jobKey, Map<String, Object> params) {
        readyJobQueue.push(parentExecutionId, sequenceNumber, jobKey, params);
    }

    public void dispatch(ReadyJob readyJob) {
        readyJobQueue.push(readyJob);
    }

    public int cancel(String jobKey) {
        return readyJobQueue.cancelJob(jobKey);
    }

    public List<String> readNonExecuteJobKeys(String parentExecutionId) {
        return readyJobDao.selectNonExecuteJobKeysByParentExecutionId(parentExecutionId);
    }
}
