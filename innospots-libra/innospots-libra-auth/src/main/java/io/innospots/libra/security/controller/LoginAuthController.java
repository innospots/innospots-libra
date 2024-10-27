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

import io.innospots.base.config.InnospotsConfigProperties;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.security.auth.AuthToken;
import io.innospots.libra.security.auth.Authentication;
import io.innospots.libra.security.auth.AuthenticationProvider;
import io.innospots.libra.security.auth.model.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @date 2021/2/16
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "login-auth")
@Tag(name = "Auth Login")
public class LoginAuthController extends BaseController {

    private final AuthenticationProvider authenticationProvider;
    private final InnospotsConfigProperties innospotsConfigProperties;

    public LoginAuthController(AuthenticationProvider authenticationProvider, InnospotsConfigProperties innospotsConfigProperties) {
        this.authenticationProvider = authenticationProvider;
        this.innospotsConfigProperties = innospotsConfigProperties;
    }


    @PostMapping
    @Operation(summary = "login", description = "authenticate userName and password")
    public R<AuthToken> authenticate(@RequestBody LoginRequest request) {

        Authentication authentication = authenticationProvider.authenticate(request);
        return success(authentication.getToken().newInstance());
    }

    @GetMapping(path = "public-key")
    @Operation(summary = "get public key", description = "get public key")
    public R<String> getPublicKey() {
        return success(innospotsConfigProperties.getPublicKey());
    }

}