package io.innospots.approve.node;

import io.innospots.approve.core.enums.ApproveResult;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveExecution;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
public class ApproveEndNode extends ApproveBaseNode {

    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        ExecutionOutput output = this.buildOutput(nodeExecution);
        List<ApproveExecution> executions = this.listPrevApproveExecution(flowExecution.getFlowExecutionId());
        boolean approved = executions.stream()
                .allMatch(ae -> Objects.equals(ae.getApproveResult(), ApproveResult.APPROVED));
        ApproveExecution execution = executions.stream().sorted().findFirst().get();
        ApproveFlowInstance flowInstance = ApproveHolder.get();
        flowInstance.setApprover(execution.getUserName());
        flowInstance.setApproverId(execution.getUserId());
        flowInstance.setMessage(execution.getMessage());
        flowInstance.setLastApproveDateTime(execution.getEndTime());
        flowInstance.setEndTime(LocalDateTime.now());
        flowInstance.setMessage(execution.getMessage());
        flowInstance.setApproveStatus(approved ? ApproveStatus.APPROVED : ApproveStatus.REJECTED);
        flowInstance.setResult(execution.getResult());
        output.addResult(execution.toInfo());
        for (ExecutionInput input : nodeExecution.getInputs()) {
            for (Map<String, Object> item : input.getData()) {
                flowExecution.addOutput(item);
            }
        }
        this.approveFlowInstanceOperator.save(flowInstance);
    }

}
