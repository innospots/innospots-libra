/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.schedule.enums.MessageStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static io.innospots.schedule.entity.ReadyJobEntity.TABLE_NAME;

/**
 * job will be executed in the ready queue
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
@Builder
public class ReadyJobEntity extends PBaseEntity {

    public static final String TABLE_NAME = "schedule_ready_queue";

    public ReadyJobEntity() {
    }

    /**
     * md5(jobKey + params)
     */
    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 32)
    private String jobReadyKey;

    /**
     * execute job implement class
     */
    @Column(length = 64)
    private String jobClass;


    @Column(length = 32)
    private String originExecutionId;

    @Column(length = 32)
    private String parentExecutionId;

    @Column(length = 32)
    private String name;

    @Column(length = 16)
    private String key;

    @Column(length = 16)
    private String keyType;

    @Column(length = 32)
    private String scopes;

    /**
     * param context, json map format
     */
    @Column(columnDefinition = "TEXT")
    private String context;

    /**
     * messsage status
     */
    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private MessageStatus messageStatus;

    @Column
    private Integer sequenceNumber;

    @Column(length = 8)
    private String groupKey;

    @Column(length = 32)
    private String extExecutionId;


    @Column(length = 32)
    private String resourceKey;

    @Column(length = 32)
    private String serverKey;

    @Version
    @Column
    private Integer version;

}
