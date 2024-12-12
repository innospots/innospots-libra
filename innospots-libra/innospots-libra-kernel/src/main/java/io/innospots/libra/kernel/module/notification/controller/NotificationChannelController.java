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

import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.notification.model.MessageElement;
import io.innospots.libra.kernel.module.notification.model.NotificationChannel;
import io.innospots.libra.kernel.module.notification.operator.NotificationChannelOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author chenc
 * @version 1.0.0
 * @date 2022/4/29
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "notification/channel")
@ModuleMenu(menuKey = "notification-channel")
@Tag(name = "Notification Channel")
public class NotificationChannelController extends BaseController {

    private final NotificationChannelOperator notificationChannelOperator;

    public NotificationChannelController(NotificationChannelOperator notificationChannelOperator) {
        this.notificationChannelOperator = notificationChannelOperator;
    }

    @OperationLog(operateType = OperateType.CREATE, primaryField = "channelId")
    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.add}")
    @Operation(description = "create channel")
    public R<NotificationChannel> createChannel(@Parameter(name = "messageChannel", required = true) @Valid @RequestBody NotificationChannel notificationChannel) {

        NotificationChannel save = notificationChannelOperator.createChannel(notificationChannel);
        return success(save);
    }

    @OperationLog(operateType = OperateType.UPDATE, primaryField = "channelId")
    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.edit}")
    @Operation(description = "update channel")
    public R<Boolean> updateChannel(@Parameter(name = "messageChannel", required = true) @Valid @RequestBody NotificationChannel notificationChannel) {

        Boolean update = notificationChannelOperator.updateChannel(notificationChannel);
        return success(update);
    }

    @OperationLog(operateType = OperateType.UPDATE_STATUS, idParamPosition = 0)
    @PutMapping("{channelId}/{dataStatus}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.status}", label = "${message.channel.status.change}")
    @Operation(description = "update channel status")
    public R<Boolean> updateStatus(@Parameter(name = "channelId", required = true) @PathVariable Integer channelId,
                                   @Parameter(required = true, name = "dataStatus") @PathVariable DataStatus dataStatus) {

        Boolean delete = notificationChannelOperator.updateStatus(channelId, dataStatus);
        return success(delete);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{channelId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(description = "delete channel")
    public R<Boolean> deleteChannel(@Parameter(name = "channelId", required = true) @PathVariable Integer channelId) {

        Boolean delete = notificationChannelOperator.deleteChannel(channelId);
        return success(delete);
    }

    @GetMapping
    @Operation(description = "list message channel")
    public R<List<NotificationChannel>> listChannels() {
        List<NotificationChannel> list = notificationChannelOperator.listChannels();
        return success(list);
    }

    @GetMapping("mapping")
    @Operation(description = "mapping column")
    public R<List<MessageElement>> mapping() {
        List<MessageElement> messageElements = new ArrayList<>();
        messageElements.add(new MessageElement("user_name", "${notification.user.label}"));
        messageElements.add(new MessageElement("user_identity", "${notification.identity.label}"));
        messageElements.add(new MessageElement("message_title", "${notification.title.label}"));
        messageElements.add(new MessageElement("message_content", "${notification.content.label}"));

        return R.success(messageElements);
    }
}