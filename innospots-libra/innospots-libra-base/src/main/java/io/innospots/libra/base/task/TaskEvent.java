package io.innospots.libra.base.task;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/8
 */
public class TaskEvent extends ApplicationEvent {

    private String appKey;

    private String extensionKey;

    private TaskAction taskAction;

    public static TaskEvent build(TaskExecution taskExecution,TaskAction taskAction){
        TaskEvent taskEvent = new TaskEvent(taskExecution.getParamContext(),
                taskExecution.getAppKey(),
                taskExecution.getExtensionKey(), taskAction);
        return taskEvent;
    }

    public TaskEvent(Object source) {
        super(source);
    }

    private TaskEvent(Object source, String appKey, String extensionKey,TaskAction taskAction) {
        super(source);
        this.appKey = appKey;
        this.extensionKey = extensionKey;
        this.taskAction = taskAction;
    }

    public Map<String,Object> params(){
        if(source!=null){
            return (Map<String, Object>) source;
        }
        return null;
    }

    public enum TaskAction{
        STOP,
        RERUN;
    }

    public String appKey() {
        return appKey;
    }

    public String extensionKey() {
        return extensionKey;
    }

    public TaskAction taskAction() {
        return taskAction;
    }
}
