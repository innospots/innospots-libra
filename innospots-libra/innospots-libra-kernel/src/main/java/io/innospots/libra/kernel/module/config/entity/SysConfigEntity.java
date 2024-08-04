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

package io.innospots.libra.kernel.module.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.crypto.EncryptType;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import static io.innospots.libra.kernel.module.config.entity.SysConfigEntity.TABLE_NAME;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class SysConfigEntity extends PBaseEntity {

    public static final String TABLE_NAME = "sys_config";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer configId;

    @Column(length = 8)
//    @Enumerated(EnumType.STRING)
    private String configGroup;

    @Column(length = 32)
    private String configName;

    @Column(length = 32)
    private String configCode;

    @Column(length = 1024)
    private String configValue;

    @Column(length = 32)
    private String placeHolder;

    @Column(length = 32)
    private String tips;

    @Column(length = 8)
    @Enumerated(EnumType.STRING)
    private EncryptType encryptType;

    @Column(length = 16)
    @Enumerated(EnumType.STRING)
    private FieldValueType valueType;

    @Column
    private Integer resourceId;

    @Column(length = 16)
    private String resourceType;

}
