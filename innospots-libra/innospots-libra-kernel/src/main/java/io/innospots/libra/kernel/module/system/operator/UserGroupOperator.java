package io.innospots.libra.kernel.module.system.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.user.UserSimpleGroup;
import io.innospots.libra.kernel.module.system.converter.UserSimpleGroupConverter;
import io.innospots.libra.kernel.module.system.dao.UserGroupDao;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;
import io.innospots.libra.kernel.module.system.model.user.GroupForm;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
@Component
public class UserGroupOperator extends ServiceImpl<UserGroupDao, SysUserGroupEntity> {


    public UserSimpleGroup createGroup(GroupForm groupForm) {
        UserSimpleGroup simpleGroup = new UserSimpleGroup();
        simpleGroup.setGroupCode(groupForm.getGroupCode());
        simpleGroup.setGroupName(groupForm.getGroupName());
        simpleGroup.setParentGroupId(groupForm.getParentGroupId());
        simpleGroup.setHeadUserId(groupForm.getHeadUserId());
        return saveGroup(simpleGroup, true);
    }

    public UserSimpleGroup saveGroup(UserSimpleGroup group, boolean create) {
        SysUserGroupEntity entity = UserSimpleGroupConverter.INSTANCE.modelToEntity(group);
        QueryWrapper<SysUserGroupEntity> qw = new QueryWrapper<>();
        long cnt = 0;
        if (group.getGroupId() == null && StringUtils.isEmpty(group.getGroupCode())) {
            do {
                entity.setGroupCode(RandomStringUtils.randomAlphabetic(8));
                qw.lambda().eq(SysUserGroupEntity::getGroupCode, entity.getGroupCode());
                cnt = this.count(qw);
            } while (cnt > 0);
        } else {
            qw.lambda().eq(SysUserGroupEntity::getGroupCode, entity.getGroupCode())
                    .ne(group.getGroupId() != null, SysUserGroupEntity::getGroupId, group.getGroupId());
            cnt = this.count(qw);
        }

        if (cnt > 0) {
            throw ResourceException.buildExistException(this.getClass(), "group code has exist", entity.getGroupCode());
        }
        qw = new QueryWrapper<>();
        qw.lambda().eq(SysUserGroupEntity::getGroupName, entity.getGroupName())
                .ne(group.getGroupId() != null, SysUserGroupEntity::getGroupId, group.getGroupId());
        cnt = this.count(qw);
        if (cnt > 0) {
            throw ResourceException.buildExistException(this.getClass(), "group name has exist", entity.getGroupName());
        }
        if (create) {
            this.save(entity);
            group.setGroupId(entity.getGroupId());
        } else {
            this.updateById(entity);
        }
        return group;
    }


    public boolean deleteGroup(Integer groupId) {
        return this.baseMapper.deleteById(groupId) > 0;
    }

    public UserSimpleGroup getGroup(Integer groupId) {
        SysUserGroupEntity entity = this.getById(groupId);
        return UserSimpleGroupConverter.INSTANCE.entityToModel(entity);
    }

    public List<UserSimpleGroup> listGroups() {
        QueryWrapper<SysUserGroupEntity> qw = new QueryWrapper<>();
        List<SysUserGroupEntity> entities = this.list(qw);
        return UserSimpleGroupConverter.INSTANCE.entitiesToModels(entities);
    }


}
