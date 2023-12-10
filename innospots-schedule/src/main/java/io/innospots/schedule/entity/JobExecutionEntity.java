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
import io.innospots.base.execution.ExecMode;
import io.innospots.base.quartz.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

import static io.innospots.schedule.entity.JobExecutionEntity.TABLE_NAME;


/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class JobExecutionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "schedule_job_execution";

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 32)
    private String executionId;

    @Column(length = 32)
    private String name;

    @Column(length = 16)
    private String key;

    @Column(length = 16)
    protected String keyType;

    /**
     * md5(context+key)
     */
    @Column(length = 32)
    protected String instanceKey;

    @Column(length = 32)
    private String scopes;

    @Column(length = 64)
    private String jobClass;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ExecutionStatus status;

    @Column
    private Integer percent;

    @Column
    private Integer subJobCount;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private Integer sequenceNumber;

    @Column(length = 128)
    private String detailUri;

    @Column(length = 32)
    private String extExecutionId;

    @Column(length = 32)
    private String resourceKey;

    @Column(length = 32)
    private String originExecutionId;

    @Column(length = 32)
    private String parentExecutionId;

    @Version
    @Column
    private Integer version;

    @Column(length = 32)
    private String serverKey;

}
