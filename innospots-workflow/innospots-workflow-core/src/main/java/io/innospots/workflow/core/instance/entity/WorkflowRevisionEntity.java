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
import io.innospots.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Raydian
 * @date 2021/1/12
 */
@Setter
@Getter
@Entity
@TableName(value = WorkflowRevisionEntity.TABLE_NAME)
@Table(name = WorkflowRevisionEntity.TABLE_NAME)
public class WorkflowRevisionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "flow_instance_revision";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flowRevisionId;

    @Column
    private Integer revision;

    @Column
    private Long workflowInstanceId;

    /**
     * the count of nodes in this revision
     */
    @Column
    private Long nodeNumber;

    @Column(length = 128)
    private String description;


    public static WorkflowRevisionEntity build(Long workflowInstanceId, Integer revision, Long nodeNumber, String description) {
        WorkflowRevisionEntity entity = new WorkflowRevisionEntity();
        entity.setNodeNumber(nodeNumber);
        entity.setWorkflowInstanceId(workflowInstanceId);
        entity.setRevision(revision);
        entity.setDescription(description);
        return entity;
    }
}
