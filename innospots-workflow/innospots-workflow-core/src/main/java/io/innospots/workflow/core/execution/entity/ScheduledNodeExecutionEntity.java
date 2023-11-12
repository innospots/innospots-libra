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

package io.innospots.workflow.core.execution.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * schedule node execution that will be execute when the schedule time arrives
 *
 * @author Smars
 * @date 2021/9/19
 */
@Getter
@Setter
@Entity
@TableName(value = ScheduledNodeExecutionEntity.TABLE_NAME)
@Table(name = ScheduledNodeExecutionEntity.TABLE_NAME, indexes = {
        @Index(columnList = "scheduledTime,status,serverKey,shardingKey", name = "idx_server_sharding")
}
)
public class ScheduledNodeExecutionEntity extends PBaseEntity {


    public static final String TABLE_NAME = "flow_execution_node_scheduled";

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 64)
    private String nodeExecutionId;

    @Column(length = 64)
    private String flowExecutionId;

    /**
     * 定时延时执行时间
     */
    @Column(length = 64)
    private LocalDateTime scheduledTime;

    @Column
    private int shardingKey;

    @Column(length = 64)
    private String serverKey;

    @Column(length = 16)
    private String nodeKey;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ExecutionStatus status;

    @Column(length = 1024)
    private String message;

}
