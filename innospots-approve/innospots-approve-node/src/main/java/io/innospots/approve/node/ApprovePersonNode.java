package io.innospots.approve.node;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.enums.ApproveResult;
import io.innospots.approve.core.model.ApproveActor;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveActorOperator;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.model.user.UserInfo;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
@Slf4j
public class ApprovePersonNode extends BaseNodeExecutor {

    private ApproveActorOperator approveActorOperator;
    private ApproveFlowInstanceOperator approveFlowInstanceOperator;
    private String resultField;
    private String messageField;
    private String actorType;
    private Integer userId;
    private Integer roleId;

    @Override
    protected void initialize() {
        resultField = this.valueString("resultField");
        messageField = this.valueString("messageField");
        actorType = this.valueString("actorType");
        userId = this.validInteger("userId");
        roleId = this.validInteger("roleId");
        approveActorOperator = this.getBean(ApproveActorOperator.class);
        approveFlowInstanceOperator = this.getBean(ApproveFlowInstanceOperator.class);
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
    }

    @Override
    protected Object processItem(Map<String, Object> item, NodeExecution nodeExecution) {
        ApproveFlowInstance approveFlowInstance = getApproveFlowInstance(item);
        ApproveActor approveActor = approveActorOperator.getApproveActor(approveFlowInstance.getApproveInstanceKey(), this.nodeKey());
        String message = (String) item.get(messageField);
        String result = (String) item.get(resultField);

        Map<String, Object> body = new HashMap<>();
        if (approveActor == null) {
            //new actor, first execute the node
            approveActor = new ApproveActor();
            approveActor.setApproveAction(ApproveAction.PENDING.name());
            approveActor.setApproveInstanceKey(approveFlowInstance.getApproveInstanceKey());
            approveActor.setNodeKey(this.nodeKey());
            approveActorOperator.saveApproveActor(approveActor);
            //not execute next node
            nodeExecution.setNext(false);
            ApproveHolder.setActor(approveActor);
            return body;
        }

        if (!validPermission(nodeExecution)) {
            return body;
        }

        if (result == null) {
            log.warn("not have {} field in the payload:{}", resultField, item);
            return body;
        }

        if (ApproveResult.APPROVED.name().equals(result)) {
            nodeExecution.setNext(true);
        } else {
            nodeExecution.setNext(false);
            nodeExecution.setStatus(ExecutionStatus.FAILED);
        }

        nodeExecution.setMessage(message);
        approveActor.setResult(result);
        approveActor.setMessage(message);
        approveActor.setUserId(CCH.userId());
        approveActor.setUserName(CCH.authUser());
        approveActor.setActorType(actorType);
        approveActor.setApproveAction(ApproveAction.DONE.name());
        approveActorOperator.saveApproveActor(approveActor);
        ApproveHolder.setActor(approveActor);

        return body;
    }

    private ApproveFlowInstance getApproveFlowInstance(Map<String, Object> item) {
        ApproveFlowInstance approveFlowInstance = ApproveHolder.get();
        if (approveFlowInstance == null) {
            String approveInstanceId = (String) item.get(ApproveConstant.APPROVE_INSTANCE_KEY);
            approveFlowInstance = approveFlowInstanceOperator.findOne(approveInstanceId);
        }
        return approveFlowInstance;
    }

    private boolean validPermission(NodeExecution nodeExecution) {
        UserInfo userInfo = ApproveHolder.getLoginUser();
        String message = null;
        boolean hasAuth = false;
        if (userId != null) {
            if (Objects.equals(userId, userInfo.getUserId())) {
                hasAuth = true;
            } else {
                message = "{user.approve.user_not_permission:用户没有审批权限}";
                nodeExecution.setMessage(message);
            }
        }
        if (roleId != null) {
            hasAuth = userInfo.getRoleIds().contains(roleId);
            if (!hasAuth) {
                message = "{user.approve.role_not_permission:用户角色没有审批权限}";
                nodeExecution.setMessage(message);
            }
        }
        if (!hasAuth) {
            message = "{user.approve.not_permission:用户没有审批权限}";
            nodeExecution.setMessage(message);
        }
        return hasAuth;
    }
}
