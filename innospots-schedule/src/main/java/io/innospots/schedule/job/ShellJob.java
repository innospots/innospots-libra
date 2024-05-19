package io.innospots.schedule.job;

import io.innospots.schedule.enums.JobType;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/5/19
 */
public class ShellJob extends BaseJob {



    @Override
    public void prepare() {
        super.prepare();
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public void execute() {

    }
}
