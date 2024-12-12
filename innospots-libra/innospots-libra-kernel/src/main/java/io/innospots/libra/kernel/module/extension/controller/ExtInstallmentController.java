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

package io.innospots.libra.kernel.module.extension.controller;

import io.innospots.base.model.response.R;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.extension.model.ExtensionInstallInfo;
import io.innospots.libra.kernel.module.extension.operator.ExtInstallmentOperator;
import io.innospots.libra.kernel.module.extension.service.ExtensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * the extensions that are installed in the system
 *
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/19
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "extension/installed")
@ModuleMenu(menuKey = "libra-extension-installed")
@Tag(name = "Extension Installment")
public class ExtInstallmentController {

    private ExtensionService extensionService;
    private ExtInstallmentOperator extInstallmentOperator;


    public ExtInstallmentController(ExtInstallmentOperator extInstallmentOperator, ExtensionService extensionService) {
        this.extInstallmentOperator = extInstallmentOperator;
        this.extensionService = extensionService;
    }

    /**
     * installed extensions list
     *
     * @return
     */
    @GetMapping("list")
    @Operation(description = "extension installed list")
    public R<List<ExtensionInstallInfo>> listInstalledExtensions() {

        return success(extInstallmentOperator.installedExtensions());
    }

    /**
     * app info list of installed
     *
     * @return
     */
    @GetMapping("list/base-info")
    @Operation(description = "extension base info installed list")
    public R<List<LibraExtensionProperties>> listInstalledExtBaseInfos() {

        return success(extInstallmentOperator.installedExtBaseInfos());
    }


    @PostMapping("extension/{extKey}")
    @ResourceItemOperation(type = BUTTON, icon = "install", name = "${app.market.button.install}")
    @Operation(description = "extension install by extKey")
    @OperationLog(operateType = OperateType.INSTALL, idParamPosition = 0)
    public R<ExtensionInstallInfo> installByKey(@PathVariable String extKey) {
        return success(extensionService.install(extKey));
    }


    @PostMapping("enabled/{extKey}")
    @ResourceItemOperation(type = BUTTON, icon = "available", name = "${app.market.button.enable}")
    @Operation(description = "extension available")
    @OperationLog(operateType = OperateType.INSTALL, idParamPosition = 0)
    public R<Boolean> enabled(@PathVariable String extKey) {
        return success(extInstallmentOperator.enabled(extKey));
    }

    @PostMapping("disabled/{extKey}")
    @ResourceItemOperation(type = BUTTON, icon = "disabled", name = "${app.market.button.disable}")
    @Operation(description = "extension disabled")
    @OperationLog(operateType = OperateType.INACTIVE, idParamPosition = 0)
    public R<Boolean> disabled(@PathVariable String extKey) {
        return success(extInstallmentOperator.disabled(extKey));
    }

}
