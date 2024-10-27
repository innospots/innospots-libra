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

import io.innospots.base.model.response.R;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.kernel.module.system.model.SystemUpdateInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * system update info api
 *
 * @author chenc
 * @date 2022/9/25 17:10
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "system/update")
@ModuleMenu(menuKey = "libra-workspace")
@Tag(name = "System Update Info")
public class SystemUpdateInfoController extends BaseController {

    @GetMapping
    @Operation(summary = "get system update info")
    public R<SystemUpdateInfo> getSystemUpdateInfo() {
        SystemUpdateInfo updateInfo = new SystemUpdateInfo();
        LibraExtensionProperties libraProperties = LibraClassPathExtPropertiesLoader.getLibraKernelProperties();
        updateInfo.setDetail("此版本发布时间："+libraProperties.getPublishTime());
        updateInfo.setCurrentVersion(libraProperties.getVersion());
        updateInfo.setLastCheckDate(DateTimeUtils.formatDate(new Date(), DateTimeUtils.DEFAULT_DATE_PATTERN + " " + DateTimeUtils.DEFAULT_SIMPLE_TIME_PATTERN));
        updateInfo.setNewest(true);
        return success(updateInfo);
    }
}