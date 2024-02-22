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

package io.innospots.base.quartz;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2020/12/20
 */
public enum ExecutionStatus {

    /**
     *
     */
    NOT_PREPARED(1),
    READY(1),
    STARTING(2),
    RUNNING(2),
    CONTINUE_RUNNING(1),
    COMPLETE(3),
    FAILED(3),
    PENDING(4),
    STOPPING(5),
    STOPPED(3);

    /**
     *
     */
    private int group;

    ExecutionStatus(int group) {
        this.group = group;
    }

    public static List<ExecutionStatus> executingStatus() {
        return Arrays.stream(ExecutionStatus.values()).filter(ExecutionStatus::isExecuting).collect(Collectors.toList());
    }

    public static List<ExecutionStatus> doneStatus() {
        return Arrays.stream(ExecutionStatus.values()).filter(ExecutionStatus::isDone).collect(Collectors.toList());
    }

    public boolean isExecuting() {
        return group == 1 || group == 2;
    }

    public boolean isDone() {
        return group == 3 || group == 4;
    }

    public boolean isStopping() {
        return this == STOPPING;
    }
}
