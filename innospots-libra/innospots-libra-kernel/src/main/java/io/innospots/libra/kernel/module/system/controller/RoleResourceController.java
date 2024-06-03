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

package io.innospots.libra.kernel.module.system.controller;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.system.model.role.resource.RoleResourceInfo;
import io.innospots.libra.kernel.module.system.model.role.resource.RoleResourceRequest;
import io.innospots.libra.kernel.module.system.operator.RoleResourceOperator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.log.OperateType.UPDATE;
import static io.innospots.libra.base.menu.ItemType.BUTTON;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/30
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "rbac")
@ModuleMenu(menuKey = "libra-rbac")
@Tag(name = "RBAC")
public class RoleResourceController extends BaseController {

    private final RoleResourceOperator roleResourceOperator;


    public RoleResourceController(RoleResourceOperator roleResourceOperator) {
        this.roleResourceOperator = roleResourceOperator;
    }


    @GetMapping("menu-permissions")
    public InnospotsResponse<Map<String, List<RoleResourceInfo>>> getMenuPermissions() {
        List<RoleResourceInfo> roleResourceInfos = roleResourceOperator.listMenuAuthorities(null);
        return success(roleResourceInfos.stream().collect(groupingBy(RoleResourceInfo::getItemKey)));
    }

    @GetMapping("menu-permissions/{itemKey}")
    public InnospotsResponse<List<RoleResourceInfo>> getMenuPermissionsByItemKey(@PathVariable String itemKey) {
        List<RoleResourceInfo> roleResourceInfos = roleResourceOperator.listMenuAuthorities(itemKey);
        return success(roleResourceInfos);
    }

    @GetMapping("operate-permissions/{roleId}")
    public InnospotsResponse<Map<String, List<RoleResourceInfo>>> getOperateRolePermissions(
            @Parameter(name = "roleId") @PathVariable Integer roleId
    ) {
        List<RoleResourceInfo> roleResourceInfos = roleResourceOperator.listOperateAuthorities(roleId);
        return success(roleResourceInfos.stream().collect(groupingBy(RoleResourceInfo::getItemKey)));
    }

    @PostMapping("menu-permissions")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${authority.list.menu.button.update}")
    @OperationLog(operateType = UPDATE)
    public InnospotsResponse<Boolean> addMenuRolePermissions(
            @RequestBody RoleResourceRequest roleResourceRequest
    ) {

        Boolean result = roleResourceOperator.saveRoleResourceAuthority(roleResourceRequest);
        return success(result);
    }

    @PostMapping("operate-permissions")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${authority.list.app.button.update}")
    @OperationLog(operateType = UPDATE)
    public InnospotsResponse<Boolean> addOperateRolePermissions(
            @RequestBody RoleResourceRequest roleResourceRequest
    ) {
        Boolean result = roleResourceOperator.saveRoleResourceAuthority(roleResourceRequest);
        return success(result);
    }
}