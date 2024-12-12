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

import io.innospots.base.enums.DataStatus;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.base.data.request.FormQuery;
import io.innospots.libra.kernel.module.system.model.user.UserForm;
import io.innospots.libra.kernel.module.system.model.user.UserPassword;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import io.innospots.libra.kernel.module.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * user api
 *
 * @author chenc
 * @date 2021/3/19 22:10
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "user")
@ModuleMenu(menuKey = "libra-user")
@Tag(name = "System User")
public class UserController extends BaseController {

    private final UserService userService;
    private final UserOperator userOperator;

    public UserController(UserService userService, UserOperator userOperator) {
        this.userService = userService;
        this.userOperator = userOperator;
    }

    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "userId")
    @Operation(description = "create user")
    public R<UserInfo> createUser(@Parameter(name = "user", required = true) @Validated @RequestBody UserForm user) {

        UserInfo userInfo = userService.createUser(user);
        return success(userInfo);
    }

    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @OperationLog(primaryField = "userId", operateType = OperateType.UPDATE)
    @Operation(description = "update user")
    public R<Boolean> updateUser(@Parameter(name = "user", required = true) @Validated @RequestBody UserForm user) {

        Boolean update = userService.updateUser(user);
        return success(update);
    }

    @DeleteMapping("{userId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(idParamPosition = 0, operateType = OperateType.DELETE)
    @Operation(description = "delete user")
    public R<Boolean> deleteUser(@Parameter(name = "userId", required = true) @PathVariable Integer userId) {

        Boolean delete = userService.deleteUser(userId);
        return success(delete);
    }

    @GetMapping("{userId}")
    @Operation(description = "view user")
    public R<UserInfo> getUser(@Parameter(name = "userId", required = true) @PathVariable Integer userId) {

        UserInfo view = userService.getUser(userId);
        return success(view);
    }

    @GetMapping("page")
    @Operation(description = "query user")
    public R<PageBody<UserInfo>> pageUsers(FormQuery request) {
        PageBody<UserInfo> pageModel = userService.pageUsers(request);
        return success(pageModel);
    }

    @OperationLog(operateType = OperateType.UPLOAD)
    @PostMapping(value = "avatar")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${setting.account.button.header}")
    @Operation(description = "upload avatar")
    public R<String> uploadAvatar(@Parameter(name = "image", required = true) @RequestParam("image") MultipartFile uploadFile) {

        return success(userService.uploadAvatar(uploadFile));
    }

    @PutMapping("password")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${setting.password.button.update}")
    @OperationLog(primaryField = "userId", operateType = OperateType.UPDATE)
    @Operation(description = "change user password")
    public R<Boolean> changePassword(@Parameter(name = "user password", required = true) @Valid @RequestBody UserPassword userPassword) {

        Boolean update = userOperator.changePassword(userPassword);
        return success(update);
    }


    @PutMapping("{userId}/status/{userStatus}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${member.slip.status.button.update}")
    @OperationLog(idParamPosition = 0, operateType = OperateType.UPDATE_STATUS)
    @Operation(description = "enable/disabled user")
    public R<Boolean> updateUserStatus(@Parameter(required = true, name = "userId") @PathVariable Integer userId,
                                       @Parameter(required = true, name = "userStatus") @PathVariable DataStatus userStatus) {

        Boolean update = userOperator.updateUserStatus(userId, userStatus);
        return success(update);
    }

    @GetMapping("related-user")
    @Operation(description = "get related user")
    public R<List<Integer>> getRelatedUser() {
        return success(userService.getRelatedUser());
    }

    @GetMapping("admin")
    @Operation(description = "whether the current user is admin")
    public R<Boolean> isAdmin() {
        return success(userService.currentUserAdminRole());
    }
}