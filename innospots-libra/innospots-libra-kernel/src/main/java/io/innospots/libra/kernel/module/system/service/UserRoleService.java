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

import io.innospots.base.model.PageBody;
import io.innospots.libra.kernel.module.system.model.role.RoleInfo;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.model.QueryRequest;
import io.innospots.libra.kernel.module.system.entity.UserRoleEntity;
import io.innospots.libra.kernel.module.system.model.role.RoleDelEvent;
import io.innospots.libra.kernel.module.system.operator.RoleOperator;
import io.innospots.libra.kernel.module.system.operator.RoleResourceOperator;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import io.innospots.libra.kernel.module.system.operator.UserRoleOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenc
 * @version 1.0.0
 * @date 2022/1/18
 */
@Service
public class UserRoleService {

    private final UserOperator userOperator;
    private final RoleOperator roleOperator;
    private final UserRoleOperator userRoleOperator;
    private final RoleResourceOperator roleResourceOperator;


    public UserRoleService(UserOperator userOperator, RoleOperator roleOperator,
                           UserRoleOperator userRoleOperator, RoleResourceOperator roleResourceOperator) {
        this.userOperator = userOperator;
        this.roleOperator = roleOperator;
        this.userRoleOperator = userRoleOperator;
        this.roleResourceOperator = roleResourceOperator;
    }

    /**
     * list role user
     */
    public List<SimpleUser> listRoleUsers(Integer roleId) {
        List<UserRoleEntity> userRoleEntities = userRoleOperator
                .selectByRoleIds(Collections.singletonList(roleId));
        if (CollectionUtils.isNotEmpty(userRoleEntities)) {
            List<Integer> userIds = userRoleEntities.stream().map(UserRoleEntity::getUserId).collect(Collectors.toList());
            return userOperator.listByIds(userIds);
        }
        return Collections.emptyList();
    }

    /**
     * get user roles
     *
     * @return
     */
    public List<String> getUserRoles() {
        List<UserRoleEntity> userRoleEntities
                = userRoleOperator.selectRoleByUserId(CCH.userId());
        if (CollectionUtils.isEmpty(userRoleEntities)) {
            return Collections.emptyList();
        }
        List<Integer> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        List<RoleInfo> roleInfos = roleOperator.listByRoleIds(roleIds);
        return roleInfos.stream().map(RoleInfo::getRoleCode).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRole(Integer roleId) {
        Boolean result = roleOperator.deleteRole(roleId);
        // delete role user
        if (result) {
            BeanContextAware.applicationContext().publishEvent(new RoleDelEvent(roleId));
        }
        return result;
    }

    public PageBody<RoleInfo> pageRoles(QueryRequest request) {
        PageBody<RoleInfo> roleInfoPageBody = roleOperator.list(request);
        List<RoleInfo> roleInfos = roleInfoPageBody.getList();
        List<Map<String, Object>> userRoles = userRoleOperator.selectCountByRoleId();
        Map<Integer, Integer> userRoleMap = null;
        if (CollectionUtils.isNotEmpty(userRoles)) {
            userRoleMap = new HashMap<>(userRoles.size());
            for (Map<String, Object> map : userRoles) {
                // TODO h2数据库存在大小写敏感问题，需要做兼容 by Alfred
                userRoleMap.put((int) map.get("ROLE_ID"), ((Long) map.get("NUM")).intValue());
            }
        }
        for (RoleInfo roleInfo : roleInfos) {
            roleInfo.setNumberOfRole(0);
            if (MapUtils.isNotEmpty(userRoleMap) && userRoleMap.get(roleInfo.getRoleId()) != null) {
                roleInfo.setNumberOfRole(userRoleMap.get(roleInfo.getRoleId()));
            }
        }
        return roleInfoPageBody;
    }

}