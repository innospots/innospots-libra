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

package io.innospots.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Raydian
 * @date 2021/1/10
 */
@Setter
@Getter
@MappedSuperclass
public class BaseEntity {

    public static final String F_CREATED_TIME = "created_time";
    public static final String F_UPDATED_TIME = "updated_time";

    @TableField(fill = FieldFill.INSERT)
    @Column
    protected LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column
    protected LocalDateTime updatedTime;

    @TableField(fill = FieldFill.INSERT)
    @Column(length = 32)
    protected String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(length = 32)
    protected String updatedBy;
}
