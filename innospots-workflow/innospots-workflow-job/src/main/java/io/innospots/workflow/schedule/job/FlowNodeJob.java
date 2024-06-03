package io.innospots.workflow.schedule.job;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.model.JobExecution;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.manage.FlowManager;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.schedule.utils.ExecutionBuilder;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/2
 */
public class FlowNodeJob extends BaseJob {

    private FlowManager flowManager;
    private Flow flow;
    private BaseNodeExecutor nodeExecutor;

    public FlowNodeJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public void prepare() {
        flowManager = BeanContextAwareUtils.getBean(FlowManager.class);
        String flowKey = this.jobExecution.getResourceKey();
        flow = flowManager.loadFlow(flowKey);
        nodeExecutor = flow.findNode(this.jobExecution.getJobKey());
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public InnospotsResponse<Map<String, Object>> execute() {
        InnospotsResponse<Map<String, Object>> resp = new InnospotsResponse<>();
        FlowExecution flowExecution = ExecutionBuilder.build(this.jobExecution, this.flow);
        NodeExecution nodeExecution = nodeExecutor.execute(flowExecution);
        resp.setMessage(nodeExecution.getMessage());
        if (nodeExecution.getStatus() == ExecutionStatus.COMPLETE) {
            resp.fillResponse(ResponseCode.SUCCESS);
        } else if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
            resp.fillResponse(ResponseCode.FAIL);
        }
        Map<String,Object> body = new HashMap<>();
        if(CollectionUtils.isNotEmpty(nodeExecution.getOutputs())){
            for (NodeOutput output : nodeExecution.getOutputs()) {
                for (Map<String, Object> result : output.getResults()) {
                    body.putAll(result);
                }
            }//end for
        }//end if
        resp.setBody(body);
        return resp;
    }
}
