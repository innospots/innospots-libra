package io.innospots.workflow.core.execution;

import io.innospots.base.events.EventBody;
import io.innospots.workflow.core.execution.flow.FlowExecution;

/**
 * @author Smars
 * @date 2023/8/7
 */
public class FlowExecutionTaskEvent extends EventBody {

    public static FlowExecutionTaskEvent build(FlowExecution flowExecution){
        return new FlowExecutionTaskEvent(flowExecution);
    }

    public FlowExecutionTaskEvent(Object body) {
        super(body);
    }

    public FlowExecution flowExecution(){
        if(this.body!=null){
            return (FlowExecution) this.body;
        }
        return null;
    }
}
