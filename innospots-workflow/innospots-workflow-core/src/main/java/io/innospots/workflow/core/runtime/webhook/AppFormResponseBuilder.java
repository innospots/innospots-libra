package io.innospots.workflow.core.runtime.webhook;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.execution.model.node.OutputDisplay;
import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        return response;
    }

    @Override
    public String responseType() {
        return "app";
    }
}
