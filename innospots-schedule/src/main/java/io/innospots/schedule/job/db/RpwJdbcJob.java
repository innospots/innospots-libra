package io.innospots.schedule.job.db;

import io.innospots.schedule.model.JobExecution;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/5/19
 */
public class RpwJdbcJob extends DbJdbcJob {


    public RpwJdbcJob(JobExecution jobExecution) {
        super(jobExecution);
    }
}
