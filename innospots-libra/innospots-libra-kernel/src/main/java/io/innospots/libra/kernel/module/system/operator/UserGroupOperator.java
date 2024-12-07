package io.innospots.libra.kernel.module.system.operator;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.model.user.UserGroup;
import io.innospots.libra.kernel.module.system.converter.UserGroupConverter;
import io.innospots.libra.kernel.module.system.dao.UserGroupDao;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
public class UserGroupOperator extends ServiceImpl<UserGroupDao, SysUserGroupEntity> {



    public UserGroup saveGroup(UserGroup group) {
        SysUserGroupEntity entity  = UserGroupConverter.INSTANCE.modelToEntity(group);
        this.saveOrUpdate(entity);
        group.setGroupId(entity.getGroupId());
        return group;
    }


    public int deleteGroup(Integer groupId) {
        return this.baseMapper.deleteById(groupId);
    }

    public UserGroup getGroup(Integer groupId) {
        SysUserGroupEntity entity = this.getById(groupId);
        return UserGroupConverter.INSTANCE.entityToModel(entity);
    }


}
