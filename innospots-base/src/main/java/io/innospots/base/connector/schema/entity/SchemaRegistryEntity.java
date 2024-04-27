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
import io.innospots.base.connector.schema.model.SchemaRegistryType;
import io.innospots.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Raydian
 * @date 2021/1/17
 */
@Setter
@Getter
@Entity
@TableName(value = SchemaRegistryEntity.TABLE_NAME)
@Table(name = SchemaRegistryEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_credential_key", columnList = "credentialKey"),
        @Index(name = "idx_app_key",columnList = "appKey")
})
public class SchemaRegistryEntity extends PBaseEntity {

    public static final String TABLE_NAME = "ds_schema_registry";

    @TableId(type = IdType.INPUT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 16)
    private String registryId;

    @Column(length = 64)
    private String name;

    @Column(length = 64)
    private String code;

    @Column(length = 8)
    private String credentialKey;

    @Column
    private Integer categoryId;

    @Column(length = 256)
    private String description;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private SchemaRegistryType registryType;

    @Column(columnDefinition = "TEXT")
    private String configs;

    @Column(columnDefinition = "TEXT")
    private String script;

    @Column(length = 16)
    private String appKey;

    @Column(length = 16)
    private String scope;

}
