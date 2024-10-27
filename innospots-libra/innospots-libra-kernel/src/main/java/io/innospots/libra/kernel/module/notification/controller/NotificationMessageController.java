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

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.notification.model.MessageFormQuery;
import io.innospots.libra.kernel.module.notification.model.NotificationMessage;
import io.innospots.libra.kernel.module.notification.operator.NotificationMessageOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "notification/message")
@ModuleMenu(menuKey = "notification-message")
@Tag(name = "Notification Message")
public class NotificationMessageController extends BaseController {

    private final NotificationMessageOperator notificationMessageOperator;

    public NotificationMessageController(NotificationMessageOperator notificationMessageOperator) {
        this.notificationMessageOperator = notificationMessageOperator;
    }

    @GetMapping("page")
    @Operation(summary = "page message")
    public R<PageBody<NotificationMessage>> pageMessages(MessageFormQuery request) {
        PageBody<NotificationMessage> pageModel = notificationMessageOperator.pageMessages(request);
        return success(pageModel);
    }

    @PutMapping("read")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${message.list.button.read}")
    @Operation(summary = "mark read")
    public R<Boolean> updateMessage(@Parameter(name = "messageIds") @RequestBody List<String> messageIds) {

        Boolean update = notificationMessageOperator.updateMessage(messageIds);
        return success(update);
    }

    @GetMapping("count/unread")
    @Operation(summary = "count unread message")
    public R<Long> countUnreadMessage() {
        return success(notificationMessageOperator.countUnreadMessage());
    }

    @OperationLog(operateType = OperateType.DELETE)
    @DeleteMapping
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(summary = "delete message")
    public R<Boolean> deleteMessage(@Parameter(name = "messageIds") @RequestBody List<Integer> messageIds) {

        Boolean delete = notificationMessageOperator.deleteMessage(messageIds);
        return success(delete);
    }
}