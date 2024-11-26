package io.innospots.approve.core.converter;

import io.innospots.approve.core.entity.ApproveFlowInstanceEntity;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveFlowInstanceBase;
import io.innospots.approve.core.model.ApproveForm;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.CCH;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
@Mapper
public interface ApproveFlowInstanceConverter extends BaseBeanConverter<ApproveFlowInstance, ApproveFlowInstanceEntity> {

    ApproveFlowInstanceConverter INSTANCE = Mappers.getMapper(ApproveFlowInstanceConverter.class);

    default List<ApproveFlowInstanceBase> entitiesToBaseModel(List<ApproveFlowInstanceEntity> entities){
        List<ApproveFlowInstanceBase> list = new ArrayList<>();
        for (ApproveFlowInstanceEntity entity : entities) {
            ApproveFlowInstanceBase base = new ApproveFlowInstanceBase();
            base.setApproveInstanceKey(entity.getApproveInstanceKey());
            base.setFlowKey(entity.getFlowKey());
            base.setAppKey(entity.getAppKey());
            base.setBelongTo(entity.getBelongTo());
            base.setApproveType(entity.getApproveType());
            base.setProposerId(entity.getProposerId());
            base.setProposer(entity.getProposer());
            base.setMessage(entity.getMessage());
            base.setApprover(entity.getApprover());
            base.setApproverId(entity.getApproverId());
            base.setLastApproveDateTime(entity.getLastApproveDateTime());
            base.setStartTime(entity.getStartTime());
            base.setEndTime(entity.getEndTime());
            if(entity.getApproveStatus()!=null){
                base.setApproveStatus(ApproveStatus.valueOf(entity.getApproveStatus()));
            }
            base.setUpdatedTime(entity.getUpdatedTime());
            list.add(base);
        }
        return list;
    }

    default ApproveFlowInstanceEntity formToEntity(ApproveForm approveForm){
        ApproveFlowInstanceEntity entity = new ApproveFlowInstanceEntity();
        entity.setApproveInstanceKey(approveForm.getApproveInstanceKey());
        entity.setAppKey(approveForm.getAppKey());
        entity.setFlowKey(approveForm.getFlowKey());
        entity.setBelongTo(approveForm.getBelongTo());
        entity.setApproveType(approveForm.getApproveType());
        entity.setFormData(JSONUtils.toJsonString(approveForm.getFormData()));
        entity.setApproveStatus(ApproveStatus.DRAFT.name());
        entity.setProposerId(CCH.userId());
        entity.setProposer(CCH.authUser());

        return entity;
    }
}
