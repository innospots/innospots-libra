package io.innospots.libra.kernel.module.system.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.user.UserSimpleGroup;
import io.innospots.libra.kernel.module.system.entity.SysUserEntity;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;
import io.innospots.libra.kernel.module.system.model.user.UserGroup;
import io.innospots.libra.kernel.module.system.model.user.UserGroupInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
@Mapper
public interface UserGroupConverter extends BaseBeanConverter<UserGroup, SysUserGroupEntity> {

    UserGroupConverter INSTANCE = Mappers.getMapper(UserGroupConverter.class);


    static UserGroupInfo userGroupInfo(SysUserGroupEntity groupEntity, SysUserEntity userEntity,int userCount){
        UserGroupInfo groupInfo = new UserGroupInfo();
        groupInfo.setGroupId(groupEntity.getGroupId());
        groupInfo.setGroupName(groupEntity.getGroupName());
        groupInfo.setGroupCode(groupEntity.getGroupCode());
        groupInfo.setParentGroupId(groupEntity.getParentGroupId());
        groupInfo.setHeadUserId(groupEntity.getHeadUserId());
        groupInfo.setAssistantUserIds(JSONUtils.toList(groupEntity.getAssistantUserIds(),Integer.class));
        groupInfo.setUserCount(userCount);
        if(userEntity!=null){
            groupInfo.setHeaderName(userEntity.getUserName());
        }
        return groupInfo;
    }

}
