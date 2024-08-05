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

package io.innospots.libra.security.logger.event;

import io.innospots.base.events.IEventListener;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.base.events.LoginEvent;
import io.innospots.libra.base.user.SysUserReader;
import io.innospots.libra.base.terminal.TerminalRequestContextHolder;
import io.innospots.libra.security.auth.model.AuthUser;
import io.innospots.libra.security.logger.entity.LoginLogEntity;
import io.innospots.libra.security.logger.operator.LoginLogOperator;
import io.innospots.libra.security.operator.AuthUserOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * LoginEventListener
 *
 * @author chenc
 * @date 2022/2/27
 */
@Slf4j
@Component
public class LoginEventListener implements IEventListener<LoginEvent> {

    private final LoginLogOperator loginLogOperator;

    private final AuthUserOperator authUserOperator;

    private final SysUserReader sysUserReader;

    public LoginEventListener(LoginLogOperator loginLogOperator, AuthUserOperator authUserOperator, SysUserReader sysUserReader) {
        this.loginLogOperator = loginLogOperator;
        this.authUserOperator = authUserOperator;
        this.sysUserReader = sysUserReader;
    }


    @Override
    public Object listen(LoginEvent loginEvent) {
        LoginLogEntity loginLog = new LoginLogEntity();
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setUserName(loginEvent.getUserName());
        loginLog.setCreatedTime(LocalDateTime.now());
        loginLog.setUpdatedTime(LocalDateTime.now());
        AuthUser user = authUserOperator.getByUserName(loginEvent.getUserName());
        if (user != null) {
            Integer userId = user.getUserId();
            loginLog.setUserId(userId);
            UserInfo userInfo = sysUserReader.getUserInfo(userId);
            if (userInfo != null) {
                loginLog.setUserRoleName(String.join(",", userInfo.getRoleNames()));
                loginLog.setUserAvatar(userInfo.getAvatarKey());
            }
        }
        loginLog.fill(TerminalRequestContextHolder.getTerminal());
        loginLog.setDetail(loginEvent.getDetail());
        loginLog.setStatus(loginEvent.getStatus());
        loginLogOperator.save(loginLog);
        return loginLog;
    }

}