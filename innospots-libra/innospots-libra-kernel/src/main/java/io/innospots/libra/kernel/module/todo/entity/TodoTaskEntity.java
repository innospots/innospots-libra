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

package io.innospots.libra.kernel.module.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.BaseEntity;
import io.innospots.libra.kernel.module.todo.enums.TaskPriority;
import io.innospots.libra.kernel.module.todo.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

import static io.innospots.libra.kernel.module.todo.entity.TodoTaskEntity.TABLE_NAME;


/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/7
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class TodoTaskEntity extends BaseEntity {


    public static final String TABLE_NAME = "sys_todo_task";

    @Id
    @Column
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer taskId;

    @Column(length = 128)
    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer principalUserId;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private TaskStatus taskStatus;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private TaskPriority taskPriority;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

}
