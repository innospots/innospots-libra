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

package io.innospots.workflow.runtime.scheduled;

import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.ServiceActionHolder;
import io.innospots.workflow.runtime.container.ScheduleRuntimeContainer;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * TriggerNodeQuartzJob
 *
 * @author Wren
 * @date 2022/4/8-18:21
 */
@Slf4j
public class TriggerNodeQuartzJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (ServiceActionHolder.isScheduleService()) {
            JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
            log.info("schedule task is trigger：{}", jobKey.getName());
            ScheduleRuntimeContainer scheduleRuntimeContainer = BeanContextAwareUtils.getBean(ScheduleRuntimeContainer.class);
            scheduleRuntimeContainer.runQuartJob(jobKey);
            log.info("schedule task {} execute end", jobKey.getName());
        } else {
            log.info("service is not leader, not execute job:{}", jobExecutionContext.getJobDetail().getKey());
        }
    }
}