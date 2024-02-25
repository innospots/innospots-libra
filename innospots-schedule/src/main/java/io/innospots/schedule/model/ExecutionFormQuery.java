package io.innospots.schedule.model;

import io.innospots.base.data.request.FormQuery;
import io.innospots.base.quartz.ExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/2/24
 */
@Getter
@Setter
@Schema(title = "job execution query")
public class ExecutionFormQuery extends FormQuery {

    @Schema(title = "start time")
    private String startTime;

    @Schema(title = "end time")
    private String endTime;

    @Schema(title = "job execution status")
    private ExecutionStatus status;

    @Schema(title = "job key")
    private String jobKey;

    @Schema(title = "job name")
    private String jobName;

    @Schema(title = "server key")
    private String serverKey;

    @Schema(title = "scopes")
    private String scopes;

    @Schema(title = "create user name")
    private String createdBy;
}
