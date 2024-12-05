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

package io.innospots.libra.kernel.module.todo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.user.UserInfo;
import io.innospots.base.service.SysUserReadService;
import io.innospots.libra.kernel.module.todo.converter.TodoTaskConverter;
import io.innospots.libra.kernel.module.todo.entity.TodoTaskEntity;
import io.innospots.libra.kernel.module.todo.entity.TodoTaskTagEntity;
import io.innospots.libra.kernel.module.todo.enums.TaskPriority;
import io.innospots.libra.kernel.module.todo.model.TodoTask;
import io.innospots.libra.kernel.module.todo.model.TodoTaskComment;
import io.innospots.libra.kernel.module.todo.model.TodoTaskForm;
import io.innospots.libra.kernel.module.todo.operator.TodoTaskCommentOperator;
import io.innospots.libra.kernel.module.todo.operator.TodoTaskOperator;
import io.innospots.libra.kernel.module.todo.operator.TodoTaskTagOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenc
 * @version 1.0.0
 * @date 2023/2/12
 */
@Slf4j
@Service
public class TodoTaskService {

    private final TodoTaskOperator todoTaskOperator;

    private final TodoTaskTagOperator todoTaskTagOperator;

    private final TodoTaskCommentOperator todoTaskCommentOperator;

    private final SysUserReadService sysUserReadService;

