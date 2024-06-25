package io.innospots.workflow.runtime.sse;

import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.model.node.NodeExecution;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/25
 */
public class SseEmitterNodeExecutionListener implements INodeExecutionListener {

    private NodeExecutionEmitter nodeExecutionEmitter;

    public SseEmitterNodeExecutionListener(NodeExecutionEmitter nodeExecutionEmitter) {
        this.nodeExecutionEmitter = nodeExecutionEmitter;
    }

    @Override
    public void start(NodeExecution nodeExecution) {

    }

    @Override
    public void complete(NodeExecution nodeExecution) {
        nodeExecutionEmitter.close(nodeExecution.getNodeExecutionId(),null);
    }

    @Override
    public void fail(NodeExecution nodeExecution) {

    }

    @Override
    public void item(NodeExecution nodeExecution, Map<String, Object> item) {
        nodeExecutionEmitter.sendItem(nodeExecution.getNodeExecutionId(),item);
    }

    @Override
    public void item(NodeExecution nodeExecution, ExecutionResource executionResource) {
        nodeExecutionEmitter.sendResources(nodeExecution.getNodeExecutionId(),executionResource);
    }

    @Override
    public void log(NodeExecution nodeExecution, Map<String, Object> log) {
        nodeExecutionEmitter.sendLog(nodeExecution.getNodeExecutionId(),log);
    }
}
