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
import io.innospots.base.enums.DataStatus;
import io.innospots.base.enums.OnOff;
import io.innospots.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * sys_user
 *
 * @author Raydian
 * @date 2021/1/10
 */
@Getter
@Setter
@Entity
@TableName(value = SysUserEntity.TABLE_NAME)
@Table(name = SysUserEntity.TABLE_NAME)
public class SysUserEntity extends BaseEntity {

    public static final String TABLE_NAME = "sys_user";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(length = 128)
    private String email;

    @Column(length = 128)
    private String realName;

    @Column(length = 128)
    private String userName;

    @Column(length = 128)
    private String password;

    @Column(length = 32)
    private String mobile;

    @Column(length = 128)
    private String department;

    @Column
    private String remark;

    @Column(length = 16)
//    @Enumerated(EnumType.STRING)
    private String status;
//    private DataStatus status;

    @Column(length = 16)
//    @Enumerated(EnumType.STRING)
//    private OnOff onOff;
    private String onOff;

    @Column
    private Integer loginTimes;

    @Column
    private LocalDateTime lastAccessTime;

    @Column
    private Integer lastOrgId;

    @Column
    private Integer lastProjectId;

    @Column(length = 64)
    private String avatarKey;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SysUserEntity{");
        sb.append("createdTime=").append(createdTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", userId=").append(userId);
        sb.append(", email='").append(email).append('\'');
        sb.append(", realName='").append(realName).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", mobile='").append(mobile).append('\'');
        sb.append(", department='").append(department).append('\'');
        sb.append(", remark='").append(remark).append('\'');
        sb.append(", status=").append(status);
        sb.append(", onOff=").append(onOff);
        sb.append(", loginTimes=").append(loginTimes);
        sb.append(", lastAccessTime=").append(lastAccessTime);
        sb.append(", lastOrgId=").append(lastOrgId);
        sb.append(", lastProjectId=").append(lastProjectId);
        sb.append('}');
        return sb.toString();
    }
}
