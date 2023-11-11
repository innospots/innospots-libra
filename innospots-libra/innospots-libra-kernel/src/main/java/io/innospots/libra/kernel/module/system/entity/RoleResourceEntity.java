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
import io.innospots.base.entity.PBaseEntity;
import io.innospots.libra.kernel.module.system.model.role.resource.RoleResourceInfo;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static io.innospots.libra.kernel.module.system.entity.RoleResourceEntity.TABLE_NAME;

/**
 * the resources that the role has
 *
 * @author chenc
 * @date 2021/6/26
 */
@Getter
@Setter
@Entity
@TableName(value = TABLE_NAME)
@Table(name = TABLE_NAME)
public class RoleResourceEntity extends PBaseEntity {

    public static final String TABLE_NAME = "sys_role_resource";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleResourceId;

    @Column
    private Integer roleId;

    /**
     * 直接使用itemKey，不再使用resourceId
     */
    /*
    @Deprecated
    @Column
    private Integer resourceId;

     */

    @Column(length = 64)
    private String itemKey;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private RoleResourceInfo.RoleResourceType resourceType;
}