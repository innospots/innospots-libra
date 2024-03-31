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
import io.innospots.schedule.explore.ScheduleJobInfoExplorer;
import io.innospots.schedule.model.ScheduleJobInfo;
import io.innospots.schedule.quartz.QuartzJobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.math3.analysis.function.Identity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * schedule service watcher
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class ScheduleJobWatcher extends AbstractWatcher {

    private final QuartzScheduleManager scheduleManager;

    private final ScheduleJobInfoExplorer scheduleJobInfoExplorer;

    private Map<String, ScheduleJobInfo> currentJobCache;

    public ScheduleJobWatcher(QuartzScheduleManager scheduleManager,
                              ScheduleJobInfoExplorer scheduleJobInfoExplorer) {
        this.scheduleManager = scheduleManager;
        this.scheduleJobInfoExplorer = scheduleJobInfoExplorer;
    }

    @Override
    public int execute() {
        List<ScheduleJobInfo> jobInfos = scheduleJobInfoExplorer.fetchOnlineQuartzTimeJob();
        Set<String> scheduleJobKeys = scheduleManager.scheduleJobs();
        if (CollectionUtils.isEmpty(jobInfos) && scheduleJobKeys.isEmpty()) {
            currentJobCache = null;
            return checkIntervalMills;
        }

        for (ScheduleJobInfo jobInfo : jobInfos) {
            ScheduleJobInfo currentJob = currentJobCache != null ? currentJobCache.get(jobInfo.getJobKey()) : null;
            if (currentJob == null || jobInfo.getUpdatedTime().isAfter(currentJob.getUpdatedTime())) {
                //have new online job or job updated
                log.info("job add to schedule:{}", jobInfo);
                Date startTime = jobInfo.getStartTime() != null ? DateTimeUtils.asDate(jobInfo.getStartTime()) : null;
                Date endTime = jobInfo.getEndTime() != null ? DateTimeUtils.asDate(jobInfo.getEndTime()) : null;
                jobInfo.initialize();
                scheduleManager.refreshJob(jobInfo.getJobKey(), QuartzJobScheduler.class, jobInfo.getCronExpression(), jobInfo.getScheduleMode(), startTime, endTime);
            }
            if (scheduleJobKeys != null) {
                scheduleJobKeys.remove(jobInfo.getJobKey());
            }
        }//end for

        if (CollectionUtils.isNotEmpty(scheduleJobKeys)) {
            scheduleJobKeys.forEach(jobKey -> {
                log.info("job remove from schedule:{}", jobKey);
                scheduleManager.deleteJob(jobKey);
            });
        }
        currentJobCache = jobInfos.stream().collect(Collectors.toMap(ScheduleJobInfo::getJobKey, jobInfo -> jobInfo));
        return checkIntervalMills;
    }
}
