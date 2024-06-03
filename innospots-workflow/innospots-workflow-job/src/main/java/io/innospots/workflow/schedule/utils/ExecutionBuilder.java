package io.innospots.workflow.schedule.utils;

import io.innospots.base.execution.ExecMode;
import io.innospots.schedule.model.JobExecution;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.flow.Flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/3
 */
public class ExecutionBuilder {


    public static FlowExecution build(JobExecution jobExecution, Flow flow){
        List<Map<String, Object>> payloads = new ArrayList<>();
        payloads.add(jobExecution.getContext());
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(
                flow.getWorkflowInstanceId(),
                flow.getRevision(),
                true,
                true,
                payloads);
        flowExecution.setFlowKey(flow.getFlowKey());
        flowExecution.setFlowExecutionId(jobExecution.getExecutionId());
        flowExecution.setExecMode(ExecMode.TASK);
        flowExecution.setStartTime(jobExecution.getStartTime());
        flowExecution.setStatus(jobExecution.getExecutionStatus());
        return flowExecution;
    }
}
