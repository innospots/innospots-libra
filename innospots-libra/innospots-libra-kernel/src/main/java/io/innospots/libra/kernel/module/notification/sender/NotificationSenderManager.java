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

package io.innospots.libra.kernel.module.notification.sender;

import cn.hutool.extra.mail.MailAccount;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.kernel.events.MessageEvent;
import io.innospots.libra.base.operator.SysUserReader;
import io.innospots.libra.kernel.module.notification.entity.NotificationMessageEntity;
import io.innospots.libra.kernel.module.notification.model.NotificationChannel;
import io.innospots.libra.kernel.module.notification.model.NotificationSetting;
import io.innospots.libra.kernel.module.notification.operator.NotificationChannelOperator;
import io.innospots.libra.kernel.module.notification.operator.NotificationMessageOperator;
import io.innospots.libra.kernel.module.notification.operator.NotificationSettingOperator;
import io.innospots.libra.kernel.service.EmailAccountLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/26
 */
@Component
@Slf4j
public class NotificationSenderManager {

    private final NotificationChannelOperator notificationChannelOperator;

    private final NotificationSettingOperator notificationSettingOperator;

    private final NotificationMessageOperator notificationMessageOperator;

    private final EmailAccountLoader emailAccountLoader;

    private final SysUserReader sysUserReader;

    public NotificationSenderManager(NotificationChannelOperator notificationChannelOperator,
                                     NotificationSettingOperator notificationSettingOperator,
                                     NotificationMessageOperator notificationMessageOperator,
                                     EmailAccountLoader emailAccountLoader,
                                     SysUserReader sysUserReader) {
        this.notificationChannelOperator = notificationChannelOperator;
        this.notificationSettingOperator = notificationSettingOperator;
        this.notificationMessageOperator = notificationMessageOperator;
        this.sysUserReader = sysUserReader;
        this.emailAccountLoader = emailAccountLoader;
    }

    private LoadingCache<Integer, INotificationSender> senderCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(10))
            .build(new CacheLoader<Integer, INotificationSender>() {
                @Override
                public @Nullable INotificationSender load(@NonNull Integer channelId) throws Exception {
                    return buildSender(channelId);
                }
            });


    public void send(MessageEvent messageEvent) {
        // TODO 基于messageSetting 的发送分组，发送渠道分群组用户和渠道分别推送
        NotificationSetting notificationSetting = notificationSettingOperator.getMessageSettingByEventCode(messageEvent.getCode());
        if (notificationSetting == null) {
            return;
        }
        List<Integer> channels = notificationSetting.getChannels();
        List<Integer> groups = notificationSetting.getGroups();
        if (CollectionUtils.isNotEmpty(channels) && CollectionUtils.isNotEmpty(groups)) {
            List<Integer> roleIds = groups.stream().collect(Collectors.toCollection(() -> new ArrayList<>(groups.size())));

            List<UserInfo> users = sysUserReader.listUserByRoleIds(roleIds);
            if (CollectionUtils.isEmpty(users)) {
                return;
            }

            List<NotificationChannel> notificationChannels = notificationChannelOperator.getMessageChannels(channels);
            Map<Integer, NotificationChannel> messageChannelMap = notificationChannels.stream().collect(Collectors.toMap(NotificationChannel::getChannelId, Function.identity()));


            for (UserInfo userInfo : users) {

                for (Integer channel : channels) {
                    if (MapUtils.isEmpty(messageChannelMap) || messageChannelMap.get(channel) == null) {
                        continue;
                    }
                    NotificationMessageEntity messageLog = new NotificationMessageEntity();
                    messageLog.setRecordTime(LocalDateTime.now());
                    messageLog.setReadFlag(Boolean.FALSE);
                    messageLog.setMessage(messageEvent.getMessage());
                    messageLog.setTitle(messageEvent.getTitle());
                    messageLog.setModule(messageEvent.getModule());
                    messageLog.setExtName(messageEvent.getExtName());
                    messageLog.setReceiveUser(userInfo.getUserName());
                    messageLog.setReceiveUserId(userInfo.getUserId());
                    messageLog.setEventCode(messageEvent.getCode());
                    messageLog.setEventName(messageEvent.getEventName());
                    messageLog.setChannelName(messageChannelMap.get(channel).getChannelName());
                    messageLog.setChannelId(channel);
                    messageLog.setSendSuccess(Boolean.TRUE);
                    INotificationSender messageSender = senderCache.get(channel);
                    if (messageSender != null) {
                        messageSender.send(messageLog,userInfo);
                    } else {
                        log.error("not message sender, {}", messageLog);
                    }
                }
            }
        }

    }

    private INotificationSender buildSender(Integer channelId) {
        NotificationChannel notificationChannel = notificationChannelOperator.getMessageChannel(channelId);
        INotificationSender messageSender = null;
        if (notificationChannel != null) {
            switch (notificationChannel.getChannelType()) {
                case INBOX:
                    messageSender = new InboxNotificationSender(notificationMessageOperator);
                    break;
                case APP:
                    messageSender = new AppNotificationSender();
                    break;
                case EMAIL:
                    MailAccount mailAccount = emailAccountLoader.getSystemMailAccount();
                    messageSender = new EmailNotificationSender(mailAccount);
                    break;
                default:
                    break;
            }
        }
        return messageSender;
    }
}
