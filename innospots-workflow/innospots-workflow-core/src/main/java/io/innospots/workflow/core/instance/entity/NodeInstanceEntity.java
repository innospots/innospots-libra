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

package io.innospots.workflow.core.instance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * @author Raydian
 * @date 2020/12/20
 */
@Setter
@Getter
@Entity
@TableName(value = NodeInstanceEntity.TABLE_NAME)
@Table(name = NodeInstanceEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_flow_rev_node", columnList = "workflowInstanceId,revision")
})
public class NodeInstanceEntity extends PBaseEntity {

    public static final String TABLE_NAME = "flow_instance_node";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nodeInstanceId;

    @Column
    private Long workflowInstanceId;

    @Column
    private Integer nodeDefinitionId;

    @Column(length = 128)
    private String nodeType;

    @Column(length = 32)
    private String name;

    @Column(length = 32)
    private String displayName;

    @Column(length = 256)
    private String description;

    @Column(length = 64)
    private String nodeKey;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column
    private Integer x;

    @Column
    private Integer y;

    /**
     * current version
     */

    @Column
    private Integer revision;


    @Column(columnDefinition = "TEXT")
    private String data;

    @Column
    private Boolean pauseFlag;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ScriptType actionScriptType;

    @Column(columnDefinition = "TEXT")
    private String action;

    @Column(columnDefinition = "TEXT")
    private String methods;


    @Column(length = 1024)
    private String inputFields;

    @Column(columnDefinition = "TEXT")
    private String outputFields;

    @Column(length = 1024)
    private String ports;

    @Column
    private Boolean continueOnFail;

    @Column
    private Boolean retryOnFail;

    @Column
    private Boolean failureBranch;

    @Column
    private Integer maxTries;

    @Column
    private Integer retryWaitTimeMills;


    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private DataStatus status = DataStatus.ONLINE;

}
