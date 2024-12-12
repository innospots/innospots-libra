/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.libra.kernel.module.todo.controller;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.todo.enums.TaskStatus;
import io.innospots.libra.kernel.module.todo.model.TodoTask;
import io.innospots.libra.kernel.module.todo.model.TodoTaskForm;
import io.innospots.libra.kernel.module.todo.operator.TodoTaskOperator;
import io.innospots.libra.kernel.module.todo.operator.TodoTaskTagOperator;
import io.innospots.libra.kernel.module.todo.service.TodoTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/8
 */
@Tag(name = "Todo Task")
@RequestMapping(BaseController.PATH_ROOT_ADMIN + "todo-task")
@ModuleMenu(menuKey = "libra-task")
@RestController
public class TodoTaskController extends BaseController {

    private final TodoTaskOperator todoTaskOperator;

    private final TodoTaskService todoTaskService;

    private final TodoTaskTagOperator todoTaskTagOperator;

    public TodoTaskController(TodoTaskOperator todoTaskOperator, TodoTaskService todoTaskService, TodoTaskTagOperator todoTaskTagOperator) {
        this.todoTaskOperator = todoTaskOperator;
        this.todoTaskService = todoTaskService;
        this.todoTaskTagOperator = todoTaskTagOperator;
    }

    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "taskId")
    @Operation(description = "create task")
    public R<TodoTask> createTodoTask(@Parameter(name = "todoTask", required = true) @Validated @RequestBody TodoTask todoTask) {

        TodoTask create = todoTaskService.createTodoTask(todoTask);
        return success(create);
    }

    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @OperationLog(primaryField = "taskId", operateType = OperateType.UPDATE)
    @Operation(description = "update task")
    public R<Boolean> updateTodoTask(@Parameter(name = "todoTask", required = true) @Validated @RequestBody TodoTask todoTask) {

        Boolean update = todoTaskService.updateTodoTask(todoTask);
        return success(update);
    }

    @DeleteMapping("{taskId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(idParamPosition = 0, operateType = OperateType.DELETE)
    @Operation(description = "delete task")
    public R<Boolean> deleteTodoTask(@Parameter(name = "taskId", required = true) @PathVariable Integer taskId) {

        Boolean delete = todoTaskService.deleteTodoTask(taskId);
        return success(delete);
    }

    @GetMapping("{taskId}")
    @Operation(description = "view task")
    public R<TodoTask> getTodoTask(@Parameter(name = "taskId", required = true) @PathVariable Integer taskId) {

        TodoTask view = todoTaskService.getTodoTask(taskId);
        return success(view);
    }

    @PutMapping("{taskId}/status/{taskStatus}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${common.button.status}")
    @OperationLog(idParamPosition = 0, operateType = OperateType.UPDATE_STATUS)
    @Operation(description = "update task status")
    public R<Boolean> updateTaskStatus(@Parameter(required = true, name = "taskId") @PathVariable Integer taskId,
                                       @Parameter(required = true, name = "taskStatus") @PathVariable TaskStatus taskStatus) {

        Boolean update = todoTaskOperator.updateTaskStatus(taskId, taskStatus);
        return success(update);
    }

    @GetMapping("priority")
    @Operation(description = "get priority")
    public R<List<String>> getPriority() {
        return success(todoTaskService.getPriority());
    }

    @GetMapping("tag")
    @Operation(description = "get tag")
    public R<List<String>> getTag() {
        return success(todoTaskTagOperator.getTag());
    }

    @GetMapping("page")
    @Operation(description = "query task")
    public R<PageBody<TodoTask>> pageTodoTasks(TodoTaskForm request) {
        PageBody<TodoTask> pageModel = todoTaskService.pageTasks(request);
        return success(pageModel);
    }
}