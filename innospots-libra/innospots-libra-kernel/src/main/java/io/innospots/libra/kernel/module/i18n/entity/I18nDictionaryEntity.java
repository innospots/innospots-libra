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
import io.innospots.libra.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static io.innospots.libra.kernel.module.i18n.entity.I18nDictionaryEntity.TABLE_NAME;


/**
 * @author Smars
 * @date 2021/12/20
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME,indexes = {@Index(name ="idx_dict_code",columnList = "code")})
@TableName(TABLE_NAME)
public class I18nDictionaryEntity extends BaseEntity {

    public static final String TABLE_NAME = "i18n_dictionary";


    @Id
    @Column
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dictionaryId;

    @Column(length = 32)
    private String app;

    @Column(length = 32)
    private String module;

    @Column(length = 64)
    private String code;

    @Column(length = 128)
    private String description;


}
