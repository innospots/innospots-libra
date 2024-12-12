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
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.workflow.core.enums.WorkflowType;
import io.innospots.workflow.console.operator.WorkflowCategoryOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author chenc
 * @date 2022/2/19
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/category")
@ModuleMenu(menuKey = "workflow-management")
//@ModuleMenu(menuKey = "workflow-category",parent = "workflow-management")
@Tag(name = "Workflow Category")
public class WorkflowCategoryController extends BaseController {

    private final WorkflowCategoryOperator workflowCategoryOperator;


    public WorkflowCategoryController(WorkflowCategoryOperator workflowCategoryOperator) {
        this.workflowCategoryOperator = workflowCategoryOperator;
    }

    @OperationLog(operateType = OperateType.CREATE, idParamPosition = 0)
    @PostMapping
    @Operation(description = "create workflow category")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${page.category.add.title}")
    public R<BaseCategory> createCategory(
            @Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName,
            @Parameter(required = false, name = "workflowType") @RequestParam(name = "workflowType", required = false, defaultValue = "EVENT") WorkflowType workflowType
    ) {
        BaseCategory category = workflowCategoryOperator.createCategory(categoryName, workflowType);
        return success(category);
    }

    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("{categoryId}")
    @Operation(description = "update strategy category")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${page.category.edit.title}")
    public R<Boolean> updateCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId,
                                     @Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        Boolean update = workflowCategoryOperator.updateCategory(categoryId, categoryName, CategoryType.WORKFLOW);
        return success(update);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{categoryId}")
    @Operation(description = "delete workflow category")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.category.delete.title}")
    public R<Boolean> deleteCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId) {
        return success(workflowCategoryOperator.deleteCategory(categoryId));
    }


    @GetMapping
    @Operation(description = "param：0-strategy category has no value 1-it has value")
    public R<List<BaseCategory>> listCategories(
            @Parameter(required = true, name = "hasNumber") @RequestParam(value = "hasNumber",required = false,defaultValue = "true") Boolean hasNumber,
            @Parameter(required = false, name = "workflowType") @RequestParam(name = "workflowType", required = false, defaultValue = "EVENTS") WorkflowType workflowType
                                                ) {
        List<BaseCategory> list = workflowCategoryOperator.list(hasNumber,workflowType);
        return success(list);

    }

    @GetMapping("check/{categoryName}")
    @Operation(description = "return: true = duplicate,false = not duplicate")
    public R<Boolean> checkNameExist(
            @Parameter(required = true, name = "categoryName") @PathVariable String categoryName,
            @Parameter(required = false, name = "workflowType") @RequestParam(name = "workflowType", required = false, defaultValue = "EVENTS") WorkflowType workflowType
            ) {
        return success(workflowCategoryOperator.checkNameExist(categoryName,workflowType));
    }

}