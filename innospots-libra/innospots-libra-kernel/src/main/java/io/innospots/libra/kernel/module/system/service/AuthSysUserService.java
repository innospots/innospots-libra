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

import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.base.user.AuthSysUserReader;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import io.innospots.libra.kernel.module.system.operator.UserRoleOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author castor_ling
 * @date 2024/11/29
 */
@Slf4j
@Service
public class AuthSysUserService implements AuthSysUserReader {

    private final UserOperator userOperator;

    private final UserRoleOperator userRoleOperator;

    public AuthSysUserService(UserOperator userOperator,UserRoleOperator userRoleOperator) {
        this.userOperator = userOperator;
        this.userRoleOperator = userRoleOperator;
    }
    @Override
    public UserInfo creatOauthUser(UserInfo userInfo) {
        UserInfo savedUserInfo = userOperator.creatOauthUser(userInfo);
        this.userRoleOperator.saveUserRoles(savedUserInfo.getUserId(),userInfo.getRoleIds());
        return savedUserInfo;
    }
}
