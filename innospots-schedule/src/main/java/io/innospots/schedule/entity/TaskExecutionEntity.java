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
import io.innospots.base.entity.BaseEntity;
import io.innospots.base.quartz.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

import static io.innospots.schedule.entity.TaskExecutionEntity.TABLE_NAME;


/**
 * @author Smars
 * @date 2023/8/7
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class TaskExecutionEntity extends BaseEntity {

    public static final String TABLE_NAME = "sys_task_execution";

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 64)
    private String taskExecutionId;

    @Column(length = 64)
    private String taskName;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ExecutionStatus executionStatus;

    @Column(length = 64)
    private String extensionKey;

    @Column(length = 16)
    private String extensionType;

    @Column(length = 32)
    private String appName;

    @Column(length = 32)
    private String appKey;

    @Column
    private int percent;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String paramContext;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private String detailUrl;
}
