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

package io.innospots.workflow.core.webhook;


import cn.hutool.http.HttpStatus;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.context.WorkflowRuntimeContext;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.node.NodeExecution;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/3/12
 */
public class DefaultResponseBuilder implements WorkflowResponseBuilder {


    @Override
    public WorkflowResponse build(WorkflowRuntimeContext workflowRuntimeContext, FlowWebhookConfig webhookConfig) {
        WorkflowResponse response = new WorkflowResponse();
        response.setContextId(String.valueOf(workflowRuntimeContext.getFlowExecution().getFlowExecutionId()));
        response.setRevision(workflowRuntimeContext.getFlowExecution().getRevision());
        response.setFlowKey(workflowRuntimeContext.getFlowExecution().getFlowKey());
        List<NodeExecution> nodeExecutions = workflowRuntimeContext.getFlowExecution().getLastNodeExecution();
        response.setConsume(
                Duration.between(
                        workflowRuntimeContext.getFlowExecution().getStartTime(),
                        workflowRuntimeContext.getFlowExecution().getEndTime()
                ).toMillis());
        response.setResponseTime(workflowRuntimeContext.getFlowExecution().getEndTime());
        if(workflowRuntimeContext.getFlowExecution().getStatus() == ExecutionStatus.FAILED){
            response.setCode(workflowRuntimeContext.getFlowExecution().getResultCode());
            response.setBody(workflowRuntimeContext.getFlowExecution().getResult());
            return response;
        }else{
            if (webhookConfig == null) {
                return response;
            }
            response.setCode(webhookConfig.getResponseCode());
        }

        if (webhookConfig.getResponseMode() == FlowWebhookConfig.ResponseMode.ACK) {
            Map<String, Object> body = new HashMap<>(7);
            for (ParamField responseField : webhookConfig.getResponseFields()) {
                body.put(responseField.getCode(), responseField.getValue());
            }
            response.setBody(body);
        } else {
            if (!nodeExecutions.isEmpty()) {
                List<Map<String, Object>> outList = nodeExecutions.stream().
                        flatMap(execution -> execution.getOutputs().stream())
                        .flatMap(nodeOutput -> nodeOutput.getResults().stream())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(outList)) {
                    if (webhookConfig.getResponseData() == FlowWebhookConfig.ResponseData.ALL) {
                        response.setBody(outList);
                    } else {
                        response.setBody(outList.get(0));
                    }
                }//end if outList
            }//end if nodeExecutions
        }//end if responseMode

        return response;
    }
}
