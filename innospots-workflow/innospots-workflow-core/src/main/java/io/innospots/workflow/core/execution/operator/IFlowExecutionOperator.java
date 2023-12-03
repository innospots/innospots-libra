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

package io.innospots.workflow.core.execution.operator;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.store.IDataStore;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.flow.FlowExecutionBase;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2023/1/20
 */
public interface IFlowExecutionOperator extends IDataStore<FlowExecution> {


    FlowExecution getFlowExecutionById(String flowExecutionId, boolean includeContext);

    FlowExecution getLatestFlowExecution(Long flowInstanceId, Integer revision, boolean includeContext);

    PageBody<FlowExecutionBase> pageFlowExecutions(Long flowInstanceId, Integer revision, List<String> statuses,
                                                   String startTime, String endTime, Integer page, Integer size);

    PageBody<FlowExecutionBase> pageLatestFlowExecutions(Long flowInstanceId, Integer revision, List<String> statuses, Integer page, Integer size);

    boolean updateStatus(String executionId, ExecutionStatus status, String message);

    default PageBody<FlowExecutionBase> pageFlowExecutions(Long flowInstanceId, Integer revision,
                                                           String startTime, String endTime, Integer page, Integer size) {
        return pageFlowExecutions(flowInstanceId, revision, null, startTime, endTime, page, size);
    }

    default PageBody<FlowExecutionBase> pageLatestFlowExecutions(Long flowInstanceId, Integer revision, Integer page, Integer size) {
        return pageLatestFlowExecutions(flowInstanceId, revision, null, page, size);
    }

}
