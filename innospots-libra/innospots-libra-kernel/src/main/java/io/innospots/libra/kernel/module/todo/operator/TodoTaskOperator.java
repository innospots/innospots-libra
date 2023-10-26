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

package io.innospots.libra.kernel.module.todo.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.utils.CCH;
import io.innospots.libra.kernel.module.todo.dao.TodoTaskDao;
import io.innospots.libra.kernel.module.todo.entity.TodoTaskEntity;
import io.innospots.libra.kernel.module.todo.enums.TaskStatus;
import io.innospots.libra.kernel.module.todo.mapper.TodoTaskBeanConverter;
import io.innospots.libra.kernel.module.todo.model.TodoTask;
import io.innospots.libra.kernel.module.todo.model.TodoTaskRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/8
 */
@Service
public class TodoTaskOperator extends ServiceImpl<TodoTaskDao, TodoTaskEntity> {

    @Transactional(rollbackFor = Exception.class)
    public TodoTask createTodoTask(TodoTask todoTask) {
        this.checkDifferentTaskName(todoTask);
        todoTask.setTaskStatus(TaskStatus.Pending);
        TodoTaskBeanConverter mapper = TodoTaskBeanConverter.INSTANCE;
        TodoTaskEntity entity = mapper.model2Entity(todoTask);
        super.save(entity);
        return mapper.entity2Model(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTodoTask(TodoTask todoTask) {
        this.checkDifferentTaskName(todoTask);
        TodoTaskEntity entity = this.getById(todoTask.getTaskId());
        TodoTaskBeanConverter.INSTANCE.updateEntity2Model(entity, todoTask);
        return super.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTodoTask(Integer taskId) {
        return this.removeById(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskStatus(Integer taskId, TaskStatus taskStatus) {
        this.checkTaskOwner(taskId);
        UpdateWrapper<TodoTaskEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(TodoTaskEntity::getTaskId, taskId)
                .set(TodoTaskEntity::getTaskStatus, taskStatus);
        return this.update(wrapper);
    }

    public TodoTaskEntity getTodoTask(Integer taskId) {
        TodoTaskEntity entity = this.getById(taskId);
        if (entity == null) {
            throw ResourceException.buildExistException(this.getClass(), "todo task does not exist");
        }
        return entity;
    }

    public IPage<TodoTaskEntity> pageTodoTasks(TodoTaskRequest request) {
        QueryWrapper<TodoTaskEntity> query = new QueryWrapper<>();
        if (StringUtils.isNotBlank(request.getQueryInput())) {
            query.like("s.TASK_NAME", "%" + request.getQueryInput() + "%");
        }
        if (CollectionUtils.isNotEmpty(request.getPrincipalUserIds())) {
            query.in("s.PRINCIPAL_USER_ID", request.getPrincipalUserIds());
        }
        if (CollectionUtils.isNotEmpty(request.getPriorities())) {
            query.in("s.TASK_PRIORITY", request.getPriorities());
        }
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            query.in("t.TAG_NAME", request.getTags());
        }
        if (StringUtils.isNotBlank(request.getStartDate())) {
            query.le("s.START_DATE", request.getStartDate());
        }
        if (StringUtils.isNotBlank(request.getEndDate())) {
            query.ge("s.END_DATE", request.getEndDate());
        }
        query.groupBy("s.TASK_ID");
        if (StringUtils.isNotBlank(request.getSort())) {
            query.orderBy(true, request.getAsc(), CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        } else {
            query.orderBy(true, false, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "s.createdTime"));
        }
        IPage<TodoTaskEntity> iPage = new Page<>(request.getPage(), request.getSize());
        return baseMapper.selectTaskPage(iPage, query);
    }

    private void checkTaskOwner(Integer taskId) {
        TodoTaskEntity entity = this.getTodoTask(taskId);
        Integer userId = CCH.userId();
        String userName = CCH.authUser();
        if (!entity.getCreatedBy().equals(userName) && !entity.getPrincipalUserId().equals(userId)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.TASK_STATUS_EDIT_ERROR, ResponseCode.TASK_STATUS_EDIT_ERROR.info());
        }
    }

    /**
     * check different task have the same taskName
     *
     * @param todoTask
     */
    private void checkDifferentTaskName(TodoTask todoTask) {
        QueryWrapper<TodoTaskEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TodoTaskEntity> lambda = queryWrapper.lambda();
        lambda.eq(TodoTaskEntity::getTaskName, todoTask.getTaskName());
        if (todoTask.getTaskId() != null) {
            lambda.ne(TodoTaskEntity::getTaskId, todoTask.getTaskId());
        }
        long count = super.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "task name", todoTask.getTaskName());
        }
    }
}
