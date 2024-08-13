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

package io.innospots.libra.kernel.module.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static io.innospots.libra.kernel.module.notification.entity.NotificationMessageEntity.TABLE_NAME;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class NotificationMessageEntity extends PBaseEntity {

    public static final String TABLE_NAME = "sys_notification_message";

    @Id
    @Column
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column
    private LocalDateTime recordTime;

    @Column
    private LocalDateTime readTime;

    @Column
    private Boolean readFlag;

    @Column(length = 2048)
    private String message;

    @Column(length = 32)
    private String extName;

    @Column(length = 64)
    private String title;

    @Column(length = 32)
    private String module;

    @Column(length = 32)
    private String receiveUser;

    @Column
    private Integer receiveUserId;

    @Column(length = 16)
    private String eventCode;

    @Column(length = 64)
    private String eventName;

    @Column(length = 16)
    private String channelName;

    @Column
    private Integer channelId;

    @Column
    private Boolean sendSuccess;

    @Column(length = 128)
    private String sendResult;

}
