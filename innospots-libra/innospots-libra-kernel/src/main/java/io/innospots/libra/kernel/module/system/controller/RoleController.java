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

import io.innospots.base.model.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.kernel.module.system.model.role.RoleInfo;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.base.model.QueryRequest;
import io.innospots.libra.kernel.module.system.model.role.UserRole;
import io.innospots.libra.kernel.module.system.operator.RoleOperator;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import io.innospots.libra.kernel.module.system.operator.UserRoleOperator;
import io.innospots.libra.kernel.module.system.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;
import static java.util.Collections.singletonList;

/**
 * role api
 *
 * @author chenc
 * @date 2021/2/9 20:24
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "role")
@ModuleMenu(menuKey = "libra-role")
@Tag(name = "System Role")
public class RoleController extends BaseController {

    private final UserOperator userOperator;
    private final RoleOperator roleOperator;
    private final UserRoleService userRoleService;
    private final UserRoleOperator userRoleOperator;

    public RoleController(UserOperator userOperator, RoleOperator roleOperator, UserRoleService userRoleService,
                          UserRoleOperator userRoleOperator) {
        this.userOperator = userOperator;
        this.roleOperator = roleOperator;
        this.userRoleService = userRoleService;
        this.userRoleOperator = userRoleOperator;
    }

    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "roleId")
    @Operation(summary = "create role")
    public InnospotResponse<RoleInfo> createRole(@Parameter(name = "role", required = true) @Valid @RequestBody RoleInfo role) {

        RoleInfo save = roleOperator.createRole(role);
        return success(save);
    }

    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "roleId")
    @Operation(summary = "update role")
    public InnospotResponse<Boolean> updateRole(@Parameter(name = "role", required = true) @Valid @RequestBody RoleInfo role) {

        Boolean update = roleOperator.updateRole(role);
        return success(update);
    }


    @DeleteMapping("{roleId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @Operation(summary = "delete role", description = "delete role")
    public InnospotResponse<Boolean> deleteRole(@Parameter(name = "roleId", required = true) @PathVariable Integer roleId) {

        Boolean delete = userRoleService.deleteRole(roleId);
        return success(delete);
    }

    @GetMapping("{roleId}")
    @Operation(summary = "get role info")
    public InnospotResponse<RoleInfo> getRole(@Parameter(name = "roleId", required = true) @PathVariable Integer roleId) {

        RoleInfo view = roleOperator.getRole(roleId);
        return success(view);
    }

    @GetMapping("page")
    @Operation(summary = "role list")
    public InnospotResponse<PageBody<RoleInfo>> pageRoles(QueryRequest request) {
        PageBody<RoleInfo> pageModel = userRoleService.pageRoles(request);
        return success(pageModel);
    }

    @GetMapping("{roleId}/users")
    @Operation(summary = "role user list")
    public InnospotResponse<List<SimpleUser>> listRoleUsers(@PathVariable Integer roleId) {
        List<SimpleUser> users = userRoleService.listRoleUsers(roleId);
        return success(users);
    }

    @PostMapping("{roleId}/user")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}", label = "${role.list.button.add_member}")
    @OperationLog(operateType = OperateType.ADD, idParamPosition = 0)
    @Operation(summary = "batch add user role")
    public InnospotResponse<List<SimpleUser>> addUserRole(@Parameter(name = "roleId", required = true) @PathVariable Integer roleId,
                                                          @RequestBody UserRole userRole) {
        userRoleOperator.saveUserRoles(userRole.getUserIds(), singletonList(roleId));
        return success(userOperator.listByIds(userRole.getUserIds()));
    }

    @DeleteMapping("{roleId}/user/{userId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.update}", label = "${role.list.button.delete_member}")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "roleId")
    @Operation(summary = "delete user role")
    public InnospotResponse<Boolean> deleteUserRole(@Parameter(name = "roleId", required = true) @PathVariable Integer roleId,
                                                    @Parameter(name = "userId", required = true) @PathVariable Integer userId) {
        Boolean delete = userRoleOperator.delete(userId, roleId);
        return success(delete);
    }

    @GetMapping("list-name")
    public InnospotResponse<List<String>> listUserRole() {
        List<String> roleNames = userRoleService.getUserRoles();
        return success(roleNames);
    }
}