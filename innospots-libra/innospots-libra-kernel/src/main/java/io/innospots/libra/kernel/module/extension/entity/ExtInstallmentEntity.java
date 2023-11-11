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

package io.innospots.libra.kernel.module.extension.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.libra.base.extension.ExtensionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

import static io.innospots.libra.kernel.module.extension.entity.ExtInstallmentEntity.TABLE_NAME;

/**
 * Extension installation information in current services and projects
 *
 * @author Smars
 * @date 2021/12/1
 */

@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class ExtInstallmentEntity extends PBaseEntity {

    public static final String TABLE_NAME = "ext_installment";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer extInstallmentId;

    @Column(length = 64)
    private String extKey;

    @Column
    private LocalDateTime installTime;

    @Column(length = 32)
    private String installPerson;

    @Column(length = 16)
    private String installVersion;

    @Column(length = 256)
    private String licenseKey;

    @Column(length = 32)
    private String licenseTime;

    /**
     * INSTALLED   ENABLED   DISABLED
     */
    @Column(length = 16)
    @Enumerated(EnumType.STRING)
    private ExtensionStatus extensionStatus;
}
