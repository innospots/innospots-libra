package io.innospots.schedule.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/2/18
 */
@Getter
@Setter
public class JobExecutionDisplay extends JobExecution {

    private List<JobExecutionDisplay> subExecutions;
}
