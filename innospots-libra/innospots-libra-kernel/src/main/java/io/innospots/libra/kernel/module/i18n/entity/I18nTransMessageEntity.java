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
import io.innospots.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import static io.innospots.libra.kernel.module.i18n.entity.I18nTransMessageEntity.TABLE_NAME;


/**
 * @author Smars
 * @date 2021/12/20
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class I18nTransMessageEntity extends BaseEntity {

    public static final String TABLE_NAME = "i18n_trans_message";


    @Id
    @Column
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @Column(length = 16)
    private String locale;

    @Column
    private Integer dictionaryId;

    @Column(length = 512)
    private String message;


    public I18nTransMessageEntity() {
    }

    public I18nTransMessageEntity(Integer dictionaryId, String locale, String message) {
        this.dictionaryId = dictionaryId;
        this.locale = locale;
        this.message = message;
    }
}
