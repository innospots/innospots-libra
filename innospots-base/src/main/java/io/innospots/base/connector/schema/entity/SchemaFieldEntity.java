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

package io.innospots.base.connector.schema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Raydian
 * @date 2021/1/17
 */
@Getter
@Setter
@Entity
@TableName(value = SchemaFieldEntity.TABLE_NAME)
@Table(name = SchemaFieldEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_registry_fld", columnList = "registryKey")
})
public class SchemaFieldEntity extends PBaseEntity {

    public static final String TABLE_NAME = "ds_schema_field";

    @TableId(type = IdType.AUTO)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fieldId;

    @Column(length = 16)
    private String registryId;

    @Column(length = 64)
    private String registryCode;

    @Column(length = 64)
    private String name;

    @Column(length = 64)
    private String code;

    @Column(length = 32)
    @Enumerated(value = EnumType.STRING)
    private FieldValueType valueType;

    @Column(length = 32)
    private String defaultValue;

    @Column(length = 128)
    private String comment;

    @Column(length = 2048)
    private String config;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private FieldScope fieldScope;

    private Boolean pkey = false;

}
