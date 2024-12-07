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

import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.utils.CCH;
import io.innospots.libra.kernel.module.system.entity.RoleResourceEntity;
import io.innospots.libra.kernel.module.system.entity.UserRoleEntity;
import io.innospots.libra.kernel.module.system.enums.SystemRoleCode;
import io.innospots.libra.kernel.module.system.operator.RoleResourceOperator;
import io.innospots.libra.kernel.module.system.operator.UserRoleOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2022/1/12
 */
@Service
public class UserResourceService {

    private final RoleResourceOperator roleResourceOperator;

    private final UserRoleOperator userRoleOperator;


    public UserResourceService(RoleResourceOperator roleResourceOperator,
                               UserRoleOperator userRoleOperator) {
        this.roleResourceOperator = roleResourceOperator;
        this.userRoleOperator = userRoleOperator;
    }


    /**
     * according role resource and user role, get user resources
     *
     * @param userId
     * @return
     */
    public List<RoleResourceEntity> getUserResourceIds(Integer userId) {
        List<UserRoleEntity> userRoleEntities
                = userRoleOperator.selectRoleByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleEntities)) {
            throw AuthenticationException.buildPermissionException(this.getClass(), "user is not have role, userId:" + userId);
        }
        List<Integer> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        return roleResourceOperator.listByRoleIds(roleIds);
    }

    public boolean hasResource(Integer userId, String itemKey) {
        List<UserRoleEntity> userRoleEntities
                = userRoleOperator.selectRoleByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleEntities)) {
            return false;
        }

        List<Integer> roleIds = userRoleEntities.stream()
                .map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        return roleResourceOperator.hasRoleResource(roleIds, itemKey);
    }

    /**
     * current login user has admin role
     *
     * @return
     */
    public boolean isSuperAdminRole() {
        return userRoleOperator.hashRole(CCH.userId(), SystemRoleCode.SUPER_ADMIN.getRoleId());
    }

    public boolean isProjectAdminRole() {
        return userRoleOperator.hashRole(CCH.userId(), SystemRoleCode.PROJECT_ADMIN.getRoleId());
    }
}