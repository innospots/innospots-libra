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

package io.innospots.libra.security.logger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.BaseEntity;
import io.innospots.libra.base.terminal.TerminalInfo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2021/12/13
 */
@Getter
@Setter
@Entity
@TableName(value = LoginLogEntity.TABLE_NAME)
@Table(name = LoginLogEntity.TABLE_NAME)
public class LoginLogEntity extends BaseEntity {

    public static final String TABLE_NAME = "sys_login_log";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private LocalDateTime loginTime;

    @Column
    private Integer userId;

    @Column(length = 32)
    private String userName;

    @Column(length = 64)
    private String userAvatar;

    @Column(length = 128)
    private String userRoleName;

    @Column(length = 32)
    private String ip;

    @Column(length = 32)
    private String province;

    @Column(length = 32)
    private String city;

    @Column(length = 32)
    private String browser;

    @Column(length = 32)
    private String os;

    @Column(length = 128)
    private String detail;

    @Column(length = 16)
    private String status;

    public void fill(TerminalInfo terminalInfo) {
        this.ip = terminalInfo.getIp();
        this.province = terminalInfo.getProvince();
        this.city = terminalInfo.getCity();
        this.browser = terminalInfo.getBrowser();
        this.os = terminalInfo.getSystem();
    }
}