package io.innospots.workflow.core.logger;

import io.innospots.base.execution.ExecutionResource;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/26
 */
public class CompositeFlowLogger implements IFlowLogger {

    private List<IFlowLogger> loggers;

    public CompositeFlowLogger() {
    }

    public CompositeFlowLogger(List<IFlowLogger> loggers) {
        this.loggers = loggers;
    }

    @Override
    public void item(String flowExecution, Map<String, Object> item) {
        loggers.forEach(logger -> logger.item(flowExecution, item));
    }

    @Override
    public void resource(String flowExecution, ExecutionResource resource) {
        loggers.forEach(logger -> logger.resource(flowExecution, resource));
    }

    @Override
    public void flowInfo(String sessionId, Object message) {
        loggers.forEach(logger -> logger.flowInfo(sessionId, message));
    }

    @Override
    public void flowError(String sessionId, Object message) {
        loggers.forEach(logger -> logger.flowError(sessionId, message));
    }

}