    public TodoTaskService(TodoTaskOperator todoTaskOperator, TodoTaskTagOperator todoTaskTagOperator,
                           TodoTaskCommentOperator todoTaskCommentOperator, SysUserReadService sysUserReadService) {
        this.todoTaskOperator = todoTaskOperator;
        this.todoTaskTagOperator = todoTaskTagOperator;
        this.todoTaskCommentOperator = todoTaskCommentOperator;
        this.sysUserReadService = sysUserReadService;
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoTask createTodoTask(TodoTask task) {
        TodoTask todoTask = todoTaskOperator.createTodoTask(task);
        // create task tag
        if (CollectionUtils.isNotEmpty(task.getTags())) {
            todoTask.setTags(task.getTags());
            todoTaskTagOperator.createTodoTaskTag(todoTask);
        }
        return todoTask;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTodoTask(TodoTask task) {
        boolean result = todoTaskOperator.updateTodoTask(task);
        // create task tag
        if (result && CollectionUtils.isNotEmpty(task.getTags())) {
            todoTaskTagOperator.createTodoTaskTag(task);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTodoTask(Integer taskId) {
        // delete todo_task
        boolean result = todoTaskOperator.deleteTodoTask(taskId);
        if (result) {
            // delete todo_task_tag
            todoTaskTagOperator.deleteTodoTaskTag(taskId);
        }
        return result;
    }

    public TodoTask getTodoTask(Integer taskId) {
        TodoTask task = TodoTaskConverter.INSTANCE.entityToModel(todoTaskOperator.getTodoTask(taskId));

        List<TodoTaskTagEntity> entities = todoTaskTagOperator.listByTaskId(taskId);
        task.setTags(entities.stream().map(TodoTaskTagEntity::getTagName).collect(Collectors.toList()));
        return task;
    }

    public TodoTask getTodoTaskWithComment(Integer taskId) {
        TodoTask task = this.getTodoTask(taskId);
        List<UserInfo> userInfos = sysUserReadService.listUsersByIds(Collections.singletonList(task.getPrincipalUserId()));
        if (CollectionUtils.isNotEmpty(userInfos)) {
            task.setPrincipalUserName(userInfos.get(0).getUserName());
        }
        List<String> userNames = new ArrayList<>();
        userNames.add(task.getCreatedBy());
        List<TodoTaskComment> entities = todoTaskCommentOperator.getTodoTaskComments(taskId);
        userNames.addAll(entities.stream().map(TodoTaskComment::getCreatedBy).collect(Collectors.toList()));
        userInfos = sysUserReadService.listUsersByNames(userNames);
        Map<String, UserInfo> userInfoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUserName, Function.identity()));
        if (MapUtils.isNotEmpty(userInfoMap) && userInfoMap.get(task.getCreatedBy()) != null) {
            task.setAvatarKey(userInfoMap.get(task.getCreatedBy()).getAvatarKey());
        }
        if (CollectionUtils.isNotEmpty(entities)) {
            for (TodoTaskComment comment : entities) {
                if (MapUtils.isNotEmpty(userInfoMap) && userInfoMap.get(comment.getCreatedBy()) != null) {
                    comment.setAvatarKey(userInfoMap.get(comment.getCreatedBy()).getAvatarKey());
                }
            }
            task.setComments(entities);
            task.setCommentCount(entities.size());
        }
        return task;
    }

    /**
     * get priority
     *
     * @return
     */
    public List<String> getPriority() {
        List<String> priorities = new ArrayList<>();
        for (TaskPriority taskPriority : TaskPriority.values()) {
            if (CollectionUtils.isEmpty(priorities) || !priorities.contains(taskPriority.name())) {
                priorities.add(taskPriority.name());
            }
        }
        return priorities;
    }

    public PageBody<TodoTask> pageTasks(TodoTaskForm request) {
        IPage<TodoTaskEntity> page = todoTaskOperator.pageTodoTasks(request);
        PageBody<TodoTask> pageBody = new PageBody<>();
        List<TodoTaskEntity> entities = page.getRecords();
        List<TodoTask> todoTasks = entities.stream().map(TodoTaskConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
        this.generateTasks(todoTasks);
        pageBody.setList(todoTasks);
        pageBody.setCurrent(page.getCurrent());
        pageBody.setPageSize(page.getSize());
        pageBody.setTotal(page.getTotal());
        pageBody.setTotalPage(page.getPages());
        return pageBody;
    }

    private void generateTasks(List<TodoTask> todoTasks) {
        if (CollectionUtils.isEmpty(todoTasks)) {
            return;
        }
        List<Integer> principalUserIds = todoTasks.stream().map(TodoTask::getPrincipalUserId).collect(Collectors.toList());
        List<Integer> taskIds = todoTasks.stream().map(TodoTask::getTaskId).distinct().collect(Collectors.toList());
        List<String> userNames = todoTasks.stream().map(TodoTask::getCreatedBy).collect(Collectors.toList());
        List<UserInfo> userNameInfos = sysUserReadService.listUsersByNames(userNames);
        Map<String, UserInfo> userNameInfoMap = userNameInfos.stream().collect(Collectors.toMap(UserInfo::getUserName, Function.identity()));

        List<TodoTaskTagEntity> tags = todoTaskTagOperator.listByTaskIds(taskIds);
        Map<Integer, List<TodoTaskTagEntity>> tagMap = tags.stream().collect(Collectors.groupingBy(TodoTaskTagEntity::getTaskId));

        List<UserInfo> userIdInfos = sysUserReadService.listUsersByIds(principalUserIds);
        Map<Integer, UserInfo> userIdInfoMap = userIdInfos.stream().collect(Collectors.toMap(UserInfo::getUserId, Function.identity()));

        List<Map<String, Object>> comments = todoTaskCommentOperator.selectCountByTaskId(taskIds);
        Map<Integer, Integer> commentMap = comments.stream().collect(
                Collectors.toMap(
                        k -> Integer.valueOf(k.get("TASK_ID").toString()),
                        v -> Integer.valueOf(v.get("CNT").toString()))
        );

        for (TodoTask task : todoTasks) {
            if (MapUtils.isNotEmpty(userNameInfoMap) && userNameInfoMap.get(task.getCreatedBy()) != null) {
                task.setAvatarKey(userNameInfoMap.get(task.getCreatedBy()).getAvatarKey());
                task.setUserId(userNameInfoMap.get(task.getCreatedBy()).getUserId());
            }
            if (MapUtils.isNotEmpty(tagMap) && CollectionUtils.isNotEmpty(tagMap.get(task.getTaskId()))) {
                task.setTags(tagMap.get(task.getTaskId()).stream().map(TodoTaskTagEntity::getTagName).collect(Collectors.toList()));
            }
            if (MapUtils.isNotEmpty(userIdInfoMap) && userIdInfoMap.get(task.getPrincipalUserId()) != null) {
                task.setPrincipalAvatarKey(userIdInfoMap.get(task.getPrincipalUserId()).getAvatarKey());
                task.setPrincipalUserName(userIdInfoMap.get(task.getPrincipalUserId()).getUserName());
            }
            task.setCommentCount(0);
            if (MapUtils.isNotEmpty(commentMap) && commentMap.get(task.getTaskId()) != null) {
                commentMap.get(task.getTaskId());
            }
        }
    }
}