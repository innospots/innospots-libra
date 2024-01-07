package io.innospots.workflow.console.task.execution;

import io.innospots.base.events.IEventListener;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.events.FlowExecutionTaskEvent;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.instance.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Smars
 * @date 2023/8/7
 */
/*
@Component
public class FlowTaskExecutionListener implements IEventListener<FlowExecutionTaskEvent> {

    private static final Logger logger = LoggerFactory.getLogger(FlowTaskExecutionListener.class);

    private final ITaskExecutionExplore taskExecutionExplore;

    private final WorkflowInstanceOperator workflowInstanceOperator;

    public static Set<String> taskSources = new HashSet<>();

    public FlowTaskExecutionListener(ITaskExecutionExplore taskExecutionExplore, WorkflowInstanceOperator workflowInstanceOperator) {
        this.taskExecutionExplore = taskExecutionExplore;
        this.workflowInstanceOperator = workflowInstanceOperator;
        taskSources.add("CRONTIMER");
    }

    @Override
    public Object listen(FlowExecutionTaskEvent event) {
        FlowExecution flowExecution = event.flowExecution();
        Long flowInstanceId = flowExecution.getFlowInstanceId();
        WorkflowInstance workflowInstance = workflowInstanceOperator.getWorkflowInstance(flowInstanceId);
        if (flowExecution.getSource() == null) {
            flowExecution.setSource(workflowInstance.getTriggerCode());
        }
        if (flowExecution.getSource() == null || !taskSources.contains(flowExecution.getSource())) {
            // not task flow execution
            return null;
        }
        TaskExecution taskExecution = buildTaskExecution(flowExecution, workflowInstance);

        // save taskExecution
        if (taskExecution.getExecutionStatus() == TaskExecutionStatus.RUNNING) {
            taskExecutionExplore.saveTaskExecution(taskExecution);

        } else {
            taskExecutionExplore.updateTaskExecution(taskExecution);
        }
        return taskExecution;
    }

    public TaskExecution buildTaskExecution(FlowExecution execution, WorkflowInstance workflowInstance) {
        logger.info("FlowTaskExecutionListener-flow execution: {}", execution);
        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setTaskExecutionId(execution.getFlowExecutionId());

        taskExecution.setTaskName(workflowInstance.getName());
        if (execution.getStatus() == ExecutionStatus.RUNNING) {
            taskExecution.setExecutionStatus(TaskExecutionStatus.RUNNING);

        } else if (execution.getStatus() == ExecutionStatus.FAILED) {
            taskExecution.setExecutionStatus(TaskExecutionStatus.FAILED);

        } else if (execution.getStatus() == ExecutionStatus.NOT_PREPARED || execution.getStatus() == ExecutionStatus.STOPPED) {
            taskExecution.setExecutionStatus(TaskExecutionStatus.STOPPED);
        }
        taskExecution.setStartTime(execution.getStartTime());
        taskExecution.setAppKey(execution.getSource());
        taskExecution.setAppName("工作流引擎");
        taskExecution.setDetailUrl("/task-execution/" + taskExecution.getAppKey() + "/" + execution.getFlowExecutionId()
                + "/" + workflowInstance.getWorkflowInstanceId() + "/" + workflowInstance.getRevision());
        Map<String, Object> paramContextMap = new HashMap<>();
        paramContextMap.put("workflowInstanceId", workflowInstance.getWorkflowInstanceId());
        paramContextMap.put("flowExecutionId", execution.getFlowExecutionId());
        taskExecution.setParamContext(paramContextMap);
        return taskExecution;
    }

    @Override
    public Class<FlowExecutionTaskEvent> eventBodyClass() {
        return FlowExecutionTaskEvent.class;
    }
}
*/
