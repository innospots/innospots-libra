package io.innospots.workflow.core.runtime.webhook;

import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.OutputDisplay;
import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * @author Smars
 * @date 2024/9/12
 */
public class AppFormResponseBuilder implements WorkflowResponseBuilder {

    @Override
    public WorkflowResponse<AppResponseBody> build(WorkflowRuntimeContext workflowRuntimeContext, FlowWebhookConfig webhookConfig) {
        WorkflowResponse<AppResponseBody> response = newInstance(workflowRuntimeContext);
        if (workflowRuntimeContext.getFlowExecution().getStatus() == ExecutionStatus.FAILED) {
            return response;
        }
        Integer page = (Integer) workflowRuntimeContext.getContexts().get("page");
        Integer size = (Integer) workflowRuntimeContext.getContexts().get("size");
        if (page == null) {
            page = 1;
        }
        if (size == null) {
            size = 50;
        }
        OutputDisplay outputDisplay = new OutputDisplay(page, size);
        outputDisplay.fill(workflowRuntimeContext.getFlowExecution().getOutput());

        AppResponseBody responseBody = new AppResponseBody();
        responseBody.setOutputs(outputDisplay);
        responseBody.setSchemaFields(ExecutionOutput.buildOutputField(outputDisplay.getResults().getList()));
        /*
        List<NodeExecution> nodeExecutions = workflowRuntimeContext.getFlowExecution().getLastNodeExecution();
        if (!nodeExecutions.isEmpty()) {
            NodeExecution nodeExecution = nodeExecutions.get(nodeExecutions.size() -1);
            AppResponseBody responseBody = new AppResponseBody();
            responseBody.setSchemaFields(nodeExecution.getSchemaFields());
            Integer page = (Integer) workflowRuntimeContext.getContexts().get("page");
            Integer size = (Integer) workflowRuntimeContext.getContexts().get("size");
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 50;
            }
            if (CollectionUtils.isNotEmpty(nodeExecution.getOutputs())) {
                OutputDisplay display = new OutputDisplay(nodeExecution.getOutputs().get(0), page, size);
                responseBody.setOutputs(display);
            }
            response.setBody(responseBody);
        }//end if nodeExecutions

         */
        response.setBody(responseBody);
        return response;
    }

    @Override
    public WorkflowResponse<AppResponseBody> newInstance(FlowExecution flowExecution) {
        WorkflowResponse workflowResponse = WorkflowResponseBuilder.super.newInstance(flowExecution);
        Integer page = (Integer) flowExecution.getContext("page");
        Integer size = (Integer) flowExecution.getContext("size");
        if (page == null) {
            page = 1;
        }
        if (size == null) {
            size = 50;
        }
        OutputDisplay outputDisplay = new OutputDisplay(page, size);
        outputDisplay.fill(flowExecution.getOutput());
        AppResponseBody responseBody = new AppResponseBody();
        responseBody.setOutputs(outputDisplay);
        responseBody.setSchemaFields(ExecutionOutput.buildOutputField(outputDisplay.getResults().getList()));
        workflowResponse.setBody(responseBody);
        return workflowResponse;
    }

    @Override
    public String responseType() {
        return "app";
    }
}
