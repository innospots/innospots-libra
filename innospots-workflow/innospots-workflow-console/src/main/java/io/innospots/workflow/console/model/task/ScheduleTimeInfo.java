package io.innospots.workflow.console.model.task;

import io.innospots.base.quartz.ScheduleMode;
import io.innospots.base.quartz.TimePeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Smars
 * @date 2023/7/17
 */
@Getter
@Setter
@Schema(title = "schedule time info")
public class ScheduleTimeInfo {

    private Long workflowInstanceId;

    @Schema(title = "execute schedule mode")
    private ScheduleMode scheduleMode;

    @Schema(title = "schedule start time")
    private LocalDateTime startTime;

    @Schema(title = "schedule end time")
    private LocalDateTime endTime;

    @Schema(title = "day week month")
    private TimePeriod timePeriod;

    @Schema(title = "examples: 1,2,3,4,5")
    private List<String> periodTimes;

    @Schema(title = "hh:mm")
    private String runTime;
}
