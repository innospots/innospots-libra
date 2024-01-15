package io.innospots.app.core.converter;

import io.innospots.app.core.entity.AppTemplateEntity;
import io.innospots.app.core.model.AppTemplate;
import io.innospots.base.converter.BaseBeanConverter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@Mapper
public interface AppTemplateConverter extends BaseBeanConverter<AppTemplate, AppTemplateEntity> {

    AppTemplateConverter INSTANCE = Mappers.getMapper(AppTemplateConverter.class);

}
