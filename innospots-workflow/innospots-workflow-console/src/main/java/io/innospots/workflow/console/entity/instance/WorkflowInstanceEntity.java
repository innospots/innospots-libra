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

package io.innospots.workflow.console.entity.instance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Raydian
 * @date 2021/1/10
 */
@Setter
@Getter
@Entity
@TableName(value = WorkflowInstanceEntity.TABLE_NAME)
@Table(name = WorkflowInstanceEntity.TABLE_NAME, indexes = {
        @Index(columnList = "status,updatedTime,revision", name = "idx_status_revision")
})
public class WorkflowInstanceEntity extends PBaseEntity {

    public static final String TABLE_NAME = "flow_instance";

    public static final String TABLE_ID = "workflow_instance_id";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workflowInstanceId;

    @Column(length = 16)
    private String templateCode;

    @Column(length = 32)
    private String name;

    @Column(length = 32)
    private String flowKey;

    @Column(length = 32)
    private String triggerCode;

    @Column(length = 128)
    private String description;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;


    @Column
    private Integer revision;


    @Column(length = 16)
    @Enumerated(EnumType.STRING)
    private DataStatus status;


    /**
     * the node execution and flow execution datasource
     */
    @Column(length = 8)
    private String datasourceCode;


    @Column
    private Integer categoryId;

    @Column
    private LocalDateTime onlineTime;

    @Column(length = 32)
    private String tableName;

    @Column
    private Integer pageId;
}
