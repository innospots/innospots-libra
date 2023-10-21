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

package io.innospots.workflow.console.entity.execution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.libra.base.entity.PBaseEntity;
import io.innospots.workflow.core.execution.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 执行记录，固定字段，预留20个字段位做看板分析使用
 *
 * @author Smars
 * @date 2021/6/29
 */
@Getter
@Setter
@Entity
@TableName(value = FlowExecutionEntity.TABLE_NAME)
@Table(name = FlowExecutionEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_flow_rev", columnList = "flowInstanceId,revision")})
public class FlowExecutionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "flow_execution";


    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 64)
    private String flowExecutionId;

    @Column
    private Long flowInstanceId;

    @Column
    private Integer revision;

    /**
     * not store node execution
     */
    @Column
    private boolean skipNodeExecution;

    /**
     * not store flow execution
     */
    @Column
    private boolean skipFlowExecution;

    /**
     * yyyymmddHHMMss.sss
     */
    @Column(length = 32)
    private String startTime;

    /**
     * yyyymmddHHMMss.sss
     */
    @Column(length = 32)
    private String endTime;

    /**
     * yyyymmddHHMMss.sss
     */
    @Column(length = 32)
    private String dataTime;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ExecutionStatus status;

    @Column(length = 64)
    private String currentNodeKeys;

    @Column(length = 64)
    private String parentFlowExecutionId;

    /*
    @Column
    private Integer strategyId;

    @Column
    private Integer stgPeriod;
     */

    @Column(length = 32)
    private String uuid;

    @Column(length = 8)
    private String uuidType;

    @Column(length = 16)
    private String channel;

    @Column(length = 32)
    private String location;

    @Column(length = 128)
    private String executionUri;

    /**
     * trigger code
     */
    @Column(length = 128)
    private String source;

    @Column
    private Integer hitNodeNumber;

    @Column(length = 2048)
    private String message;

    @Column(length = 2048)
    private String result;

    @Column(length = 8)
    private String resultCode;

    @Column(length = 8)
    private String executeMode;

}
