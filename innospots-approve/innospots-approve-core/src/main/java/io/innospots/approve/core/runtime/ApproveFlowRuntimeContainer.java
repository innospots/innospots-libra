package io.innospots.approve.core.runtime;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.enums.ApproveResult;
import io.innospots.approve.core.flow.ApproveFlowEngine;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveActorOperator;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.reader.FlowExecutionReader;
import io.innospots.workflow.core.flow.Flow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
@Slf4j
@Component
public class ApproveFlowRuntimeContainer {

    private final ApproveFlowInstanceOperator flowInstanceOperator;

    private final ApproveFlowEngine approveFlowEngine;

    private final FlowExecutionReader flowExecutionReader;

    private final ApproveActorOperator actorOperator;

    public ApproveFlowRuntimeContainer(ApproveFlowInstanceOperator flowInstanceOperator,
                                       ApproveFlowEngine approveFlowEngine,
                                       FlowExecutionReader flowExecutionReader, ApproveActorOperator actorOperator) {
        this.flowInstanceOperator = flowInstanceOperator;
        this.approveFlowEngine = approveFlowEngine;
        this.flowExecutionReader = flowExecutionReader;
        this.actorOperator = actorOperator;
    }

    public ApproveFlowInstance execute(String approveInstanceKey, Map<String,Object> data) {
        //start execute approve flow
        ApproveFlowInstance flowInstance = flowInstanceOperator.findOne(approveInstanceKey);
        data.put(ApproveConstant.APPROVE_INSTANCE_KEY,flowInstance.getApproveInstanceKey());
        execute(flowInstance,data);
        return flowInstance;
    }

    public ApproveFlowInstance start(String approveInstanceKey) {
        //start execute approve flow
        ApproveFlowInstance flowInstance = flowInstanceOperator.start(approveInstanceKey);
        Map<String,Object> data = new HashMap<>();
        data.put(ApproveConstant.APPROVE_INSTANCE_KEY,flowInstance.getApproveInstanceKey());
        execute(flowInstance,data);
        return flowInstance;
    }


    public boolean revoke(String approveInstanceKey, String message){
        boolean result = flowInstanceOperator.revoke(approveInstanceKey,message);
        if(result){
            result = actorOperator.cancelApprove(approveInstanceKey);
        }
        return result;
    }

    public boolean reject(String approveInstanceKey, String message){
        execute(approveInstanceKey,buildData(message,ApproveResult.REJECTED));
        return flowInstanceOperator.reject(approveInstanceKey,message);
    }

    public boolean approve(String approveInstanceKey, String message){
        execute(approveInstanceKey,buildData(message,ApproveResult.APPROVED));
        return true;
        //return flowInstanceOperator.approve(approveInstanceKey,message);
    }

    private Map<String, Object> buildData(String message, ApproveResult approveResult){
        Map<String,Object> data = new HashMap<>();
        data.put(ApproveConstant.APPROVE_MESSAGE,message);
        data.put(ApproveConstant.APPROVE_RESULT,approveResult.name());
        return data;
    }


    private void execute(ApproveFlowInstance flowInstance,Map<String,Object> data){
        Flow flow = approveFlowEngine.getFlow(flowInstance.getFlowKey());
        List<Map<String,Object>> payloads = new ArrayList<>();
        payloads.add(data);
        FlowExecution flowExecution = null;
        if(flowInstance.getFlowExecutionId() == null){
            flowExecution = FlowExecution.buildNewFlowExecution(flow.getWorkflowInstanceId(), flow.getRevision(),payloads);
            flowExecution.setSkipNodeExecution(true);
        }else{
            flowExecution = flowExecutionReader.getFlowExecutionById(flowInstance.getFlowExecutionId());
            flowExecution.getInput().clear();
            flowExecution.addInput(data);
        }
        ApproveHolder.set(flowInstance);
        log.info("execute flow:{}",flowInstance);
        approveFlowEngine.execute(flowExecution);
        ApproveHolder.remove();
    }



}
