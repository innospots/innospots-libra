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

package io.innospots.schedule.quartz;

import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.ServiceActionHolder;
import io.innospots.schedule.utils.ScheduleUtils;
import io.innospots.schedule.queue.IReadyJobQueue;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * time quartz scheduler
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/8
 */
@Slf4j
public class QuartzJobScheduler implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (ScheduleUtils.isExecutorLeader()) {
            JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
            log.info("schedule job is：{}, push to queue", jobKey.getName());
            IReadyJobQueue readyJobQueue = BeanContextAwareUtils.getBean(IReadyJobQueue.class);
            //push ready execute job to queue
            readyJobQueue.push(jobKey.getName());
            log.info("schedule job {} execute end", jobKey.getName());
        } else {
            log.info("service is not leader, not execute job:{}", jobExecutionContext.getJobDetail().getKey());
        }
    }
}
