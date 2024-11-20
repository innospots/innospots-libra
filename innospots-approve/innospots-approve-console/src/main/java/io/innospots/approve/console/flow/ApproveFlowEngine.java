package io.innospots.approve.console.flow;

import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.exception.FlowPrepareException;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/19
 */
public class ApproveFlowEngine implements IFlowEngine {

    @Override
    public BuildProcessInfo prepare(Long flowInstanceId, Integer version, boolean force) throws FlowPrepareException {
        return null;
    }

    @Override
    public void execute(FlowExecution flowExecution) {

    }

    @Override
    public boolean continueExecute(FlowExecution flowExecution) {
        return false;
    }

    @Override
    public FlowExecution stop(String flowExecutionId) {
        return null;
    }

    @Override
    public FlowExecution stopByFlowKey(String flowKey) {
        return null;
    }
}
