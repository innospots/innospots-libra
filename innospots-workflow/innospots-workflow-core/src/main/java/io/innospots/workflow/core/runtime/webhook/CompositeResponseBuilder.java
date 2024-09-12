package io.innospots.workflow.core.runtime.webhook;

import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2024/9/12
 */
public class CompositeResponseBuilder implements WorkflowResponseBuilder{


    private Map<String,WorkflowResponseBuilder> responseBuilderMap = new HashMap<>();
    private WorkflowResponseBuilder defaultResponseBuilder = new DefaultResponseBuilder();

    public CompositeResponseBuilder(WorkflowResponseBuilder... responseBuilders){
        for (WorkflowResponseBuilder responseBuilder : responseBuilders) {
            responseBuilderMap.put(responseBuilder.responseType(),responseBuilder);
        }
    }

    @Override
    public WorkflowResponse build(WorkflowRuntimeContext workflowRuntimeContext, FlowWebhookConfig webhookConfig) {
        return null;
    }

    @Override
    public String responseType() {
        return "composite";
    }
}
