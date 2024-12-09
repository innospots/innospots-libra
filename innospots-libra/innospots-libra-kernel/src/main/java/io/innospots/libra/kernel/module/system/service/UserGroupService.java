package io.innospots.libra.kernel.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.libra.kernel.module.system.converter.UserGroupConverter;
import io.innospots.libra.kernel.module.system.converter.UserInfoConverter;
import io.innospots.libra.kernel.module.system.entity.SysUserEntity;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;
import io.innospots.libra.kernel.module.system.model.user.UserGroup;
import io.innospots.libra.kernel.module.system.model.user.UserGroupInfo;
import io.innospots.libra.kernel.module.system.operator.UserGroupOperator;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
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

    public boolean deleteGroup(Integer groupId){
       boolean del = userGroupOperator.deleteGroup(groupId);
       if(del){
           del = userOperator.resetUserGroup(groupId);
       }
       return del;
    }

    public boolean updateUserPersonGroup(List<Integer> userIds, Integer groupId){
        return userOperator.updateUsersGroup(userIds,groupId);
    }

    public boolean updateGroupHeader(Integer groupId, String headerUserId){
        UpdateWrapper<SysUserGroupEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(SysUserGroupEntity::getHeadUserId, headerUserId)
                .eq(SysUserGroupEntity::getGroupId, groupId);
        return userGroupOperator.update(uw);
    }

    public boolean updateAssistantUsers(Integer groupId, List<Integer> assistantUserIds){
        UpdateWrapper<SysUserGroupEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(SysUserGroupEntity::getAssistantUserIds, assistantUserIds)
                .eq(SysUserGroupEntity::getGroupId, groupId);
        return userGroupOperator.update(uw);
    }

    public UserGroupInfo getUserGroupInfo(Integer groupId){
        SysUserGroupEntity groupEntity = userGroupOperator.getById(groupId);

        QueryWrapper<SysUserEntity> userQw = new QueryWrapper<>();
        userQw.select("COUNT(*) AS userCount")
              .eq("group_id", groupId);
        Map<String,Object> m = userOperator.getMap(userQw);
        int count = m.get("userCount") == null ? 0 : Integer.parseInt(m.get("userCount").toString());
        SysUserEntity userEntity = null;
        if(groupEntity.getHeadUserId()!=null){
            userEntity = userOperator.getById(groupEntity.getHeadUserId());
        }
        return UserGroupConverter.userGroupInfo(groupEntity, userEntity, count);
    }



    public List<UserGroup> listUserGroups() {
        QueryWrapper<SysUserGroupEntity> qw = new QueryWrapper<>();
        List<UserGroup> groups = UserGroupConverter.INSTANCE.entitiesToModels(userGroupOperator.list(qw));
        Map<Integer, UserGroup> groupMap = groups.stream().collect(Collectors.toMap(UserGroup::getGroupId, Function.identity()));
        QueryWrapper<SysUserEntity> userQw = new QueryWrapper<>();
        userQw.select("group_id", "COUNT(*) AS userCount")
                .groupBy("group_id");
        List<Map<String, Object>> umList = userOperator.listMaps(userQw);
        Map<Integer, Object> um = new HashMap<>();
        for (Map<String, Object> map : umList) {
            Object gid = map.get("group_id");
            Object ut = map.get("userCount");
            if(gid !=null){
                um.put(Integer.parseInt(gid.toString()), ut);
            }
        }
        for (UserGroup group : groups) {
            Object v = um.get(group.getGroupId());
            group.setUserCount(v == null ? 0 : Integer.parseInt(v.toString()));
            if (group.getParentGroupId() != null) {
                group.setParentGroup(groupMap.get(group.getParentGroupId()));
            }
        }
        return groups;
    }

    public List<SimpleUser> listUserByGroupId(Integer groupId,String userName, boolean includeSubGroup) {
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
        qw.lambda().in(SysUserEntity::getGroupId, totalGroupIds)
                .like(userName!=null, SysUserEntity::getUserName, "%"+userName+"%");
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
