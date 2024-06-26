package io.innospots.workflow.core.logger;

import io.innospots.base.execution.ExecutionResource;

import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/26
 */
public class NopFlowLogger implements IFlowLogger {

    @Override
    public void item(String flowExecution, String nodeExecutionId, Map<String, Object> item) {

    }

    @Override
    public void resource(String flowExecution, String nodeExecutionId, ExecutionResource resource) {

    }

    @Override
    public void flowInfo(String sessionId, Object message) {

    }

    @Override
    public void flowError(String sessionId, Object message) {

    }

    @Override
    public void nodeInfo(String sessionId, Object message) {

    }

    @Override
    public void nodeError(String sessionId, Object message) {

    }
}
