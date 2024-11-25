package io.innospots.approve.core.converter;

import io.innospots.approve.core.entity.ApproveActorEntity;
import io.innospots.approve.core.model.ApproveActor;
import io.innospots.base.converter.BaseBeanConverter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/25
 */
@Mapper
public interface ApproveActorConverter extends BaseBeanConverter<ApproveActor, ApproveActorEntity> {

    ApproveActorConverter INSTANCE = Mappers.getMapper(ApproveActorConverter.class);
}
