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

import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.core.node.definition.converter.FlowNodeGroupConverter;
import io.innospots.workflow.console.operator.node.FlowTemplateOperator;
import io.innospots.workflow.core.node.definition.model.FlowTemplate;
import io.innospots.workflow.core.node.definition.model.NodeGroupBaseInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/3
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "flow-node/category")
@ModuleMenu(menuKey = "node-management")
@Tag(name = "Node Category")
public class FlowNodeCategoryController extends BaseController {

    private FlowTemplateOperator flowTemplateOperator;

    public FlowNodeCategoryController(FlowTemplateOperator flowTemplateOperator) {
        this.flowTemplateOperator = flowTemplateOperator;
    }

    @GetMapping("list")
    @Operation(summary = "flow node category list")
    public InnospotResponse<List<NodeGroupBaseInfo>> listCategories(
            @RequestParam(required = false, defaultValue = "true") boolean onlyConnector) {
        return InnospotResponse.success(
                FlowNodeGroupConverter.INSTANCE.modelToBaseList(
                        flowTemplateOperator.getTemplate(1, true, onlyConnector,false)
                                .getNodeGroups())
        );
    }

    @GetMapping("def-list/{templateCode}")
    @Operation(summary = "list flow node definition")
    public InnospotResponse<FlowTemplate> getTemplate(@PathVariable String templateCode) {
        return success(flowTemplateOperator.getTemplate(1, true));
    }

    @GetMapping("def-list")
    @Operation(summary = "list flow node definition")
    public InnospotResponse<FlowTemplate> getTemplate() {
        return success(flowTemplateOperator.getTemplate(1, true));
    }
}
