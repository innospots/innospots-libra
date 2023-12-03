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

import io.innospots.base.watcher.AbstractWatcher;
import io.innospots.schedule.config.InnospotsScheduleProperties;
import io.innospots.schedule.launcher.JobExecutionLauncher;
import io.innospots.schedule.launcher.ReadyJobLauncher;
import io.innospots.schedule.operator.ReadyQueueOperator;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
public class ReadyQueueWatcher extends AbstractWatcher {

    private ReadyQueueOperator readyQueueOperator;

    private ReadyJobLauncher readyJobLauncher;

    private InnospotsScheduleProperties scheduleProperties;

    @Override
    public int execute() {

        return intervalTime();
    }

    private int intervalTime(){
        int slotTime = readyJobLauncher.currentJobCount()*100+scheduleProperties.getScanSlotTimeMills();

        return slotTime;
    }
}
