package io.innospots.approve.core.converter;

import io.innospots.approve.core.entity.ApproveExecutionEntity;
import io.innospots.approve.core.model.ApproveActor;
import io.innospots.approve.core.model.ApproveExecution;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Mapper
public interface ApproveExecutionConverter extends BaseBeanConverter<ApproveExecution, ApproveExecutionEntity> {

    ApproveExecutionConverter INSTANCE = Mappers.getMapper(ApproveExecutionConverter.class);

    static ApproveExecutionEntity toEntity(NodeExecution nodeExecution, ApproveActor actor) {
        ApproveExecutionEntity entity = new ApproveExecutionEntity();
        entity.setApproveExecutionId(nodeExecution.getNodeExecutionId());
        entity.setFlowExecutionId(nodeExecution.getFlowExecutionId());
        entity.setNodeKey(nodeExecution.getNodeKey());
        entity.setNodeName(nodeExecution.getNodeName());
        entity.setSequenceNumber(nodeExecution.getSequenceNumber());
        entity.setMessage(nodeExecution.getMessage());
        entity.setStartTime(nodeExecution.getStartTime());
        entity.setEndTime(nodeExecution.getEndTime());
        entity.setExecutionStatus(nodeExecution.getStatus().name());
        if (actor != null) {
            entity.setApproveResult(actor.getResult()!=null ? actor.getResult().name():null);
            entity.setUserId(actor.getUserId());
            entity.setApproveActorId(actor.getApproveActorId());
            entity.setUserName(actor.getUserName());
            entity.setApproveInstanceKey(actor.getApproveInstanceKey());
            entity.setMessage(actor.getMessage());
        } else {
            if (ApproveHolder.get() != null) {
                entity.setApproveInstanceKey(ApproveHolder.get().getApproveInstanceKey());
            }
        }

        return entity;
    }

    static NodeExecution newNodeExecution(ApproveExecutionEntity approveExecutionEntity, FlowExecution flowExecution) {
        NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution(approveExecutionEntity.getNodeKey(),flowExecution);
        nodeExecution.setNodeExecutionId(approveExecutionEntity.getApproveExecutionId());
        nodeExecution.setNodeKey(approveExecutionEntity.getNodeKey());
        nodeExecution.setNodeName(approveExecutionEntity.getNodeName());
        nodeExecution.setSequenceNumber(approveExecutionEntity.getSequenceNumber());
        //nodeExecution.setMessage(approveExecutionEntity.getMessage());
        nodeExecution.setStartTime(approveExecutionEntity.getStartTime());
        //nodeExecution.setEndTime(approveExecutionEntity.getEndTime());
        //nodeExecution.setStatus(ExecutionStatus.valueOf(approveExecutionEntity.getExecutionStatus()));
        nodeExecution.setFlowExecutionId(approveExecutionEntity.getFlowExecutionId());
        return nodeExecution;
    }

}
