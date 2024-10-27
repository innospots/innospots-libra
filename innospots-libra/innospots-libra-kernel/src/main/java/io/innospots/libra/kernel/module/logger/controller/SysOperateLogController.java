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

package io.innospots.libra.kernel.module.logger.controller;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.kernel.module.logger.model.LogFormQuery;
import io.innospots.libra.kernel.module.logger.model.SysOperateLog;
import io.innospots.libra.kernel.module.logger.model.UserLogInfo;
import io.innospots.libra.kernel.module.logger.operator.SysLogOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author chenc
 * @date 2021/2/7 10:12
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "operate-log")
@ModuleMenu(menuKey = "log-operate")
@Tag(name = "System Operate Log")
public class SysOperateLogController extends BaseController {

    private final SysLogOperator logOperator;


    public SysOperateLogController(SysLogOperator logOperator) {
        this.logOperator = logOperator;
    }

    @GetMapping("page")
    @Operation(summary = "query the operate log", description = "query the operate log")
    public R<PageBody<SysOperateLog>> pageLogs(LogFormQuery queryRequest) {

        PageBody<SysOperateLog> pageBody = logOperator.pageLogs(queryRequest);
        return success(pageBody);
    }

    @GetMapping("{logId}")
    @Operation(summary = "view log")
    public R<SysOperateLog> getLog(@Parameter(name = "logId", required = true) @PathVariable Integer logId) {

        SysOperateLog view = logOperator.getLog(logId);
        return success(view);
    }

    @GetMapping("newest")
    @Operation(summary = "list newest UseLog")
    public R<List<UserLogInfo>> listNewestLogs() {
        return success(logOperator.listNewestLogs());
    }

    @GetMapping("module")
    @Operation(summary = "get module")
    public R<List<String>> getModule() {
        return success(logOperator.getModule());
    }

    @GetMapping("operate")
    @Operation(summary = "get operate")
    public R<List<String>> getOperate() {
        return success(logOperator.getOperate());
    }

    @GetMapping("resource")
    @Operation(summary = "get resource")
    public R<List<String>> getResource() {
        return success(logOperator.getResource());
    }
}