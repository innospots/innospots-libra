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

import io.innospots.base.execution.ExecutionBase;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.utils.ScheduleUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2023/8/6
 */
@Getter
@Setter
public class JobExecution extends ExecutionBase {

    protected JobType jobType;

    protected Integer percent;

    protected Long subJobCount;

    protected Long successCount;

    protected Long failCount;

    protected String extExecutionId;

    protected String resourceKey;

    /**
     * retry execute the first origin executionId
     * is null if the execution is the first time executed
     */
    protected String originExecutionId;

    protected String timeConsume;

    protected String parentExecutionId;

    protected String serverKey;

    protected String jobClass;

    public boolean isTimeout() {
        Integer timeout = timeoutSecond();
        if (timeout == null) {
            return false;
        }
        if (this.getJobType().isJobContainer()) {
            return false;
        }
        if(this.startTime == null){
            return false;
        }
        LocalDateTime timeoutTime = this.getStartTime().plusSeconds(timeout);
        return LocalDateTime.now().isAfter(timeoutTime);
    }

    public Integer timeoutSecond(){
        return this.getInteger(ScheduleUtils.PARAM_TIMEOUT_SECOND);
    }

    public String info() {
        LocalDateTime ed = this.endTime != null ? this.endTime : this.selfEndTime;
        if (ed == null) {
            ed = LocalDateTime.now();
        }
        this.timeConsume = DateTimeUtils.consume(ed, this.startTime);
        return String.format("jobExecutionId:%s, timeConsume:%s, status:%s, jobKey:%s, jobClass:%s," +
                        " jobType:%s, percent:%s, subJobCount:%s," +
                        " successCount:%s, failCount:%s," +
                        " serverKey:%s, jobClass:%s",
                executionId, timeConsume,this.executionStatus, jobKey, jobClass,
                jobType, percent, subJobCount, successCount, failCount, serverKey, jobClass);
    }

}
