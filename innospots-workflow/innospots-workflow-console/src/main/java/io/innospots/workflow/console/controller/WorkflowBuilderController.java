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

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.core.instance.operator.WorkflowDraftOperator;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.instance.operator.WorkflowBodyOperator;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.innospots.base.model.response.InnospotsResponse.success;
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


    private WorkflowBodyOperator workFlowBodyOperator;

    private WorkflowDraftOperator workflowDraftOperator;

    private InnospotsWorkflowProperties workflowProperties;

    public WorkflowBuilderController(WorkflowBodyOperator workFlowBodyOperator, WorkflowDraftOperator workflowDraftOperator, InnospotsWorkflowProperties workflowProperties) {
        this.workFlowBodyOperator = workFlowBodyOperator;
        this.workflowDraftOperator = workflowDraftOperator;
        this.workflowProperties = workflowProperties;
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
    public InnospotsResponse<WorkflowBaseBody> getFlowInstanceByRevision(@PathVariable Long workflowInstanceId,
                                                                         @PathVariable Integer revision,
                                                                         @RequestParam(defaultValue = "true") Boolean includeNodes) {
        WorkflowBaseBody workflowBaseBody;
        if (revision == null || revision == 0) {
            workflowBaseBody = workflowDraftOperator.getDraftWorkflow(workflowInstanceId);
        } else {
            workflowBaseBody = workFlowBodyOperator.getWorkflowBody(workflowInstanceId, revision, includeNodes);
        }
        return success(workflowBaseBody);
    }

    /**
     * get flow instance by draft
     *
     * @param workflowInstanceId
     * @param includeNodes
     * @return
     */
    @GetMapping("draft/{workflowInstanceId}")
    @Operation(summary = "get flow instance by draft")
    public InnospotsResponse<WorkflowBaseBody> getFlowInstanceByDraft(@PathVariable Long workflowInstanceId,
                                                                      @RequestParam(defaultValue = "true") Boolean includeNodes) {
        return success(workflowDraftOperator.getDraftWorkflow(workflowInstanceId));
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
    public InnospotsResponse<WorkflowBaseBody> getFlowInstanceByLasted(@PathVariable Long workflowInstanceId,
                                                                       @RequestParam(defaultValue = "true") Boolean includeNodes) {
        return success(workFlowBodyOperator.getWorkflowBody(workflowInstanceId, null, includeNodes));
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
    public InnospotsResponse<Boolean> saveCache(@Validated @RequestBody WorkflowBaseBody workflowBaseBody) {
        return success(workflowDraftOperator.saveFlowInstanceToCache(workflowBaseBody));
    }

    /*
    @Deprecated
    @GetMapping("/{workflowInstanceId}/node-key/{nodeKey}/output-field")
    @Operation(summary = "从保存的工作流程实例缓存信息中获取节点的输出字段")
    public InnospotResponse<List<Map<String,Object>>> getNodeOutputFieldOfInstance(@PathVariable Long workflowInstanceId, @PathVariable String nodeKey) {
        //从缓存信息中获取每个节点的outputField列表，
        return success(workFlowBuilderOperator.getNodeOutputFieldOfInstance(workflowInstanceId,nodeKey));
    }
     */


    @GetMapping("/{workflowInstanceId}/node-key/{nodeKey}/input-fields")
    @Operation(summary = "the input data fields, which current select node")
    public InnospotsResponse<List<Map<String, Object>>> listNodeInputFields(
            @PathVariable Long workflowInstanceId,
            @PathVariable String nodeKey,
            @RequestParam(required = false) Set<String> sourceNodeKeys
    ) {
        return success(workflowDraftOperator.selectNodeInputFields(workflowInstanceId, nodeKey, sourceNodeKeys));
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
    public InnospotsResponse<WorkflowBaseBody> saveDraft(@Validated @RequestBody WorkflowBaseBody workflowBaseBody) {
        return success(workflowDraftOperator.saveDraft(workflowBaseBody));
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
    public InnospotsResponse<Boolean> publish(@PathVariable Long workflowInstanceId,
                                              @RequestParam(defaultValue = "") String description) {

        return success(workflowDraftOperator.publish(workflowInstanceId, description,workflowProperties.getWorkFlowInstanceKeepVersionAmount()));
    }

}
