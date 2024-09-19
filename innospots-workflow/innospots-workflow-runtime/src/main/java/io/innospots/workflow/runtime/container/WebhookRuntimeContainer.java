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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.reader.FlowExecutionReader;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;
import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;
import io.innospots.workflow.core.runtime.webhook.AppFormResponseBuilder;
import io.innospots.workflow.core.runtime.webhook.FlowWebhookConfig;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponse;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponseBuilder;
import io.innospots.workflow.core.sse.FlowEmitter;
import io.innospots.workflow.node.app.trigger.ApiTriggerNode;
import io.innospots.workflow.runtime.webhook.WebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @date 2021/3/10
 */
public class WebhookRuntimeContainer extends BaseRuntimeContainer {

    private static final Logger logger = LoggerFactory.getLogger(WebhookRuntimeContainer.class);

    private final ThreadTaskExecutor taskExecutor;

    private Cache<String, CompletableFuture<WorkflowResponse>> webhookFutures = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES).build();

    protected WorkflowResponseBuilder workflowResponseBuilder;

    private FlowExecutionReader flowExecutionReader;

    private Map<String, FlowRuntimeRegistry> triggerPaths = new HashMap<>();

    public WebhookRuntimeContainer(WorkflowResponseBuilder workflowResponseBuilder) {
        this.workflowResponseBuilder = workflowResponseBuilder;
        taskExecutor = ThreadPoolBuilder.build(16, 16, 10000, "webhook-runtime-container");
    }

    public WorkflowResponse getResponseByContextId(String contextId) {
        CompletableFuture<WorkflowResponse> cf = webhookFutures.getIfPresent(contextId);
        if (cf != null) {
            return cf.join();
        }
        FlowExecution flowExecution = flowExecutionReader.getFlowExecutionById(contextId);
        WorkflowResponse response;
        if(flowExecution == null){
            response = new WorkflowResponse();
            response.setContextId(contextId);
            response.fillResponse(ResponseCode.FAIL);
            response.setMessage("response not exist.");
        }else{
            AppFormResponseBuilder responseBuilder = new AppFormResponseBuilder();
            response = responseBuilder.newInstance(flowExecution);
        }
        return response;
    }

    public WorkflowResponse asyncExecute(WebhookPayload webhookPayload) {
        FlowRuntimeRegistry triggerInfo = triggerPaths.get(webhookPayload.getPath());
        WorkflowRuntimeContext workflowRuntimeContext = buildContext(webhookPayload, triggerInfo);
        CompletableFuture<WorkflowResponse> cf = CompletableFuture.supplyAsync(() -> {
            execute(workflowRuntimeContext);
            return workflowResponseBuilder.build(workflowRuntimeContext, ((ApiTriggerNode) triggerInfo.getRegistryNode()).getFlowWebhookConfig());
        }, taskExecutor);
        WorkflowResponse asyncResp = new WorkflowResponse();
        asyncResp.setFlowKey(triggerInfo.getFlowKey());
        asyncResp.setContextId(workflowRuntimeContext.getFlowExecution().getFlowExecutionId());
        asyncResp.setRevision(triggerInfo.getRevision());
        asyncResp.fillResponse(ResponseCode.ASYNC_RUNNING);
        FlowEmitter.createResponseEmitter(asyncResp.getContextId());
        FlowEmitter.createExecutionLogEmitter(asyncResp.getContextId());
        webhookFutures.put(asyncResp.getContextId(),cf);
        return asyncResp;
    }

    public WorkflowResponse execute(WebhookPayload webhookPayload) {
        FlowRuntimeRegistry triggerInfo = triggerPaths.get(webhookPayload.getPath());
        WorkflowRuntimeContext workflowRuntimeContext = buildContext(webhookPayload, triggerInfo);
        execute(workflowRuntimeContext);

        return workflowResponseBuilder.build(workflowRuntimeContext, ((ApiTriggerNode) triggerInfo.getRegistryNode()).getFlowWebhookConfig());
    }

    private WorkflowRuntimeContext buildContext(WebhookPayload webhookPayload, FlowRuntimeRegistry triggerInfo) {
        if (triggerInfo == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "api flow trigger not find, maybe not be published, path:" + webhookPayload.getPath());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("run trigger,{}:{} {}", triggerInfo.key(), triggerInfo, webhookPayload);
        }

        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(
                triggerInfo.getWorkflowInstanceId(),
                triggerInfo.getRevision());
        flowExecution.fillExecutionId(triggerInfo.getFlowKey());
        flowExecution.setSource(triggerInfo.getRegistryNode().nodeCode());
        flowExecution.setInput(webhookPayload.toExecutionInput());
        Object respType = webhookPayload.responseType();
        WorkflowRuntimeContext workflowRuntimeContext = WorkflowRuntimeContext.build(flowExecution);
        if (respType != null) {
            workflowRuntimeContext.setResponseType(respType.toString());
        }
        return workflowRuntimeContext;
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
