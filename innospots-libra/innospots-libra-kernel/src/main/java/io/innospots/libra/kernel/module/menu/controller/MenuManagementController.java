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

package io.innospots.libra.kernel.module.menu.controller;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ItemType;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.menu.model.MenuOrders;
import io.innospots.libra.kernel.module.menu.model.MenuResourceItem;
import io.innospots.libra.kernel.module.menu.model.NewMenuItem;
import io.innospots.libra.kernel.module.menu.operator.MenuManagementOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * menu api
 *
 * @author chenc
 * @date 2021/6/20 15:12
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "menu")
@ModuleMenu(menuKey = "libra-menu")
@Tag(name = "Menu")
public class MenuManagementController extends BaseController {

    private MenuManagementOperator menuManagementOperator;

    public MenuManagementController(MenuManagementOperator menuManagementOperator) {
        this.menuManagementOperator = menuManagementOperator;
    }

    @GetMapping("list")
    @Operation(summary = "menu tree list")
    public InnospotsResponse<List<MenuResourceItem>> listMenuItems(@RequestParam(value = "queryInput", required = false) String queryInput) {
        return success(menuManagementOperator.listMenuItems(queryInput));
    }

    @GetMapping("list/{itemType}")
    @Operation(summary = "list menu items which the itemType is category")
    public InnospotsResponse<List<MenuResourceItem>> listDirectoryMenuTree(@Parameter(name = "itemType") @PathVariable ItemType itemType) {
        return success(menuManagementOperator.listMenuItemsByItemType(itemType));
    }

    @OperationLog(operateType = OperateType.UPDATE_STATUS, idParamPosition = 0)
    @PutMapping("menu-item/{resourceId}/status/{status}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.status}")
    @Operation(summary = "update menu item status")
    public InnospotsResponse<Boolean> updateStatus(@Parameter(name = "resourceId") @PathVariable Integer resourceId,
                                                   @Parameter(name = "status") @PathVariable Boolean status) {
        return success(menuManagementOperator.updateStatus(resourceId, status));
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("menu-item/{resourceId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(summary = "delete menu item")
    public InnospotsResponse<Boolean> deleteMenuById(@Parameter(name = "resourceId") @PathVariable Integer resourceId) {
        return success(menuManagementOperator.deleteMenuById(resourceId));
    }

    @OperationLog(operateType = OperateType.CREATE, idParamPosition = 0, primaryField = "itemKey")
    @PostMapping("menu-item")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(summary = "create new menu item")
    public InnospotsResponse<MenuResourceItem> createMenu(@Valid @RequestBody NewMenuItem newMenuItem) {
        return success(menuManagementOperator.createMenu(newMenuItem));
    }

    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0, primaryField = "resourceId")
    @PutMapping("menu-item")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(summary = "update menu item")
    public InnospotsResponse<Boolean> updateMenu(@RequestBody NewMenuItem newMenuItem) {
        Boolean update = menuManagementOperator.updateMenu(newMenuItem);
        return success(update);
    }


    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0, primaryField = "resourceId")
    @PutMapping("menu-item/order")
    @Operation(summary = "order menu items")
    public InnospotsResponse<List<MenuResourceItem>> orderMenuItems(@RequestBody MenuOrders menuOrders) {
        return success(menuManagementOperator.orderItems(menuOrders));
    }

}