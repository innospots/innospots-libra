package io.innospots.workflow.core.flow.instance;

import io.innospots.workflow.core.flow.WorkflowBaseBody;
import io.innospots.workflow.core.flow.WorkflowBody;

/**
 * @author Smars
 * @date 2023/8/7
 */
public interface IWorkflowCacheDraftOperator {

    boolean saveFlowInstanceToCache(WorkflowBaseBody workflowBaseBody);

    WorkflowBaseBody getFlowInstanceDraftOrCache(Long flowInstanceId);

    WorkflowBaseBody saveCacheToDraft(Long flowInstanceId);

    WorkflowBody getWorkflowBody(Long workflowInstanceId, Integer revision, Boolean includeNodes);

    WorkflowBaseBody getWorkflowBodyByKey(String flowKey, Integer revision, Boolean includeNodes);

    WorkflowBaseBody saveDraft(WorkflowBaseBody workflowBaseBody);
}
