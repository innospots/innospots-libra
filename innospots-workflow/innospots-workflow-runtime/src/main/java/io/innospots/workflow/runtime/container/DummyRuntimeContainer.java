package io.innospots.workflow.runtime.container;

import io.innospots.workflow.core.context.WorkflowRuntimeContext;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/19
 */
public class DummyRuntimeContainer extends BaseRuntimeContainer{

    private final Map<String, FlowRuntimeRegistry> startCache = new HashMap<>();


    public WorkflowRuntimeContext execute(String flowKey,Map<String,Object> payload){
        return execute(startCache.get(flowKey),payload,null);
    }

    @Override
    protected void updateTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        super.updateTrigger(flowRuntimeRegistry);
        startCache.put(flowRuntimeRegistry.getFlowKey(),flowRuntimeRegistry);
    }

    @Override
    protected void removeTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        super.removeTrigger(flowRuntimeRegistry);
        startCache.remove(flowRuntimeRegistry.getFlowKey());
    }

    @Override
    public void close() {
        startCache.clear();
    }
}
