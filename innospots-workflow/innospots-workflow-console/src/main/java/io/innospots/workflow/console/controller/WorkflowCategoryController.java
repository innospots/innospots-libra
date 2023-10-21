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

import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.workflow.console.operator.WorkflowCategoryOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
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
    @Operation(summary = "create workflow category")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${page.category.add.title}")
    public InnospotResponse<BaseCategory> createCategory(@Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        BaseCategory category = workflowCategoryOperator.createCategory(categoryName, CategoryType.WORKFLOW);
        return success(category);
    }

    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("{categoryId}")
    @Operation(summary = "update strategy category")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${page.category.edit.title}")
    public InnospotResponse<Boolean> updateCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId,
                                                    @Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        Boolean update = workflowCategoryOperator.updateCategory(categoryId, categoryName, CategoryType.WORKFLOW);
        return success(update);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{categoryId}")
    @Operation(summary = "delete strategy category")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.category.delete.title}")
    public InnospotResponse<Boolean> deleteCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId) {
        return success(workflowCategoryOperator.deleteCategory(categoryId));
    }


    @GetMapping
    @Operation(summary = "strategy category list", description = "param：0-strategy category has no value 1-it has value")
    public InnospotResponse<List<BaseCategory>> listCategories(Boolean hasNumber) {
        List<BaseCategory> list = workflowCategoryOperator.list(hasNumber);
        return success(list);

    }

    @GetMapping("check/{categoryName}")
    @Operation(summary = "check name duplicate", description = "return: true = duplicate,false = not duplicate")
    public InnospotResponse<Boolean> checkNameExist(@Parameter(required = true, name = "categoryName") @PathVariable String categoryName) {
        return success(workflowCategoryOperator.checkNameExist(categoryName, CategoryType.WORKFLOW));
    }

}