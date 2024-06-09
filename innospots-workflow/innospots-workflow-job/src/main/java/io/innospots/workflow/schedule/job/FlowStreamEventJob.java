package io.innospots.workflow.schedule.job;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.model.JobExecution;

import java.util.Map;

/**
 * execute flow using flow stream event engine
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/9
 */
public class FlowStreamEventJob extends BaseJob {

    public FlowStreamEventJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public InnospotsResponse<Map<String, Object>> execute() {
        return null;
    }
}
