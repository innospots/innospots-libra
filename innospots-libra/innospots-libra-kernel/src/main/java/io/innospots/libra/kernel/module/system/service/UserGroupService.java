package io.innospots.libra.kernel.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.kernel.module.system.converter.UserGroupConverter;
import io.innospots.libra.kernel.module.system.converter.UserInfoConverter;
import io.innospots.libra.kernel.module.system.entity.SysUserEntity;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;
import io.innospots.libra.kernel.module.system.model.user.UserGroup;
import io.innospots.libra.kernel.module.system.operator.UserGroupOperator;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/7
 */
@Service
public class UserGroupService {

    private final UserGroupOperator userGroupOperator;

    private final UserOperator userOperator;

    public UserGroupService(UserGroupOperator userGroupOperator, UserOperator userOperator) {
        this.userGroupOperator = userGroupOperator;
        this.userOperator = userOperator;
    }


    public List<UserGroup> listUserGroups() {
        QueryWrapper<SysUserGroupEntity> qw = new QueryWrapper<>();
        List<UserGroup> groups = UserGroupConverter.INSTANCE.entitiesToModels(userGroupOperator.list(qw));
        Map<Integer, UserGroup> groupMap = groups.stream().collect(Collectors.toMap(UserGroup::getGroupId, Function.identity()));
        QueryWrapper<SysUserEntity> userQw = new QueryWrapper<>();
        userQw.select("group_id", "COUNT(*) AS userCount")
                .groupBy("group_id");
        Map<String, Object> um = userOperator.getMap(userQw);
        for (UserGroup group : groups) {
            Object v = um.get("userCount");
            group.setUserCount(v == null ? 0 : Integer.parseInt(v.toString()));
            if (group.getParentGroupId() != null) {
                group.setParentGroup(groupMap.get(group.getParentGroupId()));
            }
        }
        return groups;
    }

    public List<SimpleUser> listUserByGroupId(Integer groupId, boolean includeSubGroup) {
        Set<Integer> groupIds = new HashSet<>();
        groupIds.add(groupId);
        Set<Integer> totalGroupIds = new HashSet<>();
        totalGroupIds.add(groupId);
        if(includeSubGroup){
            fillSubGroupIds(groupIds, totalGroupIds);
        }

        QueryWrapper<SysUserGroupEntity> gQw = new QueryWrapper<>();
        gQw.lambda().in(SysUserGroupEntity::getGroupId, totalGroupIds);
        Map<Integer,String> groupMap = userGroupOperator.list(gQw)
                .stream().collect(Collectors.toMap(SysUserGroupEntity::getGroupId, SysUserGroupEntity::getGroupName));

        QueryWrapper<SysUserEntity> qw = new QueryWrapper<>();
        qw.lambda().in(SysUserEntity::getGroupId, totalGroupIds);
        List<SysUserEntity> userEntities = userOperator.list(qw);

        return UserInfoConverter.INSTANCE.entitiesToSimpleUsers(userEntities, groupMap);
    }

    public void fillSubGroupIds(Set<Integer> groupIds, Set<Integer> totalGroupIds) {
        QueryWrapper<SysUserGroupEntity> qw = new QueryWrapper<>();
        qw.lambda().in(SysUserGroupEntity::getParentGroupId, groupIds);
        List<SysUserGroupEntity> subGroupEntities = userGroupOperator.list(qw);
        if(CollectionUtils.isEmpty(subGroupEntities)){
            return;
        }
        Set<Integer> parentGroups = new HashSet<>();
        for (SysUserGroupEntity sub : subGroupEntities) {
            totalGroupIds.add(sub.getGroupId());
            parentGroups.add(sub.getGroupId());
        }//end for
        fillSubGroupIds(parentGroups, totalGroupIds);
    }

}
