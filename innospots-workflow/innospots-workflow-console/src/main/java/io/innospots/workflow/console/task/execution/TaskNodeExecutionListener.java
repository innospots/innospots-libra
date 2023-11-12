package io.innospots.workflow.console.task.execution;

import io.innospots.base.events.IEventListener;
import io.innospots.libra.base.task.ITaskExecutionExplore;
import io.innospots.libra.base.task.TaskExecution;
import io.innospots.libra.base.task.TaskExecutionStatus;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.events.NodeExecutionTaskEvent;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Component
public class TaskNodeExecutionListener implements IEventListener<NodeExecutionTaskEvent> {

    private static final Logger logger = LoggerFactory.getLogger(TaskNodeExecutionListener.class);

    private final ITaskExecutionExplore taskExecutionExplore;

    public static Set<String> taskSources = new HashSet<>();

    public TaskNodeExecutionListener(ITaskExecutionExplore taskExecutionExplore) {
        this.taskExecutionExplore = taskExecutionExplore;
        taskSources.add("CRONTIMER");
    }

    @Override
    public Object listen(NodeExecutionTaskEvent event) {
        if (event.flowExecution().getSource() == null ||
                !taskSources.contains(event.flowExecution().getSource())) {
            // not task flow execution
            return null;
        }
        // update taskExecution process percent
        TaskExecution taskExecution = buildTaskExecution(event);

//        taskExecutionExplore.updateTaskExecution(taskExecution.getTaskExecutionId(), taskExecution.getPercent());
        taskExecutionExplore.updateTaskExecution(taskExecution);

        return taskExecution;
    }

    public TaskExecution buildTaskExecution(NodeExecutionTaskEvent event) {
        FlowExecution execution = event.flowExecution();
        logger.info("TaskNodeExecutionListener-flow execution: {}", execution);
        TaskExecution taskExecution = new TaskExecution();
        taskExecution.setTaskExecutionId(execution.getFlowExecutionId());
        taskExecution.setEndTime(LocalDateTime.now());
        taskExecution.setMessage(execution.getMessage());
        if (execution.getStatus() == ExecutionStatus.PENDING) {
            taskExecution.setExecutionStatus(TaskExecutionStatus.STOPPED);

        } else if (execution.getStatus() == ExecutionStatus.FAILED) {
            taskExecution.setExecutionStatus(TaskExecutionStatus.FAILED);
        }
        List<NodeExecution> nodeExecutions = new ArrayList<>(execution.getNodeExecutions().values());
        List<NodeExecution> completeNodeExecutions = nodeExecutions.stream()
                .filter(nodeExecution -> nodeExecution.getStatus() == ExecutionStatus.COMPLETE).collect(Collectors.toList());
        taskExecution.setPercent(new BigDecimal(completeNodeExecutions.size() + "")
                .divide(new BigDecimal(execution.getTotalCount()), 2, RoundingMode.HALF_DOWN)
                .multiply(BigDecimal.TEN).multiply(BigDecimal.TEN)
                .toBigInteger().intValue());
        if (completeNodeExecutions.size() == execution.getTotalCount()) {
            taskExecution.setExecutionStatus(TaskExecutionStatus.COMPLETE);
        }
        return taskExecution;
    }

    @Override
    public Class<NodeExecutionTaskEvent> eventBodyClass() {
        return NodeExecutionTaskEvent.class;
    }
}
