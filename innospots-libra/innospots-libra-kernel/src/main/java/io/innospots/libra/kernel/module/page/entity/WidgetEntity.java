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

package io.innospots.libra.kernel.module.page.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.libra.kernel.module.page.enums.WidgetType;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import static io.innospots.libra.kernel.module.page.entity.WidgetEntity.TABLE_NAME;

/**
 * @author xiaoling
 * @date 2020-10-06
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME, indexes = {@Index(name = "idx_page_wdt", columnList = "pageId")})
@TableName(TABLE_NAME)
public class WidgetEntity extends PBaseEntity {

    public static final String TABLE_NAME = "dgm_widget";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer widgetId;

    @Column
    private Integer pageId;

    @Column
    private Integer parentId;

    @Column(length = 64)
    private String widgetKey;

    @Column(length = 128)
    private String name;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private WidgetType type;

    @Column(length = 128)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String config;

    @Column(length = 2048)
    private String viewCodes;

    @Column(length = 2048)
    private String viewIds;

    @Column(length = 2048)
    private String relations;

    @Column
    private Boolean isDelete = false;
}
