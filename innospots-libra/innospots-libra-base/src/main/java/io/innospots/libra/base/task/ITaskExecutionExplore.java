package io.innospots.libra.base.task;

/**
 * @author Smars
 * @date 2023/8/7
 */
public interface ITaskExecutionExplore {


    boolean saveTaskExecution(TaskExecution taskExecution);

    boolean updateTaskExecution(TaskExecution taskExecution);

    boolean updateTaskExecution(String taskExecutionId,int percent);

    boolean stop(String taskExecutionId);

    boolean reRun(String taskExecutionId);
}
