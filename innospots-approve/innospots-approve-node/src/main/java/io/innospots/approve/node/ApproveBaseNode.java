package io.innospots.approve.node;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.converter.ApproveExecutionConverter;
import io.innospots.approve.core.entity.ApproveExecutionEntity;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveActorOperator;
import io.innospots.approve.core.operator.ApproveExecutionOperator;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.enums.BuildStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/30
 */
public class ApproveBaseNode extends BaseNodeExecutor {

    protected ApproveActorOperator approveActorOperator;
    protected ApproveFlowInstanceOperator approveFlowInstanceOperator;
    protected ApproveExecutionOperator approveExecutionOperator;

    @Override
    protected void initialize() {
        this.approveExecutionOperator = this.getBean(ApproveExecutionOperator.class);
        this.approveActorOperator = this.getBean(ApproveActorOperator.class);
        this.approveFlowInstanceOperator = this.getBean(ApproveFlowInstanceOperator.class);
    }

    @Override
    protected NodeExecution prepare(FlowExecution flowExecution) {
        ApproveExecutionEntity ae = approveExecutionOperator.findApproveExecutionByFlowExecutionIdAndNodeKey(flowExecution.getFlowExecutionId(),this.nodeKey());
        NodeExecution nodeExecution = null;
        if(ae!=null){
            nodeExecution = ApproveExecutionConverter.newNodeExecution(ae,flowExecution);
            nodeExecution.setInputs(flowExecution.getInputs(null,nodeKey()));
        }else{
            nodeExecution = NodeExecution.buildNewNodeExecution(
                    nodeKey(),
                    flowExecution);
            nodeExecution.setNodeCode(ni.getCode());
            nodeExecution.setNodeName(ni.getName());
            nodeExecution.setInputs(this.buildExecutionInput(flowExecution));
        }
        flowExecution.addNodeExecution(nodeExecution);
        if (this.buildStatus != BuildStatus.DONE) {
            nodeExecution.end(buildException != null ? buildException.getMessage() : "build fail", ExecutionStatus.FAILED);
        }
        return nodeExecution;
    }


    protected ApproveFlowInstance getApproveFlowInstance(Map<String, Object> item) {
        ApproveFlowInstance approveFlowInstance = ApproveHolder.get();
        if (approveFlowInstance == null && item != null) {
            String approveInstanceId = (String) item.get(ApproveConstant.APPROVE_INSTANCE_KEY);
            approveFlowInstance = approveFlowInstanceOperator.findOne(approveInstanceId);
        }
        return approveFlowInstance;
    }

}
