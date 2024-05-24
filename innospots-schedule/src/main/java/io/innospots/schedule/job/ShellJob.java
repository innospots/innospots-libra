package io.innospots.schedule.job;

import io.innospots.base.model.response.InnospotResponse;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.model.JobExecution;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/5/19
 */
public class ShellJob extends BaseJob {


    public static String PARAM_SCRIPT_TYPE = "job.script.type";

    public static String PARAM_SCRIPT_BODY = "job.script.body";

    public ShellJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public void prepare() {
        super.prepare();
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public InnospotResponse<Map<String,Object>> execute() {
        InnospotResponse<Map<String,Object>> response = new InnospotResponse<>();

        return response;
    }
}
