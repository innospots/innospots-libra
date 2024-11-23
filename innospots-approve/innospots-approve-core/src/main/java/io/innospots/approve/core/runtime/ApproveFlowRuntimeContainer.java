package io.innospots.approve.core.runtime;

import io.innospots.approve.core.flow.ApproveFlowEngine;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveForm;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.manage.FlowManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
@Component
public class ApproveFlowRuntimeContainer {

    private ApproveFlowInstanceOperator flowInstanceOperator;

    private ApproveFlowEngine approveFlowEngine;

    public ApproveFlowRuntimeContainer(ApproveFlowInstanceOperator flowInstanceOperator,
                                       ApproveFlowEngine approveFlowEngine) {
        this.flowInstanceOperator = flowInstanceOperator;
        this.approveFlowEngine = approveFlowEngine;
    }

    public ApproveFlowInstance execute(String approveInstanceKey, Map<String,Object> data) {
        //start execute approve flow
        ApproveFlowInstance flowInstance = flowInstanceOperator.findOne(approveInstanceKey);
        Flow flow = approveFlowEngine.getFlow(flowInstance.getFlowKey());
        List<Map<String,Object>> payload = new ArrayList<>();
        payload.add(data);
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(flow.getWorkflowInstanceId(), flow.getRevision(),payload);
        ApproveHolder.set(flowInstance);
        approveFlowEngine.execute(flowExecution);
        ApproveHolder.remove();
        return flowInstance;
    }

    public ApproveFlowInstance start(String approveInstanceKey) {
        //start execute approve flow
        ApproveFlowInstance flowInstance = flowInstanceOperator.start(approveInstanceKey);
        Flow flow = approveFlowEngine.getFlow(flowInstance.getFlowKey());
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(flow.getWorkflowInstanceId(), flow.getRevision());
        ApproveHolder.set(flowInstance);
        approveFlowEngine.execute(flowExecution);
        ApproveHolder.remove();
        return flowInstance;
    }



}
