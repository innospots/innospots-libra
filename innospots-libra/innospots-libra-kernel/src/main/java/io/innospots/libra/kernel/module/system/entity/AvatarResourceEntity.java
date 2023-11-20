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

package io.innospots.libra.kernel.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.enums.ImageType;
import io.innospots.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * avatar_resource
 *
 * @author chenc
 * @date 2022/1/19
 */
@Getter
@Setter
@Entity
@TableName(value = AvatarResourceEntity.TABLE_NAME)
@Table(name = AvatarResourceEntity.TABLE_NAME)
public class AvatarResourceEntity extends BaseEntity {
    public static final String TABLE_NAME = "sys_avatar_resource";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer avatarId;

    @Column(length = 16)
    private String resourceId;

    @Column(length = 12)
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @Column(columnDefinition = "mediumtext")
    private String imageBase64;

    @Column
    private Integer imageSort;
}