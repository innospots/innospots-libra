package io.innospots.approve.node;

import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
public class ApproveNoticeNode extends ApproveBaseNode {


    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        ExecutionOutput nodeOutput = this.buildOutput(nodeExecution);
        this.prevNodeApproved(flowExecution.getFlowExecutionId());
        super.invoke(nodeExecution, flowExecution);
    }
}
