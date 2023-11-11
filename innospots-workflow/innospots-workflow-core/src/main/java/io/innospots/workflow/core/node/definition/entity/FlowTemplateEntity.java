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
import io.innospots.base.enums.DataStatus;
import io.innospots.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Raydian
 * @date 2020/12/14
 */
@Setter
@Getter
@Entity
@TableName(value = FlowTemplateEntity.TABLE_NAME)
@Table(name = FlowTemplateEntity.TABLE_NAME)
public class FlowTemplateEntity extends BaseEntity {

    public static final String TABLE_NAME = "flow_template";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer flowTplId;

    @Column(length = 32)
    private String tplName;

    @Column(length = 32)
    private String tplCode;

    @Column(length = 16)
    private String type;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private DataStatus status;

    @Column(length = 128)
    private String description;

    @Column(length = 32)
    private String startNodeCode;


    public FlowTemplateEntity() {
        super();
    }

    public FlowTemplateEntity(String tplName, String tplCode) {
        this.tplName = tplName;
        this.tplCode = tplCode;
        this.setStatus(DataStatus.OFFLINE);
        this.setCreatedTime(LocalDateTime.now());
        this.setUpdatedTime(LocalDateTime.now());
    }
}
