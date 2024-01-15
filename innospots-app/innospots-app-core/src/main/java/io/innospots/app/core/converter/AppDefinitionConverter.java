package io.innospots.app.core.converter;

import io.innospots.app.core.entity.AppDefinitionEntity;
import io.innospots.app.core.model.AppDefinition;
import io.innospots.base.converter.BaseBeanConverter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@Mapper
public interface AppDefinitionConverter extends BaseBeanConverter<AppDefinition, AppDefinitionEntity> {

    AppDefinitionConverter INSTANCE = Mappers.getMapper(AppDefinitionConverter.class);
}
