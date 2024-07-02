package io.innospots.workflow.core.sse;

import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.logger.IFlowLogger;

import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/26
 */
public class SseFlowLogger implements IFlowLogger {

    @Override
    public void item(String flowExecution, String nodeExecutionId, Map<String, Object> item) {
        FlowEmitter.nodeItem(flowExecution, nodeExecutionId, item);
    }

    @Override
    public void resource(String flowExecution, String nodeExecutionId, ExecutionResource resource) {
        FlowEmitter.nodeResource(flowExecution, nodeExecutionId,resource);
    }

    @Override
    public void flowInfo(String sessionId, Object message) {
        FlowEmitter.log(sessionId,message);
    }

    @Override
    public void flowError(String sessionId, Object message) {
        FlowEmitter.logError(sessionId,message);
    }

    @Override
    public void nodeInfo(String sessionId, Object message) {
        FlowEmitter.logNode(sessionId,message);
    }

    @Override
    public void nodeError(String sessionId, Object message) {
        FlowEmitter.logNodeError(sessionId,message);
    }

}
