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

import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.console.operator.node.FlowNodeGroupOperator;
import io.innospots.workflow.core.node.definition.model.NodeGroup;
import io.innospots.workflow.core.node.definition.model.NodeGroupBaseInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/3
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "flow-node/group")
@ModuleMenu(menuKey = "node-management")
@Tag(name = "Node Group")
public class FlowNodeGroupController extends BaseController {

    private FlowNodeGroupOperator flowNodeGroupOperator;

    public FlowNodeGroupController(FlowNodeGroupOperator flowNodeGroupOperator) {
        this.flowNodeGroupOperator = flowNodeGroupOperator;
    }

    @PostMapping
    @Operation(description = "create node group")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.category.add.title}")
    public R<NodeGroupBaseInfo> createNodeGroup(String name, String templateCode){
        return R.success(flowNodeGroupOperator.createNodeGroup(name,templateCode));
    }

    @PutMapping("re-name")
    @Operation(description = "re-name node group")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.category.edit.title}")
    public R<Boolean> reNameNodeGroup(
            @Parameter(name = "nodeGroupId") @RequestParam Integer nodeGroupId,
            @Parameter(name = "name") @RequestParam String name){
        return R.success(flowNodeGroupOperator.reNameNodeGroup(nodeGroupId,name));
    }

    @DeleteMapping("{nodeGroupId}")
    @Operation(description = "remove node group")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.category.delete.title}")
    public R<Boolean> removeNodeGroup(@PathVariable Integer nodeGroupId){
        return R.success(flowNodeGroupOperator.removeNodeGroup(nodeGroupId));
    }

    @PutMapping("swap-position")
    @Operation(description = "swap node group position")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.category.swap.title:调序}")
    public R<List<NodeGroupBaseInfo>> swapPosition(
            @Parameter(name = "targetGroupId") @RequestParam Integer targetGroupId,
            @Parameter(name = "toGroupId") @RequestParam Integer toGroupId){
        return R.success(flowNodeGroupOperator.swapPosition(targetGroupId,toGroupId));
    }


    @GetMapping("list/{templateCode}")
    @Operation(description = "flow node group list by template code")
    public R<List<NodeGroup>> listCategories(
            @Parameter(name = "templateCode") @PathVariable String templateCode,
            @Parameter(name = "includeNodes") @RequestParam(required = false, name = "includeNodes",defaultValue = "false") boolean includeNodes,
            @Parameter(name = "includeAll") @RequestParam(required = false, name = "includeAll",defaultValue = "false") boolean includeAll,
            @Parameter(name = "excludeTrigger") @RequestParam(required = false, name = "excludeTrigger",defaultValue = "false") boolean excludeTrigger
            ) {
        return R.success(
                        flowNodeGroupOperator.getGroupByFlowTplCode(templateCode, includeNodes,excludeTrigger,includeAll)
        );
    }

}
