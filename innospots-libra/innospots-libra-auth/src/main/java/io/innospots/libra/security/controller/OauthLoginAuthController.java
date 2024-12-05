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
import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.security.auth.AuthToken;
import io.innospots.libra.security.auth.Authentication;
import io.innospots.libra.security.auth.AuthenticationProvider;
import io.innospots.libra.security.auth.model.LoginRequest;
import io.innospots.libra.security.auth.model.OauthLoginProvider;
import io.innospots.libra.security.config.OauthProvider;
import io.innospots.libra.security.config.OauthProviderProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author castor_ling
 * @date 2024/11/27
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "login-oauth")
@Tag(name = "OAuth Login")
public class OauthLoginAuthController extends BaseController {

    @Resource(name = "oauthAuthenticationProvider")
    private  AuthenticationProvider authenticationProvider;
    @Autowired
    OauthProviderProperties oauthProviderProperties;
    @Autowired
    InnospotsConfigProperties innospotsConfigProperties;

    @PostMapping
    @Operation(summary = "login", description = "oauth authenticate")
    public R<AuthToken> authenticate(@RequestBody LoginRequest request) throws InnospotException  {
        Authentication authentication = authenticationProvider.authenticate(request);

        return success(authentication.getToken().newInstance());
    }

    @GetMapping(path = "list")
    @Operation(summary = "oauth list", description = "get oauth list")
    public R<List<OauthLoginProvider>> getOauthList() {
        List<OauthLoginProvider> providers = new ArrayList<>();
        if(oauthProviderProperties.isEnabled()){
            for (OauthProvider provider : oauthProviderProperties.getProviders()){
                OauthProvider.UrlInfo authInfo = provider.getAuthInfo();

                if(provider.isEnabled() && authInfo != null){
                    if(authInfo.getParams().containsKey("redirect_uri")){
                        String redirectUri = authInfo.getParams().get("redirect_uri").toString();
                        authInfo.getParams().put("redirect_uri",innospotsConfigProperties.getHost() + redirectUri);
                    }

                    OauthLoginProvider oauthProvider = OauthLoginProvider.builder()
                            .providerName(provider.getProviderName())
                            .icon(provider.getIcon())
                            .method(authInfo.getMethod())
                            .url(authInfo.getUrl())
                            .params(authInfo.getParams())
                            .build();
                    providers.add(oauthProvider);
                }
            }
        }
        return success(providers);
    }

}