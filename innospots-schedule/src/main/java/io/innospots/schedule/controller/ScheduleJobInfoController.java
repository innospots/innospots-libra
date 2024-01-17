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

package io.innospots.schedule.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/6
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH +"schedule/job-info")
@ModuleMenu(menuKey = "job-schedule")
@Tag(name = "schedule job info")
public class ScheduleJobInfoController {

    private ScheduleJobInfoOperator scheduleJobInfoOperator;

    public ScheduleJobInfoController(ScheduleJobInfoOperator scheduleJobInfoOperator) {
        this.scheduleJobInfoOperator = scheduleJobInfoOperator;
    }




}
