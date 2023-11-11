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

package io.innospots.libra.base.configuration;

import io.innospots.libra.base.menu.AuthMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smars
 * @date 2021/2/15
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "innospots.security")
public class AuthProperties {

    /**
     *
     */
    private AuthMode mode;

    /**
     * 是否启动安全鉴权
     */
    private boolean enabled;

    /**
     * admin console index.html path
     */
    private String indexPage = "/pages/index";

    /**
     * admin console login.html path
     */
    private String loginPage = "/pages/auth/login";

    private String successPage = "/pages/organization/overview";

    private String authFailPage = "/pages/error.html";

    /**
     * token sign key
     */
    private String tokenSigningKey = "innospots-sign-key";
    /**
     * token issuer
     */
    private String tokenIssuer = "innospots";
    /**
     * token expire time minute
     */
    private Integer tokenExpTimeMinute = 180;

    /**
     * these paths can be access in public, not be login
     */
    private List<String> ignoreAuthPaths = new ArrayList<>();

    /**
     * these paths can be access, which not be set role permission, but must be login
     */
    private List<String> ignorePermissionPaths = new ArrayList<>();

//    private List<AppAuthInfo> appAuthInfos = new ArrayList<>();

    /**
     * if true suer admin have all the permission
     */
    private boolean openSuperAdminPermission;

    /**
     * super admin user in the system, the default value is 1, which can't be delete or update name
     */
    private Integer defaultSuperAdminUserId = 1;

}
