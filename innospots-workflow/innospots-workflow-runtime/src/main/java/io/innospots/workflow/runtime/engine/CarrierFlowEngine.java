package io.innospots.workflow.runtime.engine;

import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.workflow.core.execution.listener.IFlowExecutionListener;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.manage.FlowManager;
import io.innospots.workflow.runtime.execution.ExecutionCarrier;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/27
 */
public class CarrierFlowEngine extends BaseFlowEngine{


    private ThreadTaskExecutor taskExecutor;

    public CarrierFlowEngine(List<IFlowExecutionListener> flowExecutionListeners, FlowManager flowManager) {
        super(flowExecutionListeners, flowManager);
        taskExecutor = ThreadPoolBuilder.build(8,32,0, "carrier-flow");
    }

    public CarrierFlowEngine(FlowManager flowManager) {
        this(null,flowManager);
    }

    @Override
    protected void execute(Flow flow, FlowExecution flowExecution) {
        ExecutionCarrier carrier = ExecutionCarrier.build(flow,flowExecution,taskExecutor);
        carrier.execute();
    }


}
