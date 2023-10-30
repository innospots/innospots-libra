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

package io.innospots.libra.kernel.module.credential.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.libra.base.entity.PBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2023/1/19
 */
@Getter
@Setter
@Entity
@Table(name = CredentialInfoEntity.TABLE_NAME)
@TableName(CredentialInfoEntity.TABLE_NAME)
public class CredentialInfoEntity extends PBaseEntity {

    public static final String TABLE_NAME = "ds_credential_info";

    @Id
    @TableId(type = IdType.INPUT)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 8)
    private String credentialKey;

    @Column(length = 32)
    private String name;

    /**
     * credential type primary key
     */
    @Column(length = 8)
    private String credentialTypeCode;

    @Column(length = 2048)
    private String props;

    @Column(length = 2048)
    private String encryptFormValues;

}
