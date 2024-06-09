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

package io.innospots.workflow.console.controller.execution;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.execution.reader.NodeExecutionReader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/8/31
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/node-execution")
@ModuleMenu(menuKey = "workflow-management")
@Tag(name = "Workflow Node Execution")
public class NodeExecutionController extends BaseController {


    private NodeExecutionReader nodeExecutionReader;

    public NodeExecutionController(NodeExecutionReader nodeExecutionReader) {
        this.nodeExecutionReader = nodeExecutionReader;
    }

    @GetMapping("list/latest/workflow-instance/{workflowInstanceId}")
    @Operation(summary = "latest node execution using workflowInstanceId")
    public InnospotsResponse<Map<String, NodeExecutionDisplay>> latestNodeExecutionsByFlowInstance(
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId,
            @Parameter(name = "revision") @RequestParam(required = false, defaultValue = "0") Integer revision
    ) {

        return InnospotsResponse.success(nodeExecutionReader.readLatestNodeExecutionByFlowInstanceId(workflowInstanceId, revision, Collections.emptyList()));
    }

    @GetMapping("list/workflow-execution/{flowExecutionId}")
    @Operation(summary = "list all the node executions using flowExecutionId")
    public InnospotsResponse<Map<String, NodeExecutionDisplay>> listNodeExecutionsByFlowExecution(
            @Parameter(name = "flowExecutionId") @PathVariable String flowExecutionId) {

        return InnospotsResponse.success(nodeExecutionReader.readExecutionByFlowExecutionId(flowExecutionId, Collections.emptyList(), true));
    }


    @GetMapping("find/node-execution/{nodeExecutionId}")
    @Operation(summary = "find the node executions using primary id, nodeExecutionId")
    public InnospotsResponse<NodeExecutionDisplay> findNodeExecution(
            @Parameter(name = "nodeExecutionId") @PathVariable String nodeExecutionId,
            @Parameter(name = "page") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(name = "size") @RequestParam(required = false, defaultValue = "50") Integer size
    ) {
        return InnospotsResponse.success(nodeExecutionReader.findNodeExecution(nodeExecutionId, page, size));
    }

}
