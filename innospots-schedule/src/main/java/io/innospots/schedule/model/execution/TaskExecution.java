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

package io.innospots.schedule.model.execution;

import io.innospots.base.quartz.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/6
 */
@Getter
@Setter
public class TaskExecution {

    private String taskExecutionId;

    private String taskName;

    private ExecutionStatus executionStatus;

    private String extensionKey;

    private String extensionType;

    private String appName;

    private String appKey;

    private int percent;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String detailUrl;

    private Map<String,Object> paramContext;

    private String message;

    private String timeConsume;

    private String createdBy;
}
