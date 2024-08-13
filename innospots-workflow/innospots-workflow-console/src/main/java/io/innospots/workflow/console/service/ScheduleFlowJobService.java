package io.innospots.workflow.console.service;

import io.innospots.base.constant.ModuleConstants;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.quartz.JobType;
import io.innospots.base.quartz.ScheduleJobInfo;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.base.quartz.TimeConfig;
import io.innospots.libra.base.events.ResourceActionEvent;
import io.innospots.workflow.console.operator.node.FlowTemplateOperator;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.definition.model.FlowTemplate;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import io.innospots.workflow.core.node.executor.TriggerNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/7/16
 */
@Slf4j
@Service
public class ScheduleFlowJobService {

    public static final String FLOW_JOB_CLASS = "io.innospots.workflow.schedule.job.FlowJob";

    private FlowTemplateOperator flowTemplateOperator;

    public ScheduleFlowJobService(FlowTemplateOperator flowTemplateOperator) {
        this.flowTemplateOperator = flowTemplateOperator;
    }

    public boolean updateScheduleStatus(WorkflowInstanceEntity workflowInstance) {
        ResourceActionEvent actionEvent = new ResourceActionEvent(ModuleConstants.JOB_MODULE, ResourceActionEvent.ResourceAction.STATUS, null);
        actionEvent.setStatus(DataStatus.valueOf(workflowInstance.getStatus()));
        actionEvent.setPrimaryId(workflowInstance.getFlowKey());
        EventBusCenter.postSync(actionEvent);
        return true;
    }

    public boolean deleteSchedule(WorkflowInstanceEntity workflowInstance) {
        ResourceActionEvent actionEvent = new ResourceActionEvent(ModuleConstants.JOB_MODULE, ResourceActionEvent.ResourceAction.DELETE, null);
        actionEvent.setPrimaryId(workflowInstance.getFlowKey());
        EventBusCenter.postSync(actionEvent);
        return true;
    }

    /**
     * when publish workflow
     *
     * @param workflowBody
     * @return
     */
    public boolean saveSchedule(WorkflowBody workflowBody) {
        ScheduleJobInfo jobInfo = buildScheduleJobInfo(workflowBody);
        if (jobInfo != null) {
            ResourceActionEvent actionEvent = new ResourceActionEvent(ModuleConstants.JOB_MODULE, ResourceActionEvent.ResourceAction.SAVE, jobInfo);
            actionEvent.setPrimaryId(workflowBody.getFlowKey());
            EventBusCenter.postSync(actionEvent);
            return true;
        }
        return false;
    }

    private ScheduleJobInfo buildScheduleJobInfo(WorkflowBody workflowBody) {
        FlowTemplate template = flowTemplateOperator.getTemplate(workflowBody.getTemplateCode(), false, true);
        if (!"SCHEDULE".equals(template.getType())) {
            //only schedule type flow can be scheduled
            return null;
        }
        TimeConfig timeConfig = null;
        for (NodeInstance startNode : workflowBody.getStarts()) {
            BaseNodeExecutor nodeExecutor = NodeExecutorFactory.build(workflowBody.identifier(), startNode);
            if (nodeExecutor instanceof TriggerNode) {
                Map<String, Object> triggerInfo = ((TriggerNode) nodeExecutor).triggerInfo();
                timeConfig = (TimeConfig) triggerInfo.get(TimeConfig.class.getSimpleName());
                if (timeConfig == null) {
                    log.warn("schedule time not config in flow node, key: {}, {}", nodeExecutor.nodeKey(), triggerInfo);
                    return null;
                }
                break;
            }
        }

        ScheduleJobInfo jobInfo = new ScheduleJobInfo();
        jobInfo.setTimeConfig(timeConfig);
        jobInfo.setJobKey(workflowBody.getFlowKey());
        jobInfo.setJobType(JobType.FLOW);
        jobInfo.setJobStatus(DataStatus.ONLINE);
        jobInfo.setJobName(workflowBody.getName());
        jobInfo.setScheduleMode(ScheduleMode.SCHEDULED);
        jobInfo.setSubJobCount(workflowBody.nodeSize());
        jobInfo.setScopes("flow");
        jobInfo.setResourceKey(String.valueOf(workflowBody.getWorkflowInstanceId()));
        jobInfo.setJobClass(FLOW_JOB_CLASS);
        return jobInfo;
    }


}
