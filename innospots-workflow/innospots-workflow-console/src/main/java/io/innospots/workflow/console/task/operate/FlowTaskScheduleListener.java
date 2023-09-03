package io.innospots.workflow.console.task.operate;

import io.innospots.libra.base.task.TaskEvent;
import io.innospots.workflow.core.debug.FlowNodeDebuggerBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * schedule execute flow task using task event
 *
 * @author Smars
 * @date 2023/8/8
 */
@Component
public class FlowTaskScheduleListener {

    @EventListener(TaskEvent.class)
    public void stop(TaskEvent taskEvent) {
        if (taskEvent.taskAction() != TaskEvent.TaskAction.STOP) {
            return;
        }
        if ("CRONTIMER".equals(taskEvent.appKey())) {
            Map<String, Object> param = (Map<String, Object>) taskEvent.getSource();
            String flowExecutionId = param.get("flowExecutionId").toString();
            FlowNodeDebuggerBuilder.build("nodeDebugger").stop(flowExecutionId);
        }
    }

    @EventListener(TaskEvent.class)
    public void reRun(TaskEvent taskEvent) {
        if (taskEvent.taskAction() != TaskEvent.TaskAction.RERUN) {
            return;
        }
        if ("CRONTIMER".equals(taskEvent.appKey())) {
            Map<String, Object> param = (Map<String, Object>) taskEvent.getSource();
            Long workflowInstanceId = Long.parseLong(param.get("workflowInstanceId").toString());
            FlowNodeDebuggerBuilder.build("nodeDebugger").execute(workflowInstanceId, null, new ArrayList<>());
        }
    }
}
