package io.innospots.approve.node;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.enums.ActorType;
import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.enums.ApproveResult;
import io.innospots.approve.core.model.ApproveActor;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.model.user.UserGroup;
import io.innospots.base.model.user.UserInfo;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.service.IUserGroupService;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
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
public class ApprovePersonNode extends ApproveBaseNode {

    private String resultField;
    private String messageField;
    private ActorType actorType;
    private Integer userId;
    private Integer roleId;
    private Integer leaderLevel;

    @Override
    protected void initialize() {
        super.initialize();
        resultField = this.valueString("resultField");
        messageField = this.valueString("messageField");
        if(resultField==null){
            resultField = ApproveConstant.APPROVE_RESULT;
        }
        if(messageField == null){
            messageField = ApproveConstant.APPROVE_MESSAGE;
        }
        actorType = ActorType.valueOf(this.valueString("actorType"));
        userId = this.valueInteger("userId");
        roleId = this.valueInteger("roleId");
        leaderLevel = this.valueInteger("leaderLevel");
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
    }

    @Override
    protected Object processItem(Map<String, Object> item, NodeExecution nodeExecution) {
        ApproveFlowInstance approveFlowInstance = getApproveFlowInstance(item);
        ApproveActor approveActor = approveActorOperator.getApproveActorByFlowExecutionId(approveFlowInstance.getFlowExecutionId(), this.nodeKey());
        String message = (String) item.get(messageField);
        String result = (String) item.get(resultField);

        Map<String, Object> body = new HashMap<>();
        if (approveActor == null) {
            //new actor, first execute the node
            approveActor = ApproveActor.builder()
                    .approveAction(ApproveAction.PENDING)
                    .actorType(actorType)
                    .approveInstanceKey(approveFlowInstance.getApproveInstanceKey())
                    .nodeKey(this.nodeKey())
                    .approveExecutionId(nodeExecution.getNodeExecutionId())
                    .flowExecutionId(nodeExecution.getFlowExecutionId())
                    .build();
            fillActorType(approveActor, approveFlowInstance);
            approveActor = approveActorOperator.saveApproveActor(approveActor);
            //not execute next node
            nodeExecution.setNext(false);
            ApproveHolder.setActor(approveActor);
            nodeExecution.setStatus(ExecutionStatus.PENDING);
            body.putAll(item);
            return body;
        }

        if (approveActor.getApproveAction() != ApproveAction.PENDING) {
            body.putAll(item);
            nodeExecution.setMessage("actor can't approve, action is " + approveActor.getApproveAction());
            return body;
        }

        if (!validPermission(nodeExecution)) {
            body.putAll(item);
            nodeExecution.setMessage("not allow permission");
            return body;
        }

        if (result == null) {
            body.putAll(item);
            nodeExecution.setMessage("result field is null");
            log.warn("not have {} field in the payload:{}", resultField, item);
            return body;
        }

        if (ApproveResult.APPROVED.name().equals(result)) {
            nodeExecution.setNext(true);
        } else {
            nodeExecution.setNext(false);
        }

        nodeExecution.setMessage(message);
        approveActor.setResult(result);
        approveActor.setMessage(message);
        approveActor.setUserId(CCH.userId());
        approveActor.setUserName(CCH.authUser());
        approveActor.setActorType(actorType);
        approveActor.setApproveAction(ApproveAction.DONE);
        approveActor.setApproveExecutionId(nodeExecution.getNodeExecutionId());
        approveActor.setFlowExecutionId(nodeExecution.getFlowExecutionId());
        approveActorOperator.saveApproveActor(approveActor);
        ApproveHolder.setActor(approveActor);
        approveFlowInstanceOperator.updateProcessStatus(approveFlowInstance.getApproveInstanceKey());
        return body;
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

    private void fillActorType(ApproveActor approveActor, ApproveFlowInstance flowInstance) {
        if (actorType == ActorType.GROUP) {
            UserGroup userGroup = getUserGroup(flowInstance);
            approveActor.setActorId(userGroup.getGroupId());
        } else if (actorType == ActorType.ROLE) {
            approveActor.setActorId(roleId);
        } else if (actorType == ActorType.USER) {
            approveActor.setActorId(userId);
        }
    }

    private UserGroup getUserGroup(ApproveFlowInstance flowInstance) {
        IUserGroupService userGroupService = getBean(IUserGroupService.class);
        UserGroup userGroup = userGroupService.findParentUserGroupByUserId(flowInstance.getProposerId(), leaderLevel);
        return userGroup;
    }

}
