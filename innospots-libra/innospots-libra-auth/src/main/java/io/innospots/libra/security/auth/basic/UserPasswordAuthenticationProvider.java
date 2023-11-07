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

package io.innospots.libra.security.auth.basic;

import io.innospots.base.crypto.PasswordEncoder;
import io.innospots.base.crypto.RsaKeyManager;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.exception.InnospotException;
import io.innospots.libra.base.enums.LoginStatus;
import io.innospots.libra.base.events.LoginEvent;
import io.innospots.libra.security.auth.Authentication;
import io.innospots.libra.security.auth.AuthenticationProvider;
import io.innospots.libra.security.auth.model.AuthUser;
import io.innospots.libra.security.auth.model.LoginRequest;
import io.innospots.libra.security.jwt.JwtAuthManager;
import io.innospots.libra.security.jwt.JwtToken;
import io.innospots.libra.security.operator.AuthUserOperator;
import lombok.AllArgsConstructor;

/**
 * @author Smars
 * @date 2021/2/16
 */
@AllArgsConstructor
public class UserPasswordAuthenticationProvider implements AuthenticationProvider {

    private PasswordEncoder passwordEncoder;

    private JwtAuthManager authManager;

    private AuthUserOperator authUserOperator;

    @Override
    public Authentication authenticate(LoginRequest request) throws InnospotException {
        String userName = RsaKeyManager.decrypt(request.getUsername(), authManager.getPrivateKey());
        String password = RsaKeyManager.decrypt(request.getPassword(), authManager.getPrivateKey());
        if (userName == null || password == null) {
            EventBusCenter.async(new LoginEvent(userName, LoginStatus.FAILURE.name(), "${log.message.login.fail.empty}"));
            throw AuthenticationException.buildDecryptException(this.getClass(), "User name or password is empty, login failed");
        }
        AuthUser authUser = authUserOperator.view(AuthUser.builder().userName(userName).build());
        if (!DataStatus.ONLINE.equals(authUser.getStatus())) {
            EventBusCenter.async(new LoginEvent(userName, LoginStatus.FAILURE.name(), "${log.message.login.user.disabled}"));
            throw AuthenticationException.buildUserException(this.getClass(), "User disabled, login failed");
        }
        if (!passwordEncoder.matches(password, authUser.getPassword())) {
            EventBusCenter.async(new LoginEvent(userName, LoginStatus.FAILURE.name(), "${log.message.login.password.invalid}"));
            throw AuthenticationException.buildPasswordException(this.getClass(), "User name or password error, login failed");
        }

        if (request.getOrgId() != null && request.getOrgId() != 0) {
            authUser.setLastOrgId(request.getOrgId());
        }

        JwtToken jwtToken = authManager.generateToken(authUser);
        Authentication authentication = new Authentication();
        authentication.setAuthenticated(true);
        authentication.setAuthUser(authUser);
        authentication.setRequest(request);
        authentication.setToken(jwtToken);
        EventBusCenter.getInstance().asyncPost(new LoginEvent(userName, LoginStatus.SUCCESS.name(), "${log.message.login.success}"));
        return authentication;
    }
}