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

package io.innospots.workflow.core.flow.loader;

import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.instance.operator.WorkflowBodyOperator;

import java.util.List;

/**
 * @author Smars
 * @date 2023/8/7
 */
public class WorkflowDBLoader implements IWorkflowLoader {


    private final WorkflowBodyOperator workflowBodyOperator;

    public WorkflowDBLoader(WorkflowBodyOperator workflowBodyOperator) {
        this.workflowBodyOperator = workflowBodyOperator;
    }

    @Override
    public WorkflowBody loadWorkflow(Long workflowInstanceId, Integer revision) {
        return workflowBodyOperator.getWorkflowBody(workflowInstanceId, revision, true);
    }


    @Override
    public List<WorkflowBody> loadRecentlyUpdateOrOnLine(int recentMinutes) {
        return workflowBodyOperator.selectRecentlyUpdateOrOnLine(recentMinutes);
    }

    @Override
    public WorkflowBody loadWorkflow(String flowKey) {
        return workflowBodyOperator.getWorkflowBody(flowKey);
    }

}
