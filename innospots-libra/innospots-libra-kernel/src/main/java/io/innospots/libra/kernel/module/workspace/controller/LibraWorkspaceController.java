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

package io.innospots.libra.kernel.module.workspace.controller;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.workspace.model.*;
import io.innospots.libra.kernel.module.workspace.operator.WorkspaceOperator;
import io.innospots.libra.kernel.module.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/18
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workspace")
@ModuleMenu(menuKey = "libra-workspace")
@Tag(name = "Workspace Board")
public class LibraWorkspaceController {

    private final WorkspaceOperator workspaceOperator;

    private final WorkspaceService workspaceService;

    public LibraWorkspaceController(WorkspaceOperator workspaceOperator, WorkspaceService workspaceService) {
        this.workspaceOperator = workspaceOperator;
        this.workspaceService = workspaceService;
    }

    @OperationLog(operateType = OperateType.UPDATE, primaryField = "id")
    @PostMapping
    @Operation(summary = "update workspace")
    public R<Workspace> updateWorkspace(@Parameter(name = "pageDetail", required = true) @RequestBody Workspace workspace) {
        Workspace update = workspaceOperator.updateWorkspace(workspace);
        return success(update);
    }

    @ResourceItemOperation
    @GetMapping
    @Operation(summary = "get current user workspace")
    public R<Workspace> getWorkspaceByCurrentUser() {
        Workspace workspace = workspaceOperator.getWorkspaceByCurrentUser();
        return success(workspace);
    }

    @ResourceItemOperation
    @GetMapping("latest-activity")
    @Operation(summary = "get latest activity")
    public R<News> getLatestActivity() {
        return success(workspaceService.getActivityInfo());
    }

    @ResourceItemOperation
    @GetMapping("update-app")
    @Operation(summary = "get app update")
    public R<PageBody<AppUpdateInfo>> getUpdateApp() {
        PageBody<AppUpdateInfo> pageBody = new PageBody<>();
        List<AppUpdateInfo> appUpdateInfos = new ArrayList<>();
        AppUpdateInfo appUpdateInfo = new AppUpdateInfo();
        //TODO TO BE CONTINUE
        appUpdateInfos.add(appUpdateInfo);
        pageBody.setCurrent(1L);
        pageBody.setPageSize(20L);
        pageBody.setTotal(3L);
        pageBody.setTotalPage(1L);
        pageBody.setList(appUpdateInfos);
        return success(pageBody);
    }

    @ResourceItemOperation
    @GetMapping("latest-news")
    @Operation(summary = "get news info")
    public R<NewsInfo> getNewsInfo() {
        return success(workspaceService.getNewsInfo());
    }

    @ResourceItemOperation
    @GetMapping("system")
    @Operation(summary = "get system info")
    public R<SystemInfo> getSystemInfo() {
        return success(workspaceOperator.getSystemInfo());
    }


}