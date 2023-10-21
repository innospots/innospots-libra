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

package io.innospots.connector.schema.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.connector.schema.service.Oauth2CallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Alfred
 * @date 2023/3/6
 */
@Slf4j
@Controller
@RequestMapping(PathConstant.ROOT_PATH + "oauth2")
@Tag(name = "App Credential Oauth2")
public class CredentialOauth2Controller {

    private Oauth2CallbackService authorizationCallbackService;

    public CredentialOauth2Controller(Oauth2CallbackService authorizationCallbackService) {
        this.authorizationCallbackService = authorizationCallbackService;
    }

    @GetMapping("callback/{appCode}")
    @Operation(summary = "oauth2 credential callback")
    public String callback(
            @Parameter(name = "appCode") @PathVariable String appCode,
            @Parameter(name = "code") @RequestParam("code") String code,
            @Parameter(name = "state") @RequestParam("state") String state) {
        boolean success = authorizationCallbackService.authCallback(appCode, code, state);
        return "redirect:/#/oauth-result?state=" + state + "&appCode=" + appCode + "&success=" + success;
    }

//    @Deprecated
//    @GetMapping("status/{clientId}/{clientSecret}")
//    @Operation(summary = "oauth2 credential status")
//    public InnospotResponse<Boolean> result(
//            @Parameter(name = "clientId") @PathVariable("clientId") String clientId,
//            @Parameter(name = "clientSecret") @PathVariable("clientSecret") String clientSecret
//    ) {
//        return null;
//        //String value = systemTempCacheOperator.get(clientId + "_" + clientSecret);
//        //return InnospotResponse.success(!StringUtils.isBlank(value));
//    }
}
