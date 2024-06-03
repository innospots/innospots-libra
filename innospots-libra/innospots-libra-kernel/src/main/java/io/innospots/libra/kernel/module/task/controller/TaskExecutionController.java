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

package io.innospots.libra.kernel.module.task.controller;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.task.TaskEvent;
import io.innospots.libra.base.task.TaskExecution;
import io.innospots.libra.base.task.TaskExecutionStatus;
import io.innospots.libra.kernel.module.task.explore.DBTaskExecutionExplore;
import io.innospots.libra.kernel.module.task.model.TaskExecutionForm;
import io.innospots.libra.kernel.module.task.operator.TaskExecutionOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @date 2023/8/7
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "task-execution")
@ModuleMenu(menuKey = "libra-schedule-task")
@Tag(name = "TaskExecution")
public class TaskExecutionController {

    private final TaskExecutionOperator taskExecutionOperator;

    private final DBTaskExecutionExplore taskExecutionExplore;

    public TaskExecutionController(TaskExecutionOperator taskExecutionOperator, DBTaskExecutionExplore taskExecutionExplore) {
        this.taskExecutionOperator = taskExecutionOperator;
        this.taskExecutionExplore = taskExecutionExplore;
    }

    @GetMapping("page/task-execution")
    @Operation(summary = "page task executions")
    public InnospotsResponse<PageBody<TaskExecution>> pageTaskExecutions(TaskExecutionForm request) {

        PageBody<TaskExecution> pageModel = taskExecutionOperator.pageTaskExecutions(request);
        return success(pageModel);
    }

    @GetMapping("{taskExecutionId}")
    @Operation(summary = "view task executions")
    public InnospotsResponse<TaskExecution> getTaskExecution(@Parameter(name = "taskExecutionId", required = true) @PathVariable String taskExecutionId) {

        TaskExecution taskExecution = taskExecutionOperator.getTaskExecutionById(taskExecutionId);
        return success(taskExecution);
    }

    @PutMapping("{taskExecutionId}/{operateType}")
    @Operation(summary = "operate task executions")
    public InnospotsResponse<Boolean> operateTaskExecution(@Parameter(name = "taskExecutionId", required = true) @PathVariable String taskExecutionId,
                                                           @PathVariable TaskEvent.TaskAction operateType) {
        boolean result;
        if (operateType == TaskEvent.TaskAction.RERUN) {
            result = taskExecutionExplore.reRun(taskExecutionId);

        } else if (operateType == TaskEvent.TaskAction.STOP) {
            result = taskExecutionExplore.stop(taskExecutionId);

        } else {
            throw InnospotException.buildException(this.getClass(), ResponseCode.PARAM_INVALID, ResponseCode.PARAM_INVALID.info());
        }
        return success(result);
    }

    @GetMapping("task-code")
    @Operation(summary = "get taskCode")
    public InnospotsResponse<List<String>> getTaskCode() {
        List<String> taskCodes = new ArrayList<>();
        for (TaskExecutionStatus status : TaskExecutionStatus.values()) {
            taskCodes.add(status.name());
        }
        return success(taskCodes);
    }

    @GetMapping("apps")
    @Operation(summary = "get apps")
    public InnospotsResponse<List<String>> getApps() {
        List<String> apps = new ArrayList<>();
        apps.add("工作流引擎");
        return success(apps);
    }
}
