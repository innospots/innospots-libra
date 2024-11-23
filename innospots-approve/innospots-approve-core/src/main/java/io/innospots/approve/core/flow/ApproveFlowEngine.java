package io.innospots.approve.core.flow;

import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.engine.IFlowEngine;
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
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/19
 */
@Component
@Slf4j
public class ApproveFlowEngine implements IFlowEngine {

    protected ApproveFlowExecutionStoreListener approveFlowExecutionStoreListener;

    protected FlowManager flowManager;

    protected ApproveFlowInstanceOperator approveFlowInstanceOperator;

    public ApproveFlowEngine(ApproveFlowExecutionStoreListener approveFlowExecutionStoreListener,
                             FlowManager flowManager,
                             ApproveFlowInstanceOperator approveFlowInstanceOperator) {
        this.approveFlowExecutionStoreListener = approveFlowExecutionStoreListener;
        this.flowManager = flowManager;
        this.approveFlowInstanceOperator = approveFlowInstanceOperator;
    }

    @Override
    public BuildProcessInfo prepare(Long flowInstanceId, Integer version, boolean force) throws FlowPrepareException {
        Flow flow = flowManager.loadFlow(flowInstanceId, version, force, false);
        return flow.getBuildProcessInfo();
    }

    @Override
    public void execute(FlowExecution flowExecution) {
        Flow flow = getFlow(flowExecution);

        startFlow(flow, flowExecution);
        try {
            execute(flow, flowExecution);
            /*
            if (flow.getFlowStatus() == FlowStatus.LOADED) {
                execute(flow, flowExecution);
            } else {
                failExecution(flow, flowExecution);
            }
             */
        } catch (Exception e) {
            log.error("flow execution fail!", e);
            flowExecution.setStatus(ExecutionStatus.FAILED);
            flowExecution.setResponseCode(ResponseCode.FAIL);
        }
        if (flow.getFlowStatus() != FlowStatus.LOADED) {
            failExecution(flow, flowExecution);
        } else {
            completeFlow(flowExecution);
        }
    }

    protected void execute(Flow flow, FlowExecution flowExecution) {
        if (!flow.isLoaded()) {
            return;
        }
        if (flowExecution.getStatus() != ExecutionStatus.PROCESSING) {
            log.warn("execution status is not process, can't execute this flow execution:{}", flowExecution);
            return;
        }
        ApproveFlowInstance approveFlowInstance = ApproveHolder.get();
        try {
            List<BaseNodeExecutor> nodeExecutors = null;
            /*
            if (flowExecution.getExecuteTimes() == 1 && flowExecution.getCurrentNodeKeys() == null) {
                nodeExecutors = flow.startNodes();
            } else {
                nodeExecutors = flow.nextNodes(flowExecution.getCurrentNodeKeys());
            }
             */

            if (approveFlowInstance.getNextNodeKeys() == null) {
                nodeExecutors = flow.startNodes();
            } else {
                nodeExecutors = flow.findNodes(approveFlowInstance.getNextNodeKeys());
            }

            for (BaseNodeExecutor nodeExecutor : nodeExecutors) {
                nodeExecutor.execute(flowExecution);
                flowExecution.addCurrentNodeKey(nodeExecutor.nodeKey());
            }

        } catch (Exception e) {
            log.error("flow execution fail!", e);
            flowExecution.setStatus(ExecutionStatus.FAILED);
        }
    }


    protected void failExecution(Flow flow, FlowExecution flowExecution) {
        BuildProcessInfo buildProcessInfo = flow.getBuildProcessInfo();
        log.error("flow prepare failed, {}", buildProcessInfo);
        if (buildProcessInfo.getBuildException() != null) {
            flowExecution.setMessage(buildProcessInfo.errorMessage());
            flowExecution.setResponseCode(ResponseCode.RESOURCE_BUILD_ERROR);
        } else {
            for (Map.Entry<String, Exception> exceptionEntry : buildProcessInfo.getErrorInfo().entrySet()) {
                NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution(exceptionEntry.getKey(), flowExecution);
                nodeExecution.setStartTime(LocalDateTime.now());
                nodeExecution.setEndTime(LocalDateTime.now());
                nodeExecution.setStatus(ExecutionStatus.FAILED);
                nodeExecution.setMessage(buildProcessInfo.getBuildMessage(exceptionEntry.getKey()));
                flowExecution.addNodeExecution(nodeExecution);
                if (CollectionUtils.isNotEmpty(flowManager.nodeExecutionListeners())) {
                    flowManager.nodeExecutionListeners().forEach(
                            listener -> listener.fail(nodeExecution)
                    );
                }
            }
        }
    }

    @Override
    public FlowExecution stop(String flowExecutionId) {
        return null;
    }

    @Override
    public FlowExecution stopByFlowKey(String flowKey) {
        return null;
    }

    @Override
    public boolean continueExecute(FlowExecution flowExecution) {
        execute(flowExecution);
        return true;
    }

    private void startFlow(Flow flow, FlowExecution flowExecution) {

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
        ApproveFlowInstance approveFlowInstance = ApproveHolder.get();
        if (approveFlowInstance.getFlowExecutionId() == null) {
            approveFlowInstanceOperator.bindFlowExecutionId(approveFlowInstance.getApproveInstanceKey(), flowExecution.getFlowExecutionId());
            approveFlowInstance.setFlowExecutionId(flowExecution.getFlowExecutionId());
        }
    }

    private void completeFlow(FlowExecution flowExecution) {
        ApproveFlowInstance flowInstance = ApproveHolder.get();
        ApproveStatus approveStatus = flowInstance.getApproveStatus();
//        ApproveStatus approveStatus = approveFlowInstanceOperator.getApproveStatusByFlowExecutionId(flowExecution.getFlowExecutionId());
        if (approveStatus == ApproveStatus.PROCESSING) {
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

    public Flow getFlow(String flowKey){
        return flowManager.loadFlow(flowKey);
    }

    protected Flow getFlow(FlowExecution flowExecution) {
        Flow flow = null;
        if (flowExecution.getFlowKey() != null) {
            flow = flowManager.loadFlow(flowExecution.getFlowKey());
        } else {
            flow = flowManager.loadFlow(flowExecution.getFlowInstanceId(), flowExecution.getRevision());
        }

        if (flow == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "flow not exist: " + flowExecution.getFlowInstanceId() + " ,revision: " + flowExecution.getRevision());
        }
        if (!flow.isLoaded()) {
            log.warn("the flow is not loaded completed, {},{}", flow.getWorkflowInstanceId(), flow.getRevision());
            flowExecution.setStatus(ExecutionStatus.NOT_PREPARED);
        }
        flowExecution.setTotalCount(flow.nodeSize());
        return flow;
    }


}
