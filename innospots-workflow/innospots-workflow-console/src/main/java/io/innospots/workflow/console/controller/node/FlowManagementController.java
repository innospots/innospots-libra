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

package io.innospots.workflow.console.controller.node;

import io.innospots.base.enums.DataStatus;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.console.model.NodeQueryRequest;
import io.innospots.workflow.console.service.NodeDefinitionService;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.innospots.workflow.core.node.NodeInfo;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
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
@RequestMapping(PATH_ROOT_ADMIN + "flow-node/management")
@ModuleMenu(menuKey = "node-management")
@Tag(name = "Node Definition")
public class FlowManagementController extends BaseController {


    private final NodeDefinitionService nodeDefinitionService;

    public FlowManagementController(NodeDefinitionService nodeDefinitionService) {
        this.nodeDefinitionService = nodeDefinitionService;
    }

    /**
     * node definition page info
     *
     * @param queryRequest
     * @return Page<NodeDefinition>
     */
    @GetMapping("page")
    @Operation(summary = "node definition page info")
    public InnospotResponse<PageBody<NodeInfo>> pageNodeDefinitions(
            NodeQueryRequest queryRequest) {
        return success(nodeDefinitionService.pageAppInfos(queryRequest));
    }


    @GetMapping("list/online")
    @Operation(summary = "list online nodes")
    public InnospotResponse<List<NodeDefinition>> listOnlineNodeDefinitions(
            @Parameter(name = "primitive") @RequestParam(required = false, name = "primitive")
            NodePrimitive primitive) {
        return success(nodeDefinitionService.listOnlineNodes(primitive));
    }

    /**
     * create app info
     *
     * @param nodeInfo app info
     * @return AppInfo
     */
    @OperationLog(operateType = OperateType.CREATE, primaryField = "code")
    @PostMapping("info")
    @Operation(summary = "create app info")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    public InnospotResponse<NodeInfo> createAppInfo(@Parameter(name = "app node info", required = true) @Validated @RequestBody NodeInfo nodeInfo) {
        return success(nodeDefinitionService.createAppInfo(nodeInfo));
    }

    /**
     * update app info
     *
     * @param nodeInfo app info
     * @return AppInfo
     */
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "nodeId")
    @PutMapping("info")
    @Operation(summary = "update app info")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.save}")
    public InnospotResponse<NodeInfo> updateAppInfo(@Parameter(name = "app node info", required = true) @Validated @RequestBody NodeInfo nodeInfo) {
        return success(nodeDefinitionService.updateAppInfo(nodeInfo));
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
    public InnospotResponse<NodeDefinition> updateAppNodeDefinition(@Parameter(name = "node definition", required = true) @Validated @RequestBody NodeDefinition appNodeDefinition) {
        return success(nodeDefinitionService.updateAppNodeDefinition(appNodeDefinition));
    }

    /**
     * node definition detail info
     *
     * @param nodeId
     * @return NodeDefinition
     */
    @GetMapping("/{nodeId}")
    @Operation(summary = "node definition detail info")
    public InnospotResponse<NodeDefinition> getAppNodeDefinitionById(@Parameter(name = "nodeId", required = true) @PathVariable Integer nodeId) {

        return success(nodeDefinitionService.getAppNodeDefinitionById(nodeId));
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
        return success(nodeDefinitionService.updateNodeDefinitionStatus(nodeId, status));
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
        return success(nodeDefinitionService.deleteNodeDefinition(nodeId));
    }

    @GetMapping("icons")
    @Operation(summary = "app node icons")
    public InnospotResponse<Map<String,String>> getAppNodeIcon() {
        return success(nodeDefinitionService.getAppNodeIcons());
    }

}
