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

import io.innospots.schedule.model.ReadyJob;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import io.innospots.schedule.queue.IReadyJobQueue;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
@Component
public class ReadJobDispatcher {


    private final IReadyJobQueue readyJobQueue;


    public ReadJobDispatcher(IReadyJobQueue readyJobQueue) {
        this.readyJobQueue = readyJobQueue;
    }

    /**
     * execute job by jobKey, and set external params
     *
     * @param jobKey
     * @param params
     */
    public void execute(String jobKey, Map<String, Object> params) {
        readyJobQueue.push(jobKey, params);
    }

    public void execute(String parentExecutionId, Integer sequenceNumber, String jobKey, Map<String, Object> params) {
        readyJobQueue.push(parentExecutionId, sequenceNumber, jobKey, params);
    }

    public void execute(ReadyJob readyJob) {
        readyJobQueue.push(readyJob);
    }
}
