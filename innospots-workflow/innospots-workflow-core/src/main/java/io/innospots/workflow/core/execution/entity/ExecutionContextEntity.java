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

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 执行结果的数据明细，为了减少单表的数据量级，将数据明细和数据执行记录分开保存，主键值都是contextId
 *
 * @author Smars
 * @date 2021/7/1
 */
@Setter
@Getter
@Entity
@TableName(value = ExecutionContextEntity.TABLE_NAME)
@Table(name = ExecutionContextEntity.TABLE_NAME)
public class ExecutionContextEntity {

    public static final String TABLE_NAME = "flow_execution_context";

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 64)
    private String executionId;

    @Column(length = 8)
    private String contextType;

    @Column(columnDefinition = "text")
    private String nodePaths;

    @Column(columnDefinition = "LONGTEXT")
    private String inputs;

    @Column(columnDefinition = "LONGTEXT")
    private String outputs;

    @Column
    private Integer inputSize;

    @Column
    private Integer outputSize;

    @TableField(fill = FieldFill.INSERT)
    @Column
    private LocalDateTime createdTime;

    public enum ContextType {
        /**
         *
         */
        FLOW,
        NODE
    }

}
