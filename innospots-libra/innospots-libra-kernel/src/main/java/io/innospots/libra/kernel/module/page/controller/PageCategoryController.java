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

package io.innospots.libra.kernel.module.page.controller;


import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.kernel.module.page.operator.PageCategoryOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author shengyong.huang
 * @date 2020-11-03
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "page/category")
@ModuleMenu(menuKey = "libra-page", uri = "/page")
//@ModuleMenu(parent = "libra-page", menuKey = "libra-page-category")
@Tag(name = "Page Category")
public class PageCategoryController extends BaseController {

    @Autowired
    private PageCategoryOperator pageCategoryOperator;

    @OperationLog(operateType = OperateType.CREATE, idParamPosition = 0)
    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${page.category.add.title}")
    @Operation(summary = "create page category")
    public InnospotResponse<BaseCategory> createCategory(@Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {

        BaseCategory save = pageCategoryOperator.createCategory(categoryName, CategoryType.PAGE);
        return success(save);
    }


    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("{categoryId}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${page.category.edit.title}")
    @Operation(summary = "update page category")
    public InnospotResponse<Boolean> updateCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId,
                                                    @Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        Boolean update = pageCategoryOperator.updateCategory(categoryId, categoryName, CategoryType.PAGE);
        return success(update);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{categoryId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.category.delete.title}")
    @Operation(summary = "delete page category")
    public InnospotResponse<Boolean> deleteCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId) {
        return success(pageCategoryOperator.deleteCategory(categoryId));
    }


    @GetMapping("page")
    @Operation(summary = "page category list")
    public InnospotResponse<List<BaseCategory>> listCategories() {
        List<BaseCategory> list = pageCategoryOperator.listCategories();
        return success(list);

    }
}
