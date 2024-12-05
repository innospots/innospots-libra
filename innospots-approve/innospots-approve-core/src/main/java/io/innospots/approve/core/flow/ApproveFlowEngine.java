package io.innospots.approve.core.flow;

import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.engine.StreamFlowEngine;
import io.innospots.workflow.core.enums.FlowStatus;
import io.innospots.workflow.core.exception.FlowPrepareException;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.manage.FlowManager;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/19
 */
@Component("APPROVE_FlowEngine")
@Slf4j
public class ApproveFlowEngine extends StreamFlowEngine {

    protected ApproveFlowExecutionStoreListener approveFlowExecutionStoreListener;

    protected ApproveFlowInstanceOperator approveFlowInstanceOperator;



    public ApproveFlowEngine(ApproveFlowExecutionStoreListener approveFlowExecutionStoreListener,
                             FlowManager flowManager,
                             ApproveFlowInstanceOperator approveFlowInstanceOperator) {
        super(null, flowManager);
        this.approveFlowExecutionStoreListener = approveFlowExecutionStoreListener;
        this.flowManager = flowManager;
        this.approveFlowInstanceOperator = approveFlowInstanceOperator;
    }


    protected void execute(Flow flow, FlowExecution flowExecution) {
        if (!flow.isLoaded()) {
            return;
        }
        if (flowExecution.getStatus() != ExecutionStatus.PROCESSING) {
            log.warn("execution status is not process, can't execute this flow execution:{}", flowExecution);
            return;
        }
        super.execute(flow,flowExecution);
    }


    public Flow getFlow(String flowKey){
        return flowManager.loadFlow(flowKey);
    }


    @Override
    public boolean continueExecute(FlowExecution flowExecution) {
        execute(flowExecution);
        return true;
    }

    @Override
    protected void startFlow(Flow flow, FlowExecution flowExecution) {

        flowExecution.fillExecutionId(flow.getFlowKey());
        if (flow.getFlowStatus() == FlowStatus.LOADED &&
                (flowExecution.getStatus() == null ||
                        flowExecution.getStatus() == ExecutionStatus.STARTING ||
                        flowExecution.getStatus() == ExecutionStatus.READY)) {
            flowExecution.setStatus(ExecutionStatus.PROCESSING);
        } else if (flow.getFlowStatus() == FlowStatus.FAIL) {
            flowExecution.setStatus(ExecutionStatus.FAILED);
        } else {
            flowExecution.setStatus(ExecutionStatus.NOT_PREPARED);
        }

        approveFlowExecutionStoreListener.start(flowExecution);
        log.info("start flow execution:{}", flowExecution);
        ApproveFlowInstance approveFlowInstance = ApproveHolder.get();
        if (approveFlowInstance!=null &&
                (approveFlowInstance.getFlowExecutionId() == null ||
                !approveFlowInstance.getFlowExecutionId().equals(flowExecution.getFlowExecutionId()))) {
            log.info("bind flow execution:{} to approve instance:{}", flowExecution.getFlowExecutionId(), approveFlowInstance.getApproveInstanceKey());
            approveFlowInstanceOperator.bindFlowExecutionId(approveFlowInstance.getApproveInstanceKey(), flowExecution.getFlowExecutionId());
            approveFlowInstance.setFlowExecutionId(flowExecution.getFlowExecutionId());
        }
    }


    @Override
    protected void completeFlow(FlowExecution flowExecution,boolean isUpdate) {
        ApproveFlowInstance flowInstance = ApproveHolder.get();
        ApproveStatus approveStatus = flowInstance.getApproveStatus();
//        ApproveStatus approveStatus = approveFlowInstanceOperator.getApproveStatusByFlowExecutionId(flowExecution.getFlowExecutionId());
        if (approveStatus == ApproveStatus.PROCESSING ||approveStatus == ApproveStatus.STARTING) {
            return;
        }
        if (approveStatus == ApproveStatus.APPROVED ||
                approveStatus == ApproveStatus.REVOKED ||
                approveStatus == ApproveStatus.EXPIRED ||
                approveStatus == ApproveStatus.REJECTED) {
            flowExecution.setStatus(ExecutionStatus.COMPLETE);
            flowExecution.setEndTime(LocalDateTime.now());
        } else if (approveStatus == ApproveStatus.STOPPED) {
            flowExecution.setStatus(ExecutionStatus.STOPPED);
            flowExecution.setEndTime(LocalDateTime.now());
        } else if (approveStatus == ApproveStatus.FAILED) {
            flowExecution.setResponseCode(ResponseCode.FAIL);
            flowExecution.setEndTime(LocalDateTime.now());
        }
        approveFlowExecutionStoreListener.update(flowExecution);
    }

    @Override
    protected void executeNextNode(String shouldExecuteNode, Flow flow, FlowExecution flowExecution) {
        BaseNodeExecutor baseAppNode = flow.findNode(shouldExecuteNode);
        //add to executable node list
        List<BaseNodeExecutor> nodeExecutors = new ArrayList<>();
        nodeExecutors.add(baseAppNode);
        traverseExecuteNode(nodeExecutors, flow, flowExecution);
    }

    @Override
    protected NodeExecution executeNode(BaseNodeExecutor nodeExecutor, FlowExecution flowExecution) {
        NodeExecution nodeExecution = super.executeNode(nodeExecutor, flowExecution);
        log.info("execute node:{}", nodeExecution);
        this.approveFlowInstanceOperator.updateCurrentNodeKey(ApproveHolder.get().getApproveInstanceKey(), nodeExecutor.nodeKey());
        log.info("update approve flow instance current node,approveInstanceKey: {}, nodeKey:{}", ApproveHolder.get().getApproveInstanceKey(), nodeExecutor.nodeKey());
        return nodeExecution;
    }
}
