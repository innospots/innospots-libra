package io.innospots.workflow.core.runtime.webhook;

import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2024/9/12
 */
public class CompositeResponseBuilder implements WorkflowResponseBuilder{


    private final Map<String,WorkflowResponseBuilder> responseBuilderMap = new HashMap<>();
    private final WorkflowResponseBuilder defaultResponseBuilder = new DefaultResponseBuilder();

    public CompositeResponseBuilder(WorkflowResponseBuilder... responseBuilders){
        for (WorkflowResponseBuilder responseBuilder : responseBuilders) {
            responseBuilderMap.put(responseBuilder.responseType(),responseBuilder);
        }
    }

    @Override
    public <T> WorkflowResponse<T> build(WorkflowRuntimeContext workflowRuntimeContext, FlowWebhookConfig webhookConfig) {
        String responseType = workflowRuntimeContext.getResponseType();
        if(responseType == null || !responseBuilderMap.containsKey(responseType)){
            return defaultResponseBuilder.build(workflowRuntimeContext, webhookConfig);
        }
        return responseBuilderMap.get(responseType).build(workflowRuntimeContext, webhookConfig);
    }

    @Override
    public String responseType() {
        return "composite";
    }
}
