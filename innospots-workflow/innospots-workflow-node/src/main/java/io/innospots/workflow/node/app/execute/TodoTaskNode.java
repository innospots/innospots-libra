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

package io.innospots.workflow.node.app.execute;

import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;

/**
 * @author Smars
 * @date 2023/5/12
 */
public class TodoTaskNode extends BaseNodeExecutor {


    private static final String Field_TASK_NAME = "taskName";
    private static final String Field_TASK_DESC = "taskDesc";
    private static final String Field_USER_NAME = "userName";
    private static final String Field_USER_ID = "userId";
    private static final String Field_GROUP_ID = "groupId";
    private static final String Field_TASK_TAGS = "taskTags";
    private static final String Field_TASK_PRIORITY = "taskPriority";

    @Override
    protected void initialize() {

    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
    }
}
