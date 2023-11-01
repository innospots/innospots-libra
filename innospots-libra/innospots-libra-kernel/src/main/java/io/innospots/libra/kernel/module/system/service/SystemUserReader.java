/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.libra.kernel.module.system.service;

import io.innospots.libra.kernel.module.system.model.role.RoleInfo;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.base.operator.SysUserReader;
import io.innospots.libra.kernel.module.system.entity.UserRoleEntity;
import io.innospots.libra.kernel.module.system.converter.UserInfoBeanConverter;
import io.innospots.libra.kernel.module.system.operator.RoleOperator;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import io.innospots.libra.kernel.module.system.operator.UserRoleOperator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/26
 */
@Component
public class SystemUserReader implements SysUserReader {

    private final UserOperator userOperator;

    private final RoleOperator roleOperator;

    private final UserRoleOperator userRoleOperator;

    public SystemUserReader(UserOperator userOperator, RoleOperator roleOperator, UserRoleOperator userRoleOperator) {
        this.userOperator = userOperator;
        this.roleOperator = roleOperator;
        this.userRoleOperator = userRoleOperator;
    }

    @Override
    public List<UserInfo> listUsersByIds(List<Integer> userIds) {
        List<SimpleUser> simpleUsers = userOperator.listByIds(userIds);
        return simpleUsers.stream().map(UserInfoBeanConverter.INSTANCE::simple2Info).collect(Collectors.toCollection(() -> new ArrayList<>(simpleUsers.size())));
    }

    @Override
    public List<UserInfo> listUsersByNames(List<String> userNames) {
        List<SimpleUser> simpleUsers = userOperator.listByNames(userNames);
        return simpleUsers.stream().map(UserInfoBeanConverter.INSTANCE::simple2Info).collect(Collectors.toCollection(() -> new ArrayList<>(simpleUsers.size())));
    }

    @Override
    public List<UserInfo> listUserByRoleIds(List<Integer> roleIds) {
        List<UserRoleEntity> userRoleEntities = userRoleOperator.selectByRoleIds(roleIds);
        List<Integer> userIds = userRoleEntities.stream().map(UserRoleEntity::getUserId).collect(Collectors.toList());
        return this.listUsersByIds(userIds);
    }


    @Override
    public UserInfo getUserInfo(Integer userId) {
        UserInfo userInfo = userOperator.getUser(userId);
        List<UserRoleEntity> userRoleEntities = userRoleOperator.selectRoleByUserIds(Collections.singletonList(userId));
        List<Integer> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        List<RoleInfo> roleInfos = roleOperator.listByRoleIds(roleIds);
        userInfo.setRoleIds(roleInfos.stream().map(RoleInfo::getRoleId).collect(Collectors.toList()));
        userInfo.setRoleNames(roleInfos.stream().map(RoleInfo::getRoleName).collect(Collectors.toList()));
        userInfo.setAdmin(roleInfos.stream().anyMatch(RoleInfo::isAdmin));
        return userInfo;
    }

    @Override
    public SimpleUser getSimpleUser(String userName) {
        return null;
    }
}
