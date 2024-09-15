package io.innospots.workflow.core.logger;

import io.innospots.base.execution.ExecutionResource;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/26
 */
@Slf4j
public class NopFlowLogger implements IFlowLogger {

    @Override
    public void item(String flowExecution, Map<String, Object> item) {

    }

    @Override
    public void resource(String flowExecution, ExecutionResource resource) {

    }

    @Override
    public void flowInfo(String sessionId, Object message) {
        log.info("executionId:{}, {}",sessionId,message);
    }

    @Override
    public void flowError(String sessionId, Object message) {
        log.error("executionId:{}, {}",sessionId,message);
    }
}
