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

    @Override
    public void start(NodeExecution nodeExecution) {

    }

    @Override
    public void complete(NodeExecution nodeExecution) {
    }

    @Override
    public void fail(NodeExecution nodeExecution) {

    }

    @Override
    public void item(NodeExecution nodeExecution, Map<String, Object> item) {

    }

    @Override
    public void item(NodeExecution nodeExecution, ExecutionResource executionResource) {
        INodeExecutionListener.super.item(nodeExecution, executionResource);
    }

    @Override
    public void log(NodeExecution nodeExecution, Map<String, Object> log) {
        INodeExecutionListener.super.log(nodeExecution, log);
    }
}
