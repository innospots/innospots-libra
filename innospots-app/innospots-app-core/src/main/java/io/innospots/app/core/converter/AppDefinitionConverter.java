package io.innospots.app.core.converter;

import io.innospots.app.core.entity.AppDefinitionEntity;
import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.model.AppResource;
import io.innospots.app.core.model.AppSetting;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
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


    default String strToSettings(AppSetting appSetting){
        return JSONUtils.toJsonString(appSetting);
    }

    default AppSetting settingsToStr(String settings){
        return JSONUtils.parseObject(settings, AppSetting.class);
    }

    default String strToAppResource(AppResource appResource){
        return JSONUtils.toJsonString(appResource);
    }

    default AppResource appResourceToStr(String appResource){
        return JSONUtils.parseObject(appResource, AppResource.class);
    }
}
