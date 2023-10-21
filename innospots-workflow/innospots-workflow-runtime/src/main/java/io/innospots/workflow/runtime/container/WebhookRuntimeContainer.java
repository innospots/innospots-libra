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

package io.innospots.workflow.runtime.container;

import io.innospots.base.exception.ResourceException;
import io.innospots.workflow.core.context.WorkflowRuntimeContext;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;
import io.innospots.workflow.core.webhook.FlowWebhookConfig;
import io.innospots.workflow.core.webhook.WebhookPayload;
import io.innospots.workflow.core.webhook.WorkflowResponse;
import io.innospots.workflow.core.webhook.WorkflowResponseBuilder;
import io.innospots.workflow.node.app.trigger.ApiTriggerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/10
 */
public class WebhookRuntimeContainer extends BaseRuntimeContainer {

    private static final Logger logger = LoggerFactory.getLogger(WebhookRuntimeContainer.class);

    protected WorkflowResponseBuilder workflowResponseBuilder;


    private Map<String, FlowRuntimeRegistry> triggerPaths = new HashMap<>();

    public WebhookRuntimeContainer(WorkflowResponseBuilder workflowResponseBuilder) {
        this.workflowResponseBuilder = workflowResponseBuilder;

    }

    public WorkflowResponse execute(WebhookPayload webhookPayload) {
        FlowRuntimeRegistry triggerInfo = triggerPaths.get(webhookPayload.getFlowKey());
        if (triggerInfo == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "api flow trigger not find, maybe not be published, key:" + webhookPayload.getFlowKey());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("run trigger,{}:{} {}", triggerInfo.key(), triggerInfo, webhookPayload);
        }

        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(
                triggerInfo.getWorkflowInstanceId(),
                triggerInfo.getRevision());
        flowExecution.setSource(triggerInfo.getRegistryNode().nodeCode());
        flowExecution.setInput(webhookPayload.toExecutionInput());

        WorkflowRuntimeContext workflowRuntimeContext = WorkflowRuntimeContext.build(flowExecution);
        execute(workflowRuntimeContext);

        return workflowResponseBuilder.build(workflowRuntimeContext, ((ApiTriggerNode) triggerInfo.getRegistryNode()).getFlowWebhookConfig());
    }

    public WorkflowResponse run(String path, FlowWebhookConfig.RequestMethod method, Map<String, Object> payload, Map<String, Object> context) {
        FlowRuntimeRegistry triggerInfo = triggerPaths.get(path);
        if (triggerInfo == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "api flow trigger not find, maybe not be published, key:" + path);
        }

        ApiTriggerNode triggerNode = (ApiTriggerNode) triggerInfo.getRegistryNode();
        if (triggerNode.getFlowWebhookConfig().getRequestMethod() != method) {
            throw ResourceException.buildNotExistException(this.getClass(), "Resource not found, path: " + path + " , method: " + method);
        }

        WorkflowRuntimeContext workflowRuntimeContext = execute(triggerInfo, payload, context);

        return workflowResponseBuilder.build(workflowRuntimeContext, ((ApiTriggerNode) triggerInfo.getRegistryNode()).getFlowWebhookConfig());

    }

    public WorkflowResponse run(String path, Map<String, Object> payload, Map<String, Object> context) {
        return run(path, FlowWebhookConfig.RequestMethod.POST, payload, context);
    }


    @Override
    protected void updateTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        super.updateTrigger(flowRuntimeRegistry);
        ApiTriggerNode apiTriggerNode = (ApiTriggerNode) flowRuntimeRegistry.getRegistryNode();
        String path = apiTriggerNode.apiPath();
        triggerPaths.put(path, flowRuntimeRegistry);
    }

    @Override
    protected void removeTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        super.removeTrigger(flowRuntimeRegistry);
        ApiTriggerNode apiTriggerNode = (ApiTriggerNode) flowRuntimeRegistry.getRegistryNode();
        triggerPaths.remove(apiTriggerNode.apiPath());
    }

    @Override
    public void close() {
        logger.info("close event runtime container.");
        this.triggerPaths.clear();
    }
}
