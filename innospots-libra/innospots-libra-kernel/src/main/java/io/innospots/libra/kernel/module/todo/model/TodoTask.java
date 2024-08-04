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

package io.innospots.libra.kernel.module.todo.model;

import io.innospots.libra.kernel.module.todo.enums.TaskPriority;
import io.innospots.libra.kernel.module.todo.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/8
 */
@Getter
@Setter
public class TodoTask {

    private Integer taskId;

    @NotNull(message = "task name must not be null")
    @Size(max = 128, message = "taskName length max 128")
    private String taskName;

    @NotNull(message = "description must not be null")
    private String description;

    @NotNull(message = "principal user id must not be null")
    private Integer principalUserId;

    private String principalUserName;

    private String principalAvatarKey;

    private TaskStatus taskStatus;

    @NotNull(message = "task priority must not be null")
    private TaskPriority taskPriority;

    @NotNull(message = "start date must not be null")
    private LocalDate startDate;

    @NotNull(message = "end date must not be null")
    private LocalDate endDate;

    private List<String> tags;

    private Integer userId;

    private String createdBy;

    private String avatarKey;

    private String createdTime;

    private String updatedTime;

    private List<TodoTaskComment> comments;

    private Integer commentCount;
}
