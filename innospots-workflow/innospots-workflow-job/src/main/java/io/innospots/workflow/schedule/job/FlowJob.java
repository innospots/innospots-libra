package io.innospots.workflow.schedule.job;

import io.innospots.base.model.response.R;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.model.JobExecution;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.schedule.flow.FlowJobExecutor;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/3
 */
public class FlowJob extends BaseJob {

    private FlowJobExecutor flowJobExecutor;

    private WorkflowBody workflowBody;

    public FlowJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public void prepare() {
        flowJobExecutor = BeanContextAwareUtils.getBean(FlowJobExecutor.class);
        workflowBody = flowJobExecutor.readJobFlow(jobExecution.getJobKey());
        jobExecution.setSubJobCount((long) workflowBody.nodeSize());
    }


    @Override
    public JobType jobType() {
        return JobType.FLOW;
    }


    @Override
    public R<Map<String, Object>> execute() {
        R resp = new R<>();
        flowJobExecutor.executeStartJob(this.jobExecution,workflowBody);
        return resp;
    }
}
