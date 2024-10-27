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

import io.innospots.base.enums.DataStatus;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.page.model.Page;
import io.innospots.libra.kernel.module.page.model.PageDetail;
import io.innospots.libra.kernel.module.page.operator.PageOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;
import static io.innospots.libra.kernel.module.page.enums.PageOperationType.PUBLISH;
import static io.innospots.libra.kernel.module.page.enums.PageOperationType.SAVE;

/**
 * @author Smars
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "page")
@ModuleMenu(menuKey = "libra-page")
@Tag(name = "Page")
public class PageController extends BaseController {

    @Autowired
    private PageOperator pageOperator;

    @OperationLog(operateType = OperateType.CREATE, primaryField = "id")
    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(summary = "save page")
    public R<PageDetail> createOrUpdate(
            @Parameter(name = "pageDetail", required = true) @Validated @RequestBody PageDetail pageDetail,
            BindingResult bindingResult) {
        PageDetail result = pageOperator.createOrUpdate(pageDetail, SAVE);
        return success(result);
    }


    @OperationLog(operateType = OperateType.PUBLISH, primaryField = "id")
    @PostMapping("publish")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.publish}")
    @Operation(summary = "publish page")
    public R<PageDetail> publishPage(
            @Parameter(name = "pageDetail", required = true) @Validated @RequestBody PageDetail pageDetail,
            BindingResult bindingResult) {
        PageDetail result = pageOperator.createOrUpdate(pageDetail, PUBLISH);
        return success(result);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{id}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(summary = "delete page")
    public R<Boolean> deletePage(@Parameter(name = "id", required = true) @PathVariable Integer id) {
        boolean delete = pageOperator.deletePage(id);
        return success(delete);
    }


    @GetMapping("{id}")
    @Operation(summary = "page detail")
    public R<PageDetail> getPageDetail(@Parameter(name = "id", required = true) @PathVariable Integer id) {
        PageDetail pageDetail = pageOperator.getPageDetail(id);
        return success(pageDetail);
    }

    // TODO 待拆分page和list接口
    @GetMapping("page")
    @Operation(summary = "page list")
    public R<PageBody<Page>> pagePages(@Parameter(name = "categoryId") @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                       @Parameter(name = "queryCode") @RequestParam(value = "queryCode", required = false) String queryCode,
                                       @Parameter(name = "page") @RequestParam("page") int page,
                                       @Parameter(name = "size") @RequestParam("size") int size) {
        PageBody<Page> list = pageOperator.pagePages(categoryId, queryCode, page, size);
        return success(list);
    }

    @OperationLog(operateType = OperateType.UPDATE_STATUS, idParamPosition = 0)
    @PutMapping("{id}/status/{status}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.status}")
    @Operation(summary = "update status", description = "page enable disable, ONLINE | OFFLINE")
    public R<Boolean> updateStatus(@Parameter(name = "id", required = true) @PathVariable Integer id,
                                   @Parameter(name = "status", required = true) @PathVariable DataStatus status) {
        boolean updateStatus = pageOperator.updateStatus(id, status);
        return success(updateStatus);
    }

    @OperationLog(operateType = OperateType.RECYCLE, idParamPosition = 0)
    @PutMapping("{id}/recycle")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${page.category.recycle_bin}")
    @Operation(summary = "recycle page")
    public R<Boolean> recyclePage(
            @Parameter(name = "id", required = true) @PathVariable Integer id) {
        return success(pageOperator.recyclePage(id));
    }

}
