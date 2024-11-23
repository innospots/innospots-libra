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

import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.field.SelectItem;
import io.innospots.base.model.response.R;
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

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;


/**
 * @author Smars
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "flow-node/definition")
@ModuleMenu(menuKey = "node-management")
@Tag(name = "Node Definition")
public class FlowNodeDefinitionController extends BaseController {


    private final NodeDefinitionService nodeDefinitionService;

    public FlowNodeDefinitionController(NodeDefinitionService nodeDefinitionService) {
        this.nodeDefinitionService = nodeDefinitionService;
    }

    /**
     * page node definition
     *
     * @return Page<NodeDefinition>
     */
    @GetMapping("page")
    @Operation(summary = "page node definition")
    public R<PageBody<NodeInfo>> pageNodeDefinitions(
            @RequestParam(required = false, name = "dataStatus") DataStatus dataStatus,
            @RequestParam(name = "flowTplId") Integer flowTplId,
            @RequestParam(required = false, name = "nodeGroupId") Integer nodeGroupId,
            @RequestParam(required = false, name = "queryInput") String queryInput,
            @RequestParam(required = false, name = "page", defaultValue = "1") int page,
            @RequestParam(required = false, name = "size", defaultValue = "20") int size,
            @RequestParam(required = false, name = "primitive") String primitive
    ) {
        NodeQueryRequest queryRequest = new NodeQueryRequest();
        queryRequest.setPage(page);
        queryRequest.setSize(size);
        queryRequest.setQueryInput(queryInput);
        queryRequest.setFlowTplId(flowTplId);
        queryRequest.setDataStatus(dataStatus);
        queryRequest.setNodeGroupId(nodeGroupId);
        queryRequest.setPrimitive(primitive);
        return success(nodeDefinitionService.pageNodeInfos(queryRequest));
    }


    @GetMapping("list/online")
    @Operation(summary = "list online nodes")
    public R<List<NodeDefinition>> listOnlineNodeDefinitions(
            @Parameter(name = "primitive") @RequestParam(required = false, name = "primitive") NodePrimitive primitive,
            @Parameter(name = "tplFlowCode") @RequestParam(required = false, name = "tplFlowCode",defaultValue = "EVENTS") String tplFlowCode
            ) {
        return success(nodeDefinitionService.listOnlineNodes(primitive,tplFlowCode));
    }

    /**
     * create node info
     *
     * @param nodeInfo info
     * @return AppInfo
     */
    @OperationLog(operateType = OperateType.CREATE, primaryField = "code")
    @PostMapping("info")
    @Operation(summary = "create node definition info")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    public R<NodeInfo> createNodeInfo(@Parameter(name = "node info", required = true) @Validated @RequestBody NodeInfo nodeInfo) {
        return success(nodeDefinitionService.createNodeInfo(nodeInfo));
    }

    /**
     * update node info
     *
     * @param nodeInfo node info
     * @return AppInfo
     */
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "nodeId")
    @PutMapping("info")
    @Operation(summary = "update node definition info")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.save.base:更新基本信息}")
    public R<NodeInfo> updateNodeInfo(@Parameter(name = "node info", required = true) @Validated @RequestBody NodeInfo nodeInfo) {
        return success(nodeDefinitionService.updateNodeInfo(nodeInfo));
    }

    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("group")
    @Operation(summary = "update node definition group")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update.group:变更分组}")
    public R<NodeInfo> updateNodeGroup(
            @Parameter(name = "nodeId", required = true) @RequestParam Integer nodeId,
            @Parameter(name = "nodeGroupId", required = true) @RequestParam Integer nodeGroupId) {
        return success(nodeDefinitionService.updateNodeGroup(nodeId,nodeGroupId));
    }



    /**
     * update node definition
     *
     * @param nodeDefinition node definition info
     * @return NodeDefinition
     */
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "nodeId")
    @PutMapping
    @Operation(summary = "update node definition")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.save}")
    public R<NodeDefinition> updateNodeDefinition(@Parameter(name = "node definition", required = true) @Validated @RequestBody NodeDefinition nodeDefinition) {
        return success(nodeDefinitionService.updateNodeDefinition(nodeDefinition));
    }

    /**
     * node definition detail info
     *
     * @param nodeId
     * @return NodeDefinition
     */
    @GetMapping("/{nodeId}")
    @Operation(summary = "node definition detail info")
    public R<NodeDefinition> getNodeDefinitionById(@Parameter(name = "nodeId", required = true) @PathVariable Integer nodeId) {

        return success(nodeDefinitionService.getNodeDefinitionById(nodeId));
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
    public R<Boolean> updateNodeStatus(
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
    public R<Boolean> deleteNode(
            @Parameter(name = "nodeId", required = true) @PathVariable Integer nodeId
    ) {
        return success(nodeDefinitionService.deleteNodeDefinition(nodeId));
    }

    @GetMapping("icons")
    @Operation(summary = "node icons")
    public R<Map<String, String>> getNodeIcon() {
        return success(nodeDefinitionService.getNodeIcons());
    }

    @GetMapping("primitives")
    @Operation(summary = "node primitives")
    public R<List<SelectItem>> listPrimitives() {
        return success(NodePrimitive.primitiveSelectItems());
    }

}
