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

import io.innospots.base.model.response.R;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.core.debug.FlowNodeDebugger;
import io.innospots.workflow.core.execution.model.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;


/**
 * @author Smars
 * @date 2021/5/10
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/debug")
@ModuleMenu(menuKey = "workflow-management")
@Tag(name = "Workflow Debug",description = "debug execute the workflow in the canvas")
public class FlowExecuteDebugController {

    private FlowNodeDebugger flowNodeDebugger;

    public FlowExecuteDebugController(FlowNodeDebugger flowNodeDebugger) {
        this.flowNodeDebugger = flowNodeDebugger;
    }

    @OperationLog(operateType = OperateType.EXECUTE, idParamPosition = 0)
    @PostMapping("workflow-instance/{workflowInstanceId}")
    @Operation(summary = "debug execute workflow")
    @ResourceItemOperation(key = "workflow-builder-execute")
    public R<Map<String, NodeExecutionDisplay>> executeNode(@Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId,
                                                            @Parameter(name = "nodeKey") @RequestParam(required = false) String nodeKey,
                                                            @Parameter(name = "inputs") @RequestBody(required = false) List<Map<String, Object>> inputs) {
        if (inputs == null) {
            inputs = new ArrayList<>();
        }
        return success(flowNodeDebugger.execute(workflowInstanceId, nodeKey, inputs));
    }


    @OperationLog(operateType = OperateType.EXECUTE, idParamPosition = 0)
    @PostMapping("workflow-instance/{workflowInstanceId}/node-instance/output")
    @Operation(summary = "node execution output display")
    @ResourceItemOperation(key = "workflow-builder-execute")
    public R<Map<String, NodeExecutionDisplay>> nodeExecutionOutput(@Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId,
                                                                    @Parameter(name = "nodeKeys") @RequestBody(required = false) List<String> nodeKeys) {
        nodeKeys = new ArrayList<>();
        return success(flowNodeDebugger.readNodeExecutions(workflowInstanceId, nodeKeys));
    }

    @GetMapping("current/{workflowInstanceId}")
    @Operation(summary = "current workflow execution")
    public R<FlowExecutionBase> currentFlowExecution(@PathVariable Long workflowInstanceId) {
        return success(flowNodeDebugger.currentExecuting(workflowInstanceId));
    }

    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PostMapping("stop/flow-execution/{workflowExecutionId}")
    @Operation(summary = "stop workflow execution")
    @ResourceItemOperation(key = "workflow-builder-execute")
    public R<FlowExecutionBase> currentFlowExecution(@PathVariable String workflowExecutionId) {
        return success(flowNodeDebugger.stop(workflowExecutionId));
    }

    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PostMapping("stop/workflow/{flowKey}")
    @Operation(summary = "stop current executing workflow by key")
    @ResourceItemOperation(key = "workflow-builder-execute")
    public R<FlowExecutionBase> stopWorkflowByKey(@PathVariable String flowKey) {
        return success(flowNodeDebugger.stop(flowKey));
    }



}
