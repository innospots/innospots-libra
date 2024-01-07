package io.innospots.workflow.console.task.operate;

import com.sun.source.util.TaskEvent;
import io.innospots.base.events.IEventListener;
import io.innospots.workflow.core.debug.FlowNodeDebuggerBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * schedule execute flow task using task event
 *
 * @author Smars
 * @date 2023/8/8
 */
/*
@Component
public class FlowTaskScheduleListener implements IEventListener<TaskEvent> {


    private void stop(TaskEvent taskEvent) {
        if (taskEvent.taskAction() != TaskEvent.TaskAction.STOP) {
            return;
        }
        if ("CRONTIMER".equals(taskEvent.appKey())) {
            Map<String, Object> param = (Map<String, Object>) taskEvent.getBody();
            String flowExecutionId = param.get("flowExecutionId").toString();
            FlowNodeDebuggerBuilder.build("nodeDebugger").stop(flowExecutionId);
        }
    }

    private void reRun(TaskEvent taskEvent) {
        if (taskEvent.taskAction() != TaskEvent.TaskAction.RERUN) {
            return;
        }
        if ("CRONTIMER".equals(taskEvent.appKey())) {
            Map<String, Object> param = (Map<String, Object>) taskEvent.getBody();
            Long workflowInstanceId = Long.parseLong(param.get("workflowInstanceId").toString());
            FlowNodeDebuggerBuilder.build("nodeDebugger").execute(workflowInstanceId, null, new ArrayList<>());
        }
    }

    @Override
    public Object listen(TaskEvent event) {
        if(event.taskAction() == TaskEvent.TaskAction.STOP){
            stop(event);
        }

        if(event.taskAction() == TaskEvent.TaskAction.RERUN){
            reRun(event);
        }

        return null;
    }
}
*/