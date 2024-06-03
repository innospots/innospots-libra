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

package io.innospots.libra.kernel.module.config.controller;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.model.system.OrganizationInfo;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.config.model.EmailServerInfo;
import io.innospots.libra.kernel.module.config.operator.SysConfigOperator;
import io.innospots.libra.kernel.module.config.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@Tag(name = "System Configuration")
@RestController
@RequestMapping(BaseController.PATH_ROOT_ADMIN + "configuration")
@ModuleMenu(menuKey = "libra-system-setting")
public class SysConfigController extends BaseController {

    private SysConfigOperator sysConfigOperator;
    private SysConfigService sysConfigService;

    public SysConfigController(SysConfigOperator sysConfigOperator, SysConfigService sysConfigService) {
        this.sysConfigOperator = sysConfigOperator;
        this.sysConfigService = sysConfigService;
    }

    @GetMapping("email")
    @Operation(summary = "get email server info")
    public InnospotsResponse<EmailServerInfo> showEmailConfigs() {
        return success(sysConfigOperator.getEmailServerInfo());
    }

    @PostMapping("email")
    @OperationLog(operateType = OperateType.UPDATE)
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${setting.email.button.save}")
    @Operation(summary = "save email server info")
    public InnospotsResponse<Boolean> saveEmailConfig(@RequestBody @Valid EmailServerInfo emailServerInfo) {

        return success(sysConfigOperator.saveEmailServerConfig(emailServerInfo) > 0);
    }

    @GetMapping("organization")
    @Operation(summary = "get organization info")
    public InnospotsResponse<OrganizationInfo> showOrganizationConfigs() {
        return success(sysConfigService.getOrganization());
    }

    @PostMapping("organization")
    @OperationLog(operateType = OperateType.UPDATE)
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${setting.organization.button.save}")
    @Operation(summary = "save organization info")
    public InnospotsResponse<Boolean> saveOrganizationConfig(@RequestBody @Valid OrganizationInfo organizationInfo) {
        return success(sysConfigService.saveOrganizationConfig(organizationInfo) > 0);
    }

    @OperationLog(operateType = OperateType.UPDATE)
    @PostMapping(value = "logo")
    @ResourceItemOperation(type = BUTTON, icon = "upload", name = "${common.button.upload}")
    @Operation(summary = "upload image")
    public InnospotsResponse<String> uploadLogo(@Parameter(name = "image", required = true) @RequestParam("image") MultipartFile uploadFile) {

        return success(sysConfigService.uploadLogo(uploadFile));
    }

    @OperationLog(operateType = OperateType.UPDATE)
    @PostMapping(value = "favicon")
    @ResourceItemOperation(type = BUTTON, icon = "upload", name = "${common.button.upload}")
    @Operation(summary = "upload image")
    public InnospotsResponse<String> uploadFavicon(@Parameter(name = "image", required = true) @RequestParam("image") MultipartFile uploadFile) {

        return success(sysConfigService.uploadFavicon(uploadFile));
    }


}
