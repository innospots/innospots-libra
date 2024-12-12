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

import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.console.model.flow.WorkflowStatistics;
import io.innospots.workflow.console.service.WorkflowExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author jegy
 * @date 2022-10-08
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/stat")
@ModuleMenu(menuKey = "workflow-management", uri = "/workflow/preview")
@Tag(name = "Workflow Statistics")
public class WorkflowStatController extends BaseController {

    private final WorkflowExecutionService workflowExecutionService;

    public WorkflowStatController(WorkflowExecutionService workflowExecutionService) {
        this.workflowExecutionService = workflowExecutionService;
    }

    @GetMapping("{workflowInstanceId}")
    @Operation(description = "get statistics")
    public R<WorkflowStatistics> getWorkflowStat(@Parameter(name = "workflowInstanceId", required = true) @PathVariable Long workflowInstanceId) {
        WorkflowStatistics statistics = workflowExecutionService.getWorkflowStat(workflowInstanceId);
        return success(statistics);
    }
}
