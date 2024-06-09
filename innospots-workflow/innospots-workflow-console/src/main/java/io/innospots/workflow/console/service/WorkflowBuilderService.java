package io.innospots.workflow.console.service;

import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.instance.operator.WorkflowBodyOperator;
import io.innospots.workflow.core.instance.operator.WorkflowDraftOperator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/8
 */
@Service
public class WorkflowBuilderService {


    private WorkflowBodyOperator workFlowBodyOperator;

    private WorkflowDraftOperator workflowDraftOperator;

    private InnospotsWorkflowProperties workflowProperties;

    private ScheduleFlowJobService scheduleFlowJobService;

    public WorkflowBuilderService(WorkflowBodyOperator workFlowBodyOperator,
                                  WorkflowDraftOperator workflowDraftOperator,
                                  InnospotsWorkflowProperties workflowProperties,
                                  ScheduleFlowJobService scheduleFlowJobService) {
        this.workFlowBodyOperator = workFlowBodyOperator;
        this.workflowDraftOperator = workflowDraftOperator;
        this.workflowProperties = workflowProperties;
        this.scheduleFlowJobService = scheduleFlowJobService;
    }

    public WorkflowBaseBody getFlowInstanceByRevision(Long workflowInstanceId,
                                                      Integer revision,
                                                      Boolean includeNodes) {
        WorkflowBaseBody workflowBaseBody;
        if (revision == null || revision == 0) {
            workflowBaseBody = workflowDraftOperator.getDraftWorkflow(workflowInstanceId);
        } else {
            workflowBaseBody = workFlowBodyOperator.getWorkflowBody(workflowInstanceId, revision, includeNodes);
        }
        return workflowBaseBody;
    }

    public WorkflowBaseBody getFlowInstanceByDraft(Long workflowInstanceId) {

        return workflowDraftOperator.getDraftWorkflow(workflowInstanceId);
    }

    public WorkflowBaseBody getFlowInstanceByLasted(Long workflowInstanceId,
                                                    Boolean includeNodes) {
        return workFlowBodyOperator.getWorkflowBody(workflowInstanceId, null, includeNodes);
    }

    public boolean saveCache(WorkflowBaseBody workflowBaseBody) {

        return workflowDraftOperator.saveFlowInstanceToCache(workflowBaseBody);
    }

    public WorkflowBaseBody saveDraft(WorkflowBaseBody workflowBaseBody) {

        return workflowDraftOperator.saveDraft(workflowBaseBody);
    }

    public boolean publish(Long workflowInstanceId,
                                        String description) {
        WorkflowBody workflowBody = workflowDraftOperator.publish(workflowInstanceId, description, workflowProperties.getWorkFlowInstanceKeepVersionAmount());
        //update or create schedule flow
        scheduleFlowJobService.saveSchedule(workflowBody);
        return workflowBody != null;
    }

    public List<Map<String, Object>> listNodeInputFields(
            Long workflowInstanceId,
            String nodeKey,
            Set<String> sourceNodeKeys) {
        return workflowDraftOperator.selectNodeInputFields(workflowInstanceId, nodeKey, sourceNodeKeys);
    }


}
