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
 * node execution entity
 *
 * @author Smars
 * @date 2021/6/29
 */
@Getter
@Setter
@Entity
@TableName(value = NodeExecutionEntity.TABLE_NAME)
@Table(name = NodeExecutionEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_flow_rev_ext_node", columnList = "flowInstanceId,revision"),
        @Index(name = "idx_flow_ext_node", columnList = "flowExecutionId")
})
public class NodeExecutionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "flow_execution_node";


    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 64)
    private String nodeExecutionId;

    @Column(length = 64)
    private String flowExecutionId;

    @Column
    private Long flowInstanceId;

    @Column
    private Integer revision;

    @Column
    private Integer sequenceNumber;


    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ExecutionStatus status;

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

    private String flowStartTime;

    @Column
    private Boolean next;

    @Column
    private Boolean skipNodeExecution;

    @Column(length = 256)
    private String nextNodeKeys;

    @Column(length = 256)
    private String previousNodeKeys;

    @Column(length = 16)
    private String nodeKey;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 32)
    private String nodeName;

    @Column(length = 32)
    private String nodeCode;


}
