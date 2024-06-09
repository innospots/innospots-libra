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
import io.innospots.base.entity.PBaseEntity;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.base.quartz.JobType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Getter
@Setter
@Entity
@Table(name = ScheduleJobInfoEntity.TABLE_NAME)
@TableName(ScheduleJobInfoEntity.TABLE_NAME)
public class ScheduleJobInfoEntity extends PBaseEntity {

    public static final String TABLE_NAME = "schedule_job_info";

    @Id
    @TableId(type = IdType.INPUT)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(length = 16)
    private String jobKey;

    @Column(length = 32)
    private String jobName;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ScheduleMode scheduleMode;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private DataStatus jobStatus;

    /**
     * job type
     */
    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private JobType jobType;

    /**
     * sub job count, zero if execute
     */
    @Column
    private Integer subJobCount;

    /**
     * job implement class, which will be executed
     */
    @Column(length = 128)
    private String jobClass;

    //TODO
    @Column(length = 128)
    private String splitter;

    /**
     * timeConfig json format, include runtime, time period, TimePeriod, period Times
     */
    @Column(length = 2056)
    private String timeConfig;

    /**
     * job params map
     */
    @Column(columnDefinition = "TEXT")
    private String params;

    /**
     * module job scope
     */
    @Column(length = 32)
    private String scopes;

    /**
     * module resource key relation the schedule job
     */
    @Column(length = 32)
    private String resourceKey;

    /**
     * job category id
     */
    @Column
    private Integer categoryId;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

}
