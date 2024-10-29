package io.innospots.schedule.job.db;

import io.innospots.connector.core.jdbc.JdbcDataConnectionMinder;
import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.base.model.response.R;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.model.JobExecution;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/5/19
 */
public abstract class DbJdbcJob extends BaseJob {

    public static final String PARAM_CREDENTIAL_KEY = "job.credential.key";

    private String credentialKey;

    public DbJdbcJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public void prepare() {
        super.prepare();
        credentialKey = validParamString(PARAM_CREDENTIAL_KEY);
        JdbcDataConnectionMinder minder = (JdbcDataConnectionMinder) DataConnectionMinderManager.getCredentialMinder(credentialKey);
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public R<Map<String, Object>> execute() {
        return null;
    }


}
