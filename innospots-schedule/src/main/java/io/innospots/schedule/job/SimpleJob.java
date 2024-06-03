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

package io.innospots.schedule.job;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class SimpleJob extends BaseJob {

    public static final String PARAM_SLEEP_TIME = "job.sleep.time";

    public SimpleJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public InnospotsResponse<Map<String,Object>> execute() {
        InnospotsResponse<Map<String,Object>> response = new InnospotsResponse<>();
        Integer sleepTime = this.jobExecution.getInteger(PARAM_SLEEP_TIME);
        if(sleepTime == null){
            sleepTime = 100;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
            log.info("execute simple job, sleep:{}, {}",sleepTime,jobExecution);
        } catch (InterruptedException e) {
            log.error(e.getMessage()+", info:"+jobExecution.info(),e);
        }
        response.setMessage("sleep time:"+ sleepTime);
        return response;
    }

}
