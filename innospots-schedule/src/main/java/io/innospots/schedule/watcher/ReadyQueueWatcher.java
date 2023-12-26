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

import io.innospots.base.utils.ServiceActionHolder;
import io.innospots.base.watcher.AbstractWatcher;
import io.innospots.schedule.config.InnospotsScheduleProperties;
import io.innospots.schedule.config.ScheduleConstant;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.launcher.JobLauncher;
import io.innospots.schedule.queue.IReadyJobQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * execute service
 * ready job queue thread watcher
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Slf4j
public class ReadyQueueWatcher extends AbstractWatcher {

    private IReadyJobQueue readyJobDbQueue;

    private JobLauncher jobLauncher;

    private InnospotsScheduleProperties scheduleProperties;

    private LocalDateTime lastUpdateTime;

    @Override
    public int execute() {
        //fetch available worker size
        int fetchSize = scheduleProperties.getExecutorSize() - jobLauncher.currentJobCount();

        if (fetchSize <= 0) {
            return intervalTime();
        }

        //fetch from ready queue
        List<ReadyJobEntity> readyList = readyJobDbQueue.poll(fetchSize, getGroupKeys());
        if (CollectionUtils.isEmpty(readyList)) {
            //empty
            return intervalTime();
        }

        for (ReadyJobEntity readyJobEntity : readyList) {
            jobLauncher.launch(readyJobEntity);
        }

        return intervalTime();
    }

    private List<String> getGroupKeys() {
        return scheduleProperties.getGroupKeys() != null ? scheduleProperties.getGroupKeys() : ServiceActionHolder.getGroupKeys();
    }

    private int intervalTime() {
        int slotTime = jobLauncher.currentJobCount() * 100 + scheduleProperties.getScanSlotTimeMills();
        return slotTime;
    }

    private void checkAssignTime() {
        if(!ScheduleConstant.isScheduler()){
            return;
        }
        if (lastUpdateTime == null) {
            lastUpdateTime = LocalDateTime.now();
        } else {
            if (LocalDateTime.now().isAfter(lastUpdateTime.plusSeconds(scheduleProperties.getAssignedJobTimeoutSecond()))) {
                readyJobDbQueue.resetAssignTimeoutJob(scheduleProperties.getAssignedJobTimeoutSecond());
            }
        }
    }
}
