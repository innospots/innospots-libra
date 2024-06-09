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

package io.innospots.workflow.schedule.listener;

import io.innospots.base.events.IEventListener;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.events.JobExecutionEvent;
import io.innospots.schedule.model.JobExecution;
import io.innospots.workflow.schedule.flow.FlowJobExecutor;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
public class FlowJobScheduleListener implements IEventListener<JobExecutionEvent> {

    private FlowJobExecutor flowJobExecutor;

    public FlowJobScheduleListener(FlowJobExecutor flowJobExecutor) {
        this.flowJobExecutor = flowJobExecutor;
    }

    @Override
    public Object listen(JobExecutionEvent event) {
        JobExecution jobExecution = event.jobExecution();
        if (jobExecution.getJobType() != JobType.FLOW) {
            //only process flow job
            flowJobExecutor.executeNodeJob(jobExecution);
        }
        return null;
    }
}
