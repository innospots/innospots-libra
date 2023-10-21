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

package io.innospots.connector.schema.controller;

import io.innospots.base.exception.ValidatorException;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.connector.schema.operator.SchemaCategoryOperator;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.base.category.BaseCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * Dataset schema category
 *
 * @author Alfred
 * @date 2022/1/27
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "apps/schema/category")
@ModuleMenu(menuKey = "libra-apps-dataset")
@Tag(name = "Schema Registry Category")
public class SchemaCategoryController {

    private final SchemaCategoryOperator schemaCategoryOperator;

    public SchemaCategoryController(
            SchemaCategoryOperator schemaCategoryOperator) {
        this.schemaCategoryOperator = schemaCategoryOperator;
    }

    @PostMapping
    @Operation(summary = "create dataset category")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${meta.view.category.button.create}")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "categoryId")
    public InnospotResponse<BaseCategory> createDatasetCategory(
            @Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        check(categoryName);
        BaseCategory category = schemaCategoryOperator.createCategory(categoryName, CategoryType.DATA_SET);
        return success(category);
    }

    @PutMapping("{categoryId}")
    @Operation(summary = "update dataset category")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${meta.view.category.button.update}")
    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    public InnospotResponse<Boolean> updateDatasetCategory(
            @Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId,
            @Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        check(categoryName);
        Boolean update = schemaCategoryOperator.updateCategory(categoryId, categoryName, CategoryType.DATA_SET);
        return success(update);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{categoryId}")
    @Operation(summary = "delete dataset category")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${meta.view.category.button.delete}")
    public InnospotResponse<Boolean> deleteDatasetCategory(
            @Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId) {
        return success(schemaCategoryOperator.deleteCategory(categoryId));
    }

    @GetMapping("list")
    @Operation(summary = "list dataset categories")
    public InnospotResponse<List<BaseCategory>> listDatasetCategories() {
        List<BaseCategory> list = schemaCategoryOperator.listCategories();
        return success(list);
    }

    private void check(String categoryName) {
        if (StringUtils.isBlank(categoryName) || categoryName.length() > 16) {
            throw ValidatorException.buildException(this.getClass(), ResponseCode.PARAM_INVALID, "category name length max 16 and must not be null");
        }
    }

}
