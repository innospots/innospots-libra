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
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.console.service.WorkflowBuilderService;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @date 2021/4/19
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/builder")
@ModuleMenu(menuKey = "workflow-management", uri = "/workflow/builder")
@Tag(name = "Workflow Builder")
public class WorkflowBuilderController extends BaseController {


    private WorkflowBuilderService workflowBuilderService;


    public WorkflowBuilderController(WorkflowBuilderService workflowBuilderService) {
        this.workflowBuilderService = workflowBuilderService;
    }

    /**
     * get flow instance by revision
     *
     * @param workflowInstanceId
     * @param revision
     * @param includeNodes
     * @return
     */
    @GetMapping("{workflowInstanceId}/revision/{revision}")
    @ResourceItemOperation(type = BUTTON, icon = "edit", name = "${common.button.edit}")
    @Operation(summary = "get flow instance by revision")
    public R<WorkflowBaseBody> getFlowInstanceByRevision(@PathVariable Long workflowInstanceId,
                                                         @PathVariable Integer revision,
                                                         @RequestParam(defaultValue = "true") Boolean includeNodes) {
        return success(workflowBuilderService.getFlowInstanceByRevision(workflowInstanceId,revision,includeNodes));
    }

    /**
     * get flow instance by draft
     *
     * @param workflowInstanceId
     * @return
     */
    @GetMapping("draft/{workflowInstanceId}")
    @Operation(summary = "get flow instance by draft")
    public R<WorkflowBaseBody> getFlowInstanceByDraft(@PathVariable Long workflowInstanceId) {
        return success(workflowBuilderService.getFlowInstanceByDraft(workflowInstanceId));
    }


    /**
     * get flow instance by lasted
     *
     * @param workflowInstanceId
     * @param includeNodes
     * @return
     */
    @GetMapping("lasted/{workflowInstanceId}")
    @Operation(summary = "get flow instance by lasted")
    public R<WorkflowBaseBody> getFlowInstanceByLasted(@PathVariable Long workflowInstanceId,
                                                       @RequestParam(defaultValue = "true") Boolean includeNodes) {
        return success(workflowBuilderService.getFlowInstanceByLasted(workflowInstanceId, includeNodes));
    }


    /**
     * save flow instance to cache
     *
     * @param workflowBaseBody
     * @return
     */
    @PostMapping("cache")
    @Operation(summary = "save flow instance to cache")
    @ResourceItemOperation(key = "workflow-builder-opt")
    public R<Boolean> saveCache(@Validated @RequestBody WorkflowBaseBody workflowBaseBody) {
        return success(workflowBuilderService.saveCache(workflowBaseBody));
    }


    @GetMapping("/{workflowInstanceId}/node-key/{nodeKey}/input-fields")
    @Operation(summary = "the input data fields, which current select node")
    public R<List<Map<String, Object>>> listNodeInputFields(
            @PathVariable Long workflowInstanceId,
            @PathVariable String nodeKey,
            @RequestParam(required = false) Set<String> sourceNodeKeys
    ) {
        return success(workflowBuilderService.listNodeInputFields(workflowInstanceId, nodeKey, sourceNodeKeys));
    }


    /**
     * save flow instance draft revision
     *
     * @return
     */
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "workflowInstanceId")
    @PutMapping("draft")
    @Operation(summary = "save flow instance draft revision")
    @ResourceItemOperation(key = "workflow-builder-opt", type = BUTTON, icon = "save", name = "${common.button.save}")
    public R<WorkflowBaseBody> saveDraft(@Validated @RequestBody WorkflowBaseBody workflowBaseBody) {
        return success(workflowBuilderService.saveDraft(workflowBaseBody));
    }


    /**
     * publish flow instance
     *
     * @param workflowInstanceId
     * @return
     */
    @OperationLog(operateType = OperateType.PUBLISH, idParamPosition = 0)
    @PostMapping("publish/{workflowInstanceId}")
    @Operation(summary = "publish flow instance")
    @ResourceItemOperation(type = BUTTON, icon = "save", name = "${common.button.publish}")
    public R<Boolean> publish(@PathVariable Long workflowInstanceId,
                              @RequestParam(defaultValue = "") String description) {

        return success(workflowBuilderService.publish(workflowInstanceId, description));
    }

}
