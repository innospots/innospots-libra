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
import io.innospots.base.quartz.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

import static io.innospots.schedule.entity.JobExecutionEntity.TABLE_NAME;


/**
 * job execution
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

    /**
     * primary id
     */
    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 32)
    private String executionId;

    /**
     * job name
     */
    @Column(length = 32)
    private String jobName;

    /**
     * job primary key
     */
    @Column(length = 16)
    private String jobKey;

    /**
     * job key type: flow, node ,task etc
     */
    @Column(length = 16)
    protected String keyType;

    /**
     * md5(context+jobKey), job execution instance primary key, which be created by params and job key
     */
    @Column(length = 64)
    protected String instanceKey;

    /**
     * value: group, execute,flow
     */
    @Column(length = 32)
    private String scopes;

    /**
     * execute job implement class
     */
    @Column(length = 64)
    private String jobClass;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ExecutionStatus executionStatus;

    /**
     * the value of execution process percent
     */
    @Column
    private Integer percent;

    /**
     * sub job count, if this job is the group or container type
     */
    @Column
    private Long subJobCount;


    @Column
    private Long successCount;


    @Column
    private Long failCount;

    /**
     * start time
     */
    @Column
    private LocalDateTime startTime;

    /**
     * end time
     */
    @Column
    private LocalDateTime endTime;

    @Column
    private LocalDateTime selfEndTime;

    /**
     * execution param context, json map format
     */
    @Column(columnDefinition = "TEXT")
    private String context;

    /**
     * complete or fail reason
     */
    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * execution sequence
     */
    @Column
    private Integer sequenceNumber;


    /**
     * job execution detail uri, using in the console, if have more job information will be showed
     */
    @Column(length = 128)
    private String detailUri;

    /**
     * external execution primary id, eg, flow execution
     */
    @Column(length = 32)
    private String extExecutionId;

    /**
     * external resource primary key
     */
    @Column(length = 32)
    private String resourceKey;

    /**
     * the first execution primary id ,if the job execution is failure, and repeat execute again
     */
    @Column(length = 32)
    private String originExecutionId;

    /**
     * parent execution primary id ,if the job execution is the sub job execution
     */
    @Column(length = 32)
    private String parentExecutionId;

    /**
     * Optimistic lock version
     */
    @Version
    @Column
    private Integer version;

    /**
     * executor node key: ip+port
     */
    @Column(length = 32)
    private String serverKey;

}
