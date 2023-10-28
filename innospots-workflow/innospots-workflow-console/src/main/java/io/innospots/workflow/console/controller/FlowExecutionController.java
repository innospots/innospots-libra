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

package io.innospots.workflow.console.controller;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.core.execution.ExecutionResource;
import io.innospots.workflow.core.execution.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.reader.FlowExecutionReader;
import io.innospots.workflow.core.utils.WorkflowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/8/31
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/flow-execution")
@ModuleMenu(menuKey = "workflow-management")
@Tag(name = "Workflow Execution")
public class FlowExecutionController extends BaseController {

    private final FlowExecutionReader flowExecutionReader;

    public FlowExecutionController(FlowExecutionReader flowExecutionReader) {
        this.flowExecutionReader = flowExecutionReader;
    }

    @GetMapping("page/workflow-instance/{workflowInstanceId}")
    @Operation(summary = "page flow executions using workflowInstanceId")
    public InnospotResponse<PageBody<FlowExecutionBase>> pageFlowExecutions(
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId,
            @Parameter(name = "page") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(name = "size") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(name = "statuses") @RequestParam(required = false) List<String> statuses,
            @Parameter(name = "startTime") @RequestParam(required = false) String startTime,
            @Parameter(name = "endTime") @RequestParam(required = false) String endTime,
            @Parameter(name = "revision") @RequestParam(required = false) Integer revision
    ) {
        if (page > 0) {
            page --;
        }
        return InnospotResponse.success(flowExecutionReader.pageFlowExecutions(workflowInstanceId, revision,
                statuses, startTime, endTime, page, size));
    }

    @GetMapping("result-code")
    @Operation(summary = "get resultCode")
    public InnospotResponse<List<String>> getResultCode() {
        List<String> resultCodes = new ArrayList<>();
        resultCodes.add("COMPLETE");
        resultCodes.add("FAILED");
        return success(resultCodes);
    }

    @GetMapping("latest/workflow-instance/{workflowInstanceId}")
    @Operation(summary = "find latest flow execution using workflowInstanceId")
    public InnospotResponse<FlowExecutionBase> findLatestFlowExecution(
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId,
            @Parameter(name = "revision") @RequestParam(required = false, defaultValue = "0") Integer revision
    ) {
        return InnospotResponse.success(flowExecutionReader.findLatestFlowExecution(workflowInstanceId, revision));
    }

    @GetMapping("id/{flowExecutionId}")
    @Operation(summary = "get flow execution by flowExecutionId")
    public InnospotResponse<FlowExecutionBase> getFlowExecution(
            @Parameter(name = "flowExecutionId") @PathVariable String flowExecutionId
    ) {
        return InnospotResponse.success(flowExecutionReader.getFlowExecutionById(flowExecutionId));
    }

    @GetMapping(value = "resources")
    @ResponseBody
    public ResponseEntity resource(@RequestParam("resourceId") String resourceId) throws IOException {
        String uri = WorkflowUtils.encryptor.decode(resourceId);
        File resFile = new File(uri);
        log.debug("resource file:{}",uri);
        ExecutionResource executionResource = ExecutionResource.buildResource(resFile,true);
        InputStreamSource resource = executionResource.buildInputStreamSource();
        String[] ss = executionResource.getMimeType().split("/");
        return ResponseEntity.ok().contentType(new MediaType(ss[0], ss[1])).body(resource);
    }

}
