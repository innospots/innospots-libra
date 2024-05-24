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

package io.innospots.schedule.starter;

import io.innospots.base.utils.ServiceRoleHolder;
import io.innospots.schedule.config.InnospotsScheduleProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/7
 */
@Order(10)
public class ScheduleExecutorStarter implements ApplicationRunner {


    private InnospotsScheduleProperties scheduleProperties;

    public ScheduleExecutorStarter(InnospotsScheduleProperties scheduleProperties) {
        this.scheduleProperties = scheduleProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ServiceRoleHolder.setGroupKeys(scheduleProperties.getGroupKeys());
    }
}
