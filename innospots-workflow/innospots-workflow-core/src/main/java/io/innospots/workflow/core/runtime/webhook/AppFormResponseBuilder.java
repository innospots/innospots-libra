package io.innospots.workflow.core.runtime.webhook;

import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;

/**
 * @author Smars
 * @date 2024/9/12
 */
public class AppFormResponseBuilder implements WorkflowResponseBuilder {

    @Override
    public WorkflowResponse build(WorkflowRuntimeContext workflowRuntimeContext, FlowWebhookConfig webhookConfig) {
        return null;
    }

    @Override
    public String responseType() {
        return "app";
    }
}
