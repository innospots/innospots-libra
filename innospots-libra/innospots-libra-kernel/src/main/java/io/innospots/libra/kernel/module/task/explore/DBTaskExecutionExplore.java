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

package io.innospots.libra.kernel.module.task.explore;

import io.innospots.base.events.EventBusCenter;
import io.innospots.libra.base.task.ITaskExecutionExplore;
import io.innospots.libra.base.task.TaskEvent;
import io.innospots.libra.base.task.TaskExecution;
import io.innospots.libra.kernel.module.task.operator.TaskExecutionOperator;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Component
public class DBTaskExecutionExplore implements ITaskExecutionExplore {

    private final TaskExecutionOperator taskExecutionOperator;

    public DBTaskExecutionExplore(TaskExecutionOperator taskExecutionOperator) {
        this.taskExecutionOperator = taskExecutionOperator;
    }

    @Override
    public boolean saveTaskExecution(TaskExecution taskExecution) {
        return taskExecutionOperator.createTaskExecution(taskExecution);
    }

    @Override
    public boolean updateTaskExecution(TaskExecution taskExecution) {
        return taskExecutionOperator.updateTaskExecution(taskExecution);
    }

    @Override
    public boolean updateTaskExecution(String taskExecutionId, int percent) {
        return false;
    }

    @Override
    public boolean stop(String taskExecutionId) {
        TaskExecution taskExecution = taskExecutionOperator.getTaskExecution(taskExecutionId);

        TaskEvent taskEvent = TaskEvent.build(taskExecution, TaskEvent.TaskAction.STOP);

        //forward application event
        EventBusCenter.getInstance().post(taskEvent);
        return true;
    }

    @Override
    public boolean reRun(String taskExecutionId) {
        TaskExecution taskExecution = taskExecutionOperator.getTaskExecution(taskExecutionId);

        TaskEvent taskEvent = TaskEvent.build(taskExecution, TaskEvent.TaskAction.RERUN);

        // forward application event
        EventBusCenter.getInstance().post(taskEvent);
        return true;
    }
}
