package io.innospots.approve.core.converter;

import io.innospots.approve.core.entity.ApproveFlowInstanceEntity;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveForm;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.CCH;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
@Mapper
public interface ApproveFlowInstanceConverter extends BaseBeanConverter<ApproveFlowInstance, ApproveFlowInstanceEntity> {

    ApproveFlowInstanceConverter INSTANCE = Mappers.getMapper(ApproveFlowInstanceConverter.class);


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
