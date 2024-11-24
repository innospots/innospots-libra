package io.innospots.approve.core.flow;

import io.innospots.approve.core.operator.ApproveExecutionOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Slf4j
@Component
public class ApproveNodeExecutionListener implements INodeExecutionListener {

    private ApproveExecutionOperator approveExecutionOperator;

    public ApproveNodeExecutionListener(ApproveExecutionOperator approveExecutionOperator) {
        this.approveExecutionOperator = approveExecutionOperator;
    }

    @Override
    public void start(NodeExecution nodeExecution) {
        log.debug("node execution start time:{} {}", LocalDateTime.now(), nodeExecution);
        nodeExecution.setStartTime(LocalDateTime.now());
    }

    @Override
    public void complete(NodeExecution nodeExecution) {
        approveExecutionOperator.createExecution(nodeExecution, ApproveHolder.getActor());
    }

    @Override
    public void fail(NodeExecution nodeExecution) {
        approveExecutionOperator.createExecution(nodeExecution, ApproveHolder.getActor());
    }
}
