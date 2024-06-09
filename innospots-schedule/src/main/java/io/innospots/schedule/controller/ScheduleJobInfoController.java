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
import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.base.quartz.JobType;
import io.innospots.base.quartz.ScheduleJobInfo;
import io.innospots.schedule.operator.ScheduleJobInfoOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static io.innospots.libra.base.menu.ItemType.BUTTON;

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

    @OperationLog(operateType = OperateType.CREATE, primaryField = "jobKey")
    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(summary = "create schedule")
    public InnospotsResponse<ScheduleJobInfo> createScheduleJobInfo(@Validated @RequestBody ScheduleJobInfo scheduleJobInfo) {
        return InnospotsResponse.success(scheduleJobInfoOperator.createScheduleJobInfo(scheduleJobInfo));
    }


    @OperationLog(operateType = OperateType.UPDATE, primaryField = "jobKey")
    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(summary = "update schedule")
    public InnospotsResponse<ScheduleJobInfo> updateScheduleJobInfo(@Validated @RequestBody ScheduleJobInfo scheduleJobInfo) {
        return InnospotsResponse.success(scheduleJobInfoOperator.updateScheduleJobInfo(scheduleJobInfo));
    }

    @OperationLog(operateType = OperateType.UPDATE_STATUS, primaryField = "jobKey")
    @PutMapping("{jobKey}/status/{jobStatus}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.status}")
    public InnospotsResponse updateScheduleJobStatus(@PathVariable String jobKey, @PathVariable DataStatus jobStatus) {
        return InnospotsResponse.success(scheduleJobInfoOperator.updateScheduleJobStatus(jobKey, jobStatus));
    }

    @GetMapping("{jobKey}")
    public InnospotsResponse<ScheduleJobInfo> getScheduleJobInfo(@PathVariable String jobKey) {
        return InnospotsResponse.success(scheduleJobInfoOperator.getScheduleJobInfo(jobKey));
    }

    @OperationLog(operateType = OperateType.DELETE, primaryField = "jobKey")
    @DeleteMapping("{jobKey}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(summary = "delete schedule")
    public InnospotsResponse<Boolean> deleteScheduleJobInfo(@PathVariable String jobKey) {
        return InnospotsResponse.success(scheduleJobInfoOperator.deleteScheduleJobInfo(jobKey));
    }

    @GetMapping("page")
    @Operation(summary = "page schedule list")
    public InnospotsResponse<PageBody<ScheduleJobInfo>> pageScheduleJobInfo(
            @Parameter(name = "page") @RequestParam("page") int page,
            @Parameter(name = "size") @RequestParam("size") int size,
            @Parameter(name = "jobType") @RequestParam(value = "jobType", required = false) JobType jobType,
            @Parameter(name = "jobStatus") @RequestParam(value = "jobStatus", required = false) DataStatus jobStatus,
            @Parameter(name = "scheduleMode") @RequestParam(value = "scheduleMode", required = false) ScheduleMode scheduleMode
            ){
                return InnospotsResponse.success(scheduleJobInfoOperator.pageScheduleJobInfo(page, size, jobType, jobStatus, scheduleMode));
    }

}
