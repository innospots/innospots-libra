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

package io.innospots.libra.kernel.module.i18n.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static io.innospots.libra.kernel.module.i18n.entity.I18nLanguageEntity.TABLE_NAME;


/**
 * @author Smars
 * @date 2021/12/20
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class I18nLanguageEntity extends BaseEntity {

    public static final String TABLE_NAME = "i18n_language";
    public static final String FIELD_DEFAULT_LAN = "default_lan";


    @Id
    @Column
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer languageId;

    @Column(length = 32)
    private String name;

    /**
     * unique in the table
     */
    @Column(length = 16)
    private String locale;

    @Column(length = 16)
    private String icon;

    @Column
    private Integer currencyId;

    @Column(length = 4)
    private String decimalSeparator;

    @Column(length = 4)
    private String thousandSeparator;

    @Column(length = 16)
    private String dateFormat;

    @Column(length = 16)
    private String timeFormat;

    @Column
    private boolean defaultLan;

    @Column(length = 16)
    @Enumerated(EnumType.STRING)
    private DataStatus status;

}
