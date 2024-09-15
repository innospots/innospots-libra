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
    public void item(String flowExecution, Map<String, Object> item) {
        FlowEmitter.item(flowExecution, item);
    }

    @Override
    public void resource(String flowExecution, ExecutionResource resource) {
        FlowEmitter.resource(flowExecution,resource);
    }

    @Override
    public void flowInfo(String sessionId, Object message) {
        FlowEmitter.log(sessionId,message);
    }

    @Override
    public void flowError(String sessionId, Object message) {
        FlowEmitter.logError(sessionId,message);
    }


}
