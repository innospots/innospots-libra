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

package io.innospots.libra.kernel.module.notification.controller;

import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.notification.event.NotificationDefinitionLoader;
import io.innospots.libra.kernel.module.notification.event.NotificationGroup;
import io.innospots.libra.kernel.module.notification.model.NotificationSetting;
import io.innospots.libra.kernel.module.notification.operator.NotificationSettingOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author chenc
 * @version 1.0.0
 * @date 2022/4/29
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "notification/setting")
@ModuleMenu(menuKey = "notification-setting")
@Tag(name = "Notification Setting")
public class NotificationSettingController extends BaseController {

    private final NotificationSettingOperator notificationSettingOperator;

    public NotificationSettingController(NotificationSettingOperator notificationSettingOperator) {
        this.notificationSettingOperator = notificationSettingOperator;
    }

    @GetMapping("events")
    @Operation(summary = "get the list of the module in the events.")
    public R<Map<String, NotificationGroup>> events() {
        return success(NotificationDefinitionLoader.load());
    }

    @GetMapping("list")
    public R<List<NotificationSetting>> listMessageSettings() {

        List<NotificationSetting> notificationSettings = notificationSettingOperator.listMessageSettings();
        return success(notificationSettings);
    }

    @OperationLog(operateType = OperateType.UPDATE)
    @PostMapping()
    @ResourceItemOperation(type = BUTTON, icon = "save", name = "${common.button.update}")
    public R<Boolean> saveMessageSetting(
            @Parameter(name = "messageSettings") @RequestBody List<NotificationSetting> notificationSettings
    ) {
        Boolean result = notificationSettingOperator.saveMessageSetting(notificationSettings);
        return success(result);
    }
}