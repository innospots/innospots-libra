package io.innospots.workflow.console.model.task;

import io.innospots.base.quartz.ScheduleMode;
import io.innospots.base.quartz.ExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2023/7/16
 */
@Getter
@Setter
@Schema(title = "Schedule Task")
public class ScheduleTask {

    @Schema(title = "schedule workflow instance primary id")
    private Long workflowInstanceId;

    @Schema(title = "workflow instance name")
    private String taskName;

    @Schema(title = "workflow instance description")
    private String description;

    @Schema(title = "workflow instance trigger code , app node definition code")
    private Integer appCode;

    @Schema(title = "app node definition name")
    private String appName;

    @Schema(title = "workflow trigger node config")
    private String scheduleTime;

    @Schema(title = "workflow trigger node schedule mode")
    private ScheduleMode scheduleMode;

    @Schema(title = "schedule start time and end time")
    private String scheduleRangeTime;

    @Schema(title = "workflow node number")
    private int taskNumber;

    @Schema(title = "last execution status")
    private ExecutionStatus executionStatus;

    @Schema(title = "last execution start time")
    private LocalDateTime lastRunTime;


}
