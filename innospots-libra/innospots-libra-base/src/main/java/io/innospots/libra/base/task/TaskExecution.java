package io.innospots.libra.base.task;

import io.innospots.libra.base.task.TaskExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/6
 */
@Getter
@Setter
public class TaskExecution {

    private String taskExecutionId;

    private String taskName;

    private TaskExecutionStatus executionStatus;

    private String extensionKey;

    private String extensionType;

    private String appName;

    private String appKey;

    private int percent;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String detailUrl;

    private Map<String,Object> paramContext;

    private String message;

    private String timeConsume;

    private String createdBy;
}
