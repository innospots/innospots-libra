package io.innospots.libra.kernel.module.system.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.model.user.UserSimpleGroup;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;
import io.innospots.libra.kernel.module.system.model.user.UserGroup;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
@Mapper
public interface UserSimpleGroupConverter extends BaseBeanConverter<UserSimpleGroup, SysUserGroupEntity> {

    UserSimpleGroupConverter INSTANCE = Mappers.getMapper(UserSimpleGroupConverter.class);


}
