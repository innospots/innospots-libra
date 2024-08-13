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

package io.innospots.workflow.core.node.definition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.base.enums.DataScope;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * @author Raydian
 * @date 2020/12/14
 */
@Getter
@Setter
@Entity
@TableName(value = FlowNodeGroupEntity.TABLE_NAME)
@Table(name = FlowNodeGroupEntity.TABLE_NAME, indexes = {
        @Index(columnList = "flowTplId", name = "idx_flow_tpl_group")
})
public class FlowNodeGroupEntity extends PBaseEntity {

    public static final String TABLE_NAME = "flow_node_group";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer nodeGroupId;

    @Column(length = 64)
    private String name;

    @Column(length = 64)
    private String code;

    @Column
    private Integer flowTplId;

    @Column
    private Integer position;

    /**
     * system or user
     */
    @Column(length = 16)
//    @Enumerated(EnumType.STRING)
    private String scopes;
//    private DataScope scopes;

    public static FlowNodeGroupEntity constructor(Integer flowTplId, String name, String code, Integer position,DataScope dataScope) {
        FlowNodeGroupEntity entity = new FlowNodeGroupEntity();
        entity.flowTplId = flowTplId;
        entity.name = name;
        entity.code = code;
        if (position != null && position > 0) {
            entity.position = position;
        } else {
            entity.position = Integer.MAX_VALUE;
        }
        entity.scopes = dataScope.name();
        return entity;
    }


}
