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

package io.innospots.schedule.events;

import io.innospots.base.events.EventBody;
import io.innospots.schedule.model.execution.TaskExecution;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/8
 */
public class TaskEvent extends EventBody {

    private String appKey;

    private String extensionKey;

    private TaskAction taskAction;

    public static TaskEvent build(TaskExecution taskExecution, TaskAction taskAction) {
        TaskEvent taskEvent = new TaskEvent(taskExecution.getParamContext(),
                taskExecution.getAppKey(),
                taskExecution.getExtensionKey(), taskAction);
        return taskEvent;
    }

    public TaskEvent(Object source) {
        super(source);
    }

    private TaskEvent(Object source, String appKey, String extensionKey, TaskAction taskAction) {
        super(source);
        this.appKey = appKey;
        this.extensionKey = extensionKey;
        this.taskAction = taskAction;
    }

    public Map<String, Object> params() {
        if (body != null) {
            return (Map<String, Object>) body;
        }
        return null;
    }

    public enum TaskAction {
        STOP,
        RERUN;
    }

    public String appKey() {
        return appKey;
    }

    public String extensionKey() {
        return extensionKey;
    }

    public TaskAction taskAction() {
        return taskAction;
    }
}
