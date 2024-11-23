package io.innospots.approve.core.converter;

import io.innospots.approve.core.entity.ApproveFlowInstanceEntity;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveForm;
import io.innospots.base.converter.BaseBeanConverter;
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
        entity.setOriginatorId(CCH.userId());
        entity.setOriginator(CCH.authUser());

        return entity;
    }
}
