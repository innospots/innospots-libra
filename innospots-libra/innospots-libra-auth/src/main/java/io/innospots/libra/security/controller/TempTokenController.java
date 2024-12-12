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

import io.innospots.base.model.response.R;
import io.innospots.libra.security.auth.model.AuthUser;
import io.innospots.libra.security.jwt.JwtAuthManager;
import io.innospots.libra.security.jwt.JwtToken;
import io.innospots.libra.security.operator.AuthUserOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * only active in the dev,local profile config
 * not be used in the production environment
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/19
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "temp-token")
@Tag(name = "Auth Temp Token")
public class TempTokenController {

    private static final Logger logger = LoggerFactory.getLogger(TempTokenController.class);

    private final JwtAuthManager authManager;
    private final AuthUserOperator authUserOperator;

    public TempTokenController(JwtAuthManager authManager, AuthUserOperator authUserOperator) {
        this.authManager = authManager;
        this.authUserOperator = authUserOperator;
    }

    /*
    @GetMapping("{userId}/{orgId}")
    @Operation(description = "temp user token using orgId")
    public InnospotResponse<String> tempToken(@Parameter(name = "userId", required = true) @PathVariable Integer userId,
                                   @PathVariable Integer orgId) {

        AuthUser authUser = authUserOperator.view(AuthUser.builder().userId(userId).build());
        if(orgId!=null){
            authUser.setLastOrgId(orgId);
        }
        JwtToken jwtToken = authManager.generateToken(authUser);
        return InnospotResponse.success("Bearer " + jwtToken.getRawToken());
    }

     */

    @GetMapping("{userId}")
    @Operation(description = "temp user token")
    public R<String> tempToken(@Parameter(name = "userId", required = true) @PathVariable Integer userId) {

        AuthUser authUser = authUserOperator.view(AuthUser.builder().userId(userId).build());
        JwtToken jwtToken = authManager.generateToken(authUser);
        return R.success("" + jwtToken.getRawToken());
    }

    @GetMapping("getCoupon")
    @Operation(description = "n8n策略流测试，后面会删除")
    public Map<String, String> getCoupon(@RequestParam String customerName, @RequestParam String mobileNo) {
        Map<String, String> couponMap = new HashMap<>();
        couponMap.put("customerName", customerName);
        couponMap.put("mobileNo", mobileNo);
        couponMap.put("coupon", "满10.01减10");
        return couponMap;
    }

    @GetMapping("getMessage")
    @Operation(description = "n8n发送短信测试，后面会删除")
    public String getMessage(@RequestParam String message, @RequestParam String mobileNo) {
        logger.info("message: {}， mobileNo: {}", message, mobileNo);
        return "发送成功";
    }
}
