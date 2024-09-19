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

package io.innospots.workflow.core.runtime.webhook;

import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;

import java.time.Duration;
import java.util.List;


/**
 * @author Smars
 * @date 2021/3/12
 */
public interface WorkflowResponseBuilder {


    <T> WorkflowResponse<T> build(WorkflowRuntimeContext workflowRuntimeContext, FlowWebhookConfig webhookConfig);

    default  <T> WorkflowResponse<T>  build(WorkflowRuntimeContext workflowRuntimeContext) {
        return build(workflowRuntimeContext, null);
    }

    default <T> WorkflowResponse<T> newInstance(FlowExecution flowExecution){
        WorkflowResponse<T> response = new WorkflowResponse<>();
        response.setContextId(String.valueOf(flowExecution.getFlowExecutionId()));
        response.setRevision(flowExecution.getRevision());
        response.setFlowKey(flowExecution.getFlowKey());
        response.setConsume(
                Duration.between(
                        flowExecution.getStartTime(),
                        flowExecution.getEndTime()
                ).toMillis());
        if(flowExecution.getStatus() == ExecutionStatus.FAILED){
            response.fillResponse(flowExecution.getResponseCode());
            response.setMessage(flowExecution.getMessage());
            return response;
        }
        return response;
    }

    default <T> WorkflowResponse<T> newInstance(WorkflowRuntimeContext workflowRuntimeContext){
        WorkflowResponse<T> response = new WorkflowResponse<>();
        response.setContextId(String.valueOf(workflowRuntimeContext.getFlowExecution().getFlowExecutionId()));
        response.setRevision(workflowRuntimeContext.getFlowExecution().getRevision());
        response.setFlowKey(workflowRuntimeContext.getFlowExecution().getFlowKey());
        response.setConsume(
                Duration.between(
                        workflowRuntimeContext.getFlowExecution().getStartTime(),
                        workflowRuntimeContext.getFlowExecution().getEndTime()
                ).toMillis());
        if(workflowRuntimeContext.getFlowExecution().getStatus() == ExecutionStatus.FAILED){
            response.fillResponse(workflowRuntimeContext.getFlowExecution().getResponseCode());
            response.setMessage(workflowRuntimeContext.getFlowExecution().getMessage());
            return response;
        }
        return response;
    }

    String responseType();
}
