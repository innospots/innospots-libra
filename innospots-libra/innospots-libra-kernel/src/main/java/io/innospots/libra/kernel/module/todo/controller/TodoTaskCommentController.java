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

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.todo.model.TodoTask;
import io.innospots.libra.kernel.module.todo.model.TodoTaskComment;
import io.innospots.libra.kernel.module.todo.operator.TodoTaskCommentOperator;
import io.innospots.libra.kernel.module.todo.service.TodoTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/8
 */
@Tag(name = "Todo Task Comment")
@RequestMapping(BaseController.PATH_ROOT_ADMIN + "todo-task/comment")
@ModuleMenu(menuKey = "libra-task")
@RestController
public class TodoTaskCommentController extends BaseController {

    private final TodoTaskService todoTaskService;

    private final TodoTaskCommentOperator todoTaskCommentOperator;

    public TodoTaskCommentController(TodoTaskService todoTaskService, TodoTaskCommentOperator todoTaskCommentOperator) {
        this.todoTaskService = todoTaskService;
        this.todoTaskCommentOperator = todoTaskCommentOperator;
    }

    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "commentId")
    @Operation(summary = "create task comment")
    public InnospotsResponse<TodoTaskComment> createTodoTaskComment(@Parameter(name = "todoTaskComment", required = true) @Validated @RequestBody TodoTaskComment todoTaskComment) {

        TodoTaskComment create = todoTaskCommentOperator.createTodoTaskComment(todoTaskComment);
        return success(create);
    }

    @GetMapping("{taskId}")
    @Operation(summary = "view task with comment")
    public InnospotsResponse<TodoTask> getTodoTaskWithComment(@Parameter(name = "taskId", required = true) @PathVariable Integer taskId) {

        TodoTask view = todoTaskService.getTodoTaskWithComment(taskId);
        return success(view);
    }

    @DeleteMapping("{commentId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(idParamPosition = 0, operateType = OperateType.DELETE)
    @Operation(summary = "delete task comment")
    public InnospotsResponse<Boolean> deleteTodoTaskComment(@Parameter(name = "commentId", required = true) @PathVariable Integer commentId) {

        Boolean delete = todoTaskCommentOperator.deleteTodoTaskComment(commentId);
        return success(delete);
    }

    //    @OperationLog(operateType = OperateType.UPLOAD)
    @PostMapping(value = "image")
//    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(summary = "upload image")
    public InnospotsResponse<List<String>> uploadImage(@Parameter(name = "images", required = true) @RequestParam("images") List<MultipartFile> uploadFiles) {

        return success(todoTaskCommentOperator.uploadImage(uploadFiles));
    }
}