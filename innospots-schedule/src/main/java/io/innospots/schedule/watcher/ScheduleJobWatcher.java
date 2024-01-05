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

package io.innospots.schedule.watcher;

import io.innospots.base.enums.DataStatus;
import io.innospots.base.quartz.QuartzScheduleManager;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.base.watcher.AbstractWatcher;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import io.innospots.schedule.quartz.QuartzJobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * schedule service watcher
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Component
@Slf4j
public class ScheduleJobWatcher extends AbstractWatcher {

    private final QuartzScheduleManager scheduleManager;

    private final ScheduleJobInfoOperator scheduleJobInfoOperator;

    public ScheduleJobWatcher(QuartzScheduleManager scheduleManager,
                              ScheduleJobInfoOperator scheduleJobInfoOperator) {
        this.scheduleManager = scheduleManager;
        this.scheduleJobInfoOperator = scheduleJobInfoOperator;
    }

    @Override
    public int execute() {
        List<ScheduleJobInfo> jobInfos = scheduleJobInfoOperator.fetchUpdatedQuartzTimeJob();
        if (CollectionUtils.isEmpty(jobInfos)) {
            return checkIntervalSecond;
        }
        for (ScheduleJobInfo jobInfo : jobInfos) {
            if (jobInfo.getJobStatus() == DataStatus.OFFLINE) {
                log.info("offline job remove from scheduler:{}", jobInfo.getJobName());
                scheduleManager.deleteJob(jobInfo.getJobKey());
            } else {
                log.info("job add to schedule:{}", jobInfo);
                Date startTime = jobInfo.getStartTime() != null ? DateTimeUtils.asDate(jobInfo.getStartTime()) : null;
                Date endTime = jobInfo.getEndTime() != null ? DateTimeUtils.asDate(jobInfo.getEndTime()) : null;
                scheduleManager.refreshJob(jobInfo.getJobKey(), QuartzJobScheduler.class, jobInfo.getCronExpression(), jobInfo.getScheduleMode(), startTime, endTime);
            }
        }
        return checkIntervalSecond;
    }
}
