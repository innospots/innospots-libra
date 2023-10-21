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

package io.innospots.workflow.console.controller.apps;

import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.console.model.AppQueryRequest;
import io.innospots.workflow.console.service.AppService;
import io.innospots.workflow.core.enums.AppPrimitive;
import io.innospots.workflow.core.node.AppInfo;
import io.innospots.workflow.core.node.apps.AppNodeDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;


/**
 * @author Smars
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "apps/definition")
@ModuleMenu(menuKey = "application")
@Tag(name = "Apps Management Definition")
public class AppManagementController extends BaseController {


    private final AppService appService;

    public AppManagementController(AppService appService) {
        this.appService = appService;
    }

    /**
     * node definition page info
     *
     * @param queryRequest
     * @return Page<NodeDefinition>
     */
    @GetMapping("page")
    @Operation(summary = "node definition page info")
    public InnospotResponse<PageBody<AppInfo>> pageNodeDefinitions(
            AppQueryRequest queryRequest) {
        return success(appService.pageAppInfos(queryRequest));
    }


    @GetMapping("list/online")
    @Operation(summary = "list online nodes")
    public InnospotResponse<List<AppNodeDefinition>> listOnlineNodeDefinitions(
            @Parameter(name = "primitive") @RequestParam(required = false, name = "primitive")
            AppPrimitive primitive) {
        return success(appService.listOnlineNodes(primitive));
    }

    /**
     * create app info
     *
     * @param appInfo app info
     * @return AppInfo
     */
    @OperationLog(operateType = OperateType.CREATE, primaryField = "code")
    @PostMapping("info")
    @Operation(summary = "create app info")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    public InnospotResponse<AppInfo> createAppInfo(@Parameter(name = "app node info", required = true) @Validated @RequestBody AppInfo appInfo) {
        return success(appService.createAppInfo(appInfo));
    }

    /**
     * update app info
     *
     * @param appInfo app info
     * @return AppInfo
     */
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "nodeId")
    @PutMapping("info")
    @Operation(summary = "update app info")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.save}")
    public InnospotResponse<AppInfo> updateAppInfo(@Parameter(name = "app node info", required = true) @Validated @RequestBody AppInfo appInfo) {
        return success(appService.updateAppInfo(appInfo));
    }

    /**
     * update app node definition
     *
     * @param appNodeDefinition app node definition info
     * @return AppNodeDefinition
     */
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "nodeId")
    @PutMapping
    @Operation(summary = "update app node definition")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.save}")
    public InnospotResponse<AppNodeDefinition> updateAppNodeDefinition(@Parameter(name = "node definition", required = true) @Validated @RequestBody AppNodeDefinition appNodeDefinition) {
        return success(appService.updateAppNodeDefinition(appNodeDefinition));
    }

    /**
     * node definition detail info
     *
     * @param nodeId
     * @return NodeDefinition
     */
    @GetMapping("/{nodeId}")
    @Operation(summary = "node definition detail info")
    public InnospotResponse<AppNodeDefinition> getAppNodeDefinitionById(@Parameter(name = "nodeId", required = true) @PathVariable Integer nodeId) {

        return success(appService.getAppNodeDefinitionById(nodeId));
    }


    /**
     * update node definition status
     *
     * @param nodeId
     * @return NodeDefinition
     */
    @OperationLog(operateType = OperateType.UPDATE_STATUS, idParamPosition = 0)
    @PutMapping("/{nodeId}/status/{status}")
    @Operation(summary = "update node definition status")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.status}")
    public InnospotResponse<Boolean> updateNodeStatus(
            @Parameter(name = "nodeId", required = true) @PathVariable Integer nodeId,
            @Parameter(name = "status", required = true) @PathVariable DataStatus status
    ) {
        return success(appService.updateNodeDefinitionStatus(nodeId, status));
    }


    /**
     * delete node definition from db
     *
     * @param nodeId
     * @return NodeDefinition
     */
    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("/{nodeId}")
    @Operation(summary = "delete node definition from db")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    public InnospotResponse<Boolean> deleteNode(
            @Parameter(name = "nodeId", required = true) @PathVariable Integer nodeId
    ) {
        return success(appService.deleteNodeDefinition(nodeId));
    }

    @GetMapping("icons")
    @Operation(summary = "app node icons")
    public InnospotResponse<Map<String,String>> getAppNodeIcon() {
        return success(appService.getAppNodeIcons());
    }

}
