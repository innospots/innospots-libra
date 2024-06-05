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

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.core.debug.DebugPayload;
import io.innospots.workflow.core.debug.FlowNodeDebuggerBuilder;
import io.innospots.workflow.core.execution.model.ExecutionResource;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @date 2023/4/23
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "flow-node/debug")
@ModuleMenu(menuKey = "node-management")
@Tag(name = "Node debug")
public class FlowNodeDebugController {


    @PostMapping("upload-file/{force}")
    @Operation(summary = "upload debug file")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.upload}")
    public InnospotsResponse<ExecutionResource> uploadFile
            (@Parameter(name = "files", required = true) @RequestParam("files") MultipartFile uploadFile,   @PathVariable boolean force){

        return InnospotsResponse.success(
                FlowNodeDebuggerBuilder.build("nodeDebugger").updateTestFile(uploadFile,force));
    }

    @OperationLog(operateType = OperateType.EXECUTE)
    @PostMapping("execute")
    @Operation(summary = "debug app node")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.execute}")
    public InnospotsResponse<NodeExecutionDisplay> execute(
            @RequestBody DebugPayload debugPayload){

        return InnospotsResponse.success(
                FlowNodeDebuggerBuilder.build("nodeDebugger").execute(debugPayload));
    }

}
