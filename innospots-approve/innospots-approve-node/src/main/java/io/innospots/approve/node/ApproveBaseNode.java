package io.innospots.approve.node;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveActorOperator;
import io.innospots.approve.core.operator.ApproveExecutionOperator;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
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

    protected ApproveFlowInstance getApproveFlowInstance(Map<String, Object> item) {
        ApproveFlowInstance approveFlowInstance = ApproveHolder.get();
        if (approveFlowInstance == null && item != null) {
            String approveInstanceId = (String) item.get(ApproveConstant.APPROVE_INSTANCE_KEY);
            approveFlowInstance = approveFlowInstanceOperator.findOne(approveInstanceId);
        }
        return approveFlowInstance;
    }

}
