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
import io.innospots.base.model.response.R;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH + "schedule/job")
@ModuleMenu(menuKey = "job-schedule")
@Tag(name = "schedule job executor")
public class JobExecutorController {

    private final ReadJobDispatcher readJobDispatcher;

    public JobExecutorController(ReadJobDispatcher readJobDispatcher) {
        this.readJobDispatcher = readJobDispatcher;
    }

    @Operation(description = "execute schedule job")
    @OperationLog(operateType = OperateType.EXECUTE)
    @PostMapping("execute/{jobKey}")
    public R<Void> launch(@PathVariable String jobKey, @RequestBody Map<String, Object> params) {
        readJobDispatcher.dispatch(jobKey, params);
        return R.success();
    }

    @OperationLog(operateType = OperateType.UPDATE_STATUS)
    @Operation(description = "cancel schedule job in the queue")
    @PostMapping("cancel/{jobKey}")
    public R<Integer> cancel(@PathVariable String jobKey) {
        return R.success(readJobDispatcher.cancel(jobKey));
    }

    @OperationLog(operateType = OperateType.EXECUTE)
    @Operation(description = "retry execute schedule job")
    @PostMapping("retry/{jobExecutionId}")
    public R<Integer> retry(@PathVariable String jobExecutionId) {
        int c = readJobDispatcher.retryDispatch(jobExecutionId);
        return R.success(c);
    }

    @OperationLog(operateType = OperateType.EXECUTE)
    @Operation(description = "continue execute stopped job")
    @PostMapping("continue/{jobExecutionId}")
    public R<Integer> continueExecute(@PathVariable String jobExecutionId) {
        int c = readJobDispatcher.continueDispatch(jobExecutionId);
        return R.success(c);
    }

    @OperationLog(operateType = OperateType.OFFLINE)
    @Operation(description = "stop executing job")
    @PostMapping("stop/{jobExecutionId}")
    public R<Integer> stop(@PathVariable String jobExecutionId,
                           @RequestParam(required = false) String message) {
        return R.success(readJobDispatcher.stop(jobExecutionId, message));
    }

}
