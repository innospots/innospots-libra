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

package io.innospots.libra.security.logger.controller;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.security.logger.model.LogFormQuery;
import io.innospots.libra.security.logger.model.LoginLog;
import io.innospots.libra.security.logger.operator.LoginLogOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @date 2021/12/14
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "login-log")
@ModuleMenu(menuKey = "log-login")
@Tag(name = "Auth Login Log")
public class LoginLogController {

    private final LoginLogOperator loginLogOperator;

    public LoginLogController(LoginLogOperator loginLogOperator) {
        this.loginLogOperator = loginLogOperator;
    }

    @GetMapping("page")
    @Operation(description = "page logs")
    public R<PageBody<LoginLog>> pageLoginLogs(LogFormQuery request) {
        PageBody<LoginLog> pageModel = loginLogOperator.pageLogs(request);
        return success(pageModel);
    }

    @GetMapping("{logId}")
    @Operation(description = "view log detail")
    public R<LoginLog> view(@Parameter(name = "logId", required = true) @PathVariable Integer logId) {

        LoginLog view = loginLogOperator.getLog(logId);
        return success(view);
    }

    @GetMapping("current-user/page")
    @Operation(description = "list login user's logs")
    public R<PageBody<LoginLog>> pageCurrentUserLogs(
            @RequestParam(defaultValue = "1", required = false) Integer page,
            @RequestParam(defaultValue = "20", required = false) Integer size) {
        return success(loginLogOperator.pageLogs(CCH.userId(), page, size));
    }

    @GetMapping("latest")
    @Operation(description = "current user latest login log")
    public R<LoginLog> getLatest() {
        return success(loginLogOperator.getLatest());
    }

    @GetMapping("browsers")
    @Operation(description = "list browser")
    public R<List<String>> listBrowsers() {
        return success(loginLogOperator.listBrowsers());
    }

    @GetMapping("operation-systems")
    @Operation(description = "list os")
    public R<List<String>> listOperationSystems() {
        return success(loginLogOperator.listOperationSystems());
    }
}