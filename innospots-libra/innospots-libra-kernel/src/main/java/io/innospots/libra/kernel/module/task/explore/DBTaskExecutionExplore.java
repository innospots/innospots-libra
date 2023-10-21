package io.innospots.libra.kernel.module.task.explore;

import io.innospots.base.utils.ApplicationContextUtils;
import io.innospots.libra.base.task.ITaskExecutionExplore;
import io.innospots.libra.base.task.TaskEvent;
import io.innospots.libra.base.task.TaskExecution;
import io.innospots.libra.kernel.module.task.operator.TaskExecutionOperator;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Component
public class DBTaskExecutionExplore implements ITaskExecutionExplore {

    private final TaskExecutionOperator taskExecutionOperator;

    public DBTaskExecutionExplore(TaskExecutionOperator taskExecutionOperator) {
        this.taskExecutionOperator = taskExecutionOperator;
    }

    @Override
    public boolean saveTaskExecution(TaskExecution taskExecution) {
        return taskExecutionOperator.createTaskExecution(taskExecution);
    }

    @Override
    public boolean updateTaskExecution(TaskExecution taskExecution) {
        return taskExecutionOperator.updateTaskExecution(taskExecution);
    }

    @Override
    public boolean updateTaskExecution(String taskExecutionId, int percent) {
        return false;
    }

    @Override
    public boolean stop(String taskExecutionId) {
        TaskExecution taskExecution = taskExecutionOperator.getTaskExecution(taskExecutionId);

        TaskEvent taskEvent = TaskEvent.build(taskExecution, TaskEvent.TaskAction.STOP);

        //forward application event
        ApplicationContextUtils.sendAppEvent(taskEvent);
        return true;
    }

    @Override
    public boolean reRun(String taskExecutionId) {
        TaskExecution taskExecution = taskExecutionOperator.getTaskExecution(taskExecutionId);

        TaskEvent taskEvent = TaskEvent.build(taskExecution, TaskEvent.TaskAction.RERUN);

        // forward application event
        ApplicationContextUtils.sendAppEvent(taskEvent);
        return true;
    }
}
