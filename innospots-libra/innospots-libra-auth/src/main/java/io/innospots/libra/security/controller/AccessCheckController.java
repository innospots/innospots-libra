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

package io.innospots.libra.security.controller;

import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.security.auth.AuthToken;
import io.innospots.libra.security.jwt.JwtAuthManager;
import io.innospots.libra.security.jwt.JwtToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import static io.innospots.base.exception.AuthenticationException.buildTokenExpiredException;
import static io.innospots.base.exception.AuthenticationException.buildTokenInvalidException;
import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.security.context.SecurityContextHolder.getContext;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "access")
@Tag(name = "Access Check")
public class AccessCheckController extends BaseController {


    private final JwtAuthManager authManager;

    public AccessCheckController(JwtAuthManager authManager) {
        this.authManager = authManager;
    }

    @GetMapping("check")
    @Operation(summary = "check whether the user is login")
    public R<AuthToken> checkToken(@RequestParam(required = false) String pageUrl, HttpServletRequest httpRequest) {
        JwtToken authToken = (JwtToken) getContext();
        if (authToken == null) {
            throw buildTokenInvalidException(this.getClass());
        }
        if (!authToken.isTokenNotExpired()) {
            throw buildTokenExpiredException(this.getClass());
        }
        boolean pageAccess = (Boolean) httpRequest.getAttribute("pageAccess");
        if (!pageAccess) {
            throw AuthenticationException.buildPermissionException(this.getClass(), "not allow access", pageUrl);
        }
        AuthToken token = authToken.newInstance();
        token.setAccess(true);
        return success(token);
    }
}
