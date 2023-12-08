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

package io.innospots.schedule.model;

import io.innospots.base.enums.DataStatus;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.base.utils.Initializer;
import io.innospots.schedule.enums.JobType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
@Getter
@Setter
public class ScheduleJobInfo implements Initializer {

    private String jobKey;

    private String jobName;

    private ScheduleMode scheduleMode;

    private DataStatus jobStatus;

    private JobType jobType;

    private String jobClass;

    private String scopes;

    private TimeConfig timeConfig;

    private Map<String,String> params;

    private String cronExpression;

    private LocalDateTime startTime;

    private LocalDateTime endTime;


    @Override
    public void initialize() {
        cronExpression = timeConfig.cronExpression(scheduleMode);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("jobKey='").append(jobKey).append('\'');
        sb.append(", jobName='").append(jobName).append('\'');
        sb.append(", scheduleMode=").append(scheduleMode);
        sb.append(", jobStatus=").append(jobStatus);
        sb.append(", jobType=").append(jobType);
        sb.append(", jobClass='").append(jobClass).append('\'');
        sb.append(", timeConfig=").append(timeConfig);
        sb.append(", cronExpression='").append(cronExpression).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
