package io.innospots.libra.kernel.module.system.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.model.user.UserGroup;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
public interface UserGroupConverter extends BaseBeanConverter<UserGroup, SysUserGroupEntity> {

    UserGroupConverter INSTANCE = Mappers.getMapper(UserGroupConverter.class);


}
