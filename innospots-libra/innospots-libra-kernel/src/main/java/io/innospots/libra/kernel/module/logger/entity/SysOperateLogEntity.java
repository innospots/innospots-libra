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

package io.innospots.libra.kernel.module.logger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.terminal.TerminalInfo;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Raydian
 * @date 2021/1/17
 */
@Getter
@Setter
@Entity
@TableName(value = SysOperateLogEntity.TABLE_NAME)
@Table(name = SysOperateLogEntity.TABLE_NAME)
public class SysOperateLogEntity extends PBaseEntity {

    public static final String TABLE_NAME = "sys_operation_log";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column
    private LocalDateTime operateTime;

    @Column(length = 64)
    private String module;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private OperateType operateType;

    @Column(length = 64)
    private String resourceId;

    @Column(length = 32)
    private String resourceType;

    @Column(length = 64)
    private String resourceName;

    @Column(length = 32)
    private String ip;

    @Column
    private Integer userId;

    @Column(length = 32)
    private String username;

    @Column(length = 64)
    private String userAvatar;

    /**
     * multi role split by comma
     */
    @Column(length = 128)
    private String roles;

    @Column(length = 256)
    private String detail;

    @Column(length = 32)
    private String location;

    @Column(length = 32)
    private String province;

    @Column(length = 32)
    private String city;

    @Column(length = 64)
    private String browser;

    @Column(length = 32)
    private String osSystem;

    @Column(length = 32)
    private String device;

    @Column(length = 32)
    private String manufacturer;

    @Column(length = 64)
    private String resolution;

    @Column(length = 128)
    private String language;

    @Column(length = 128)
    private String requestPath;

    public void fill(TerminalInfo terminalInfo) {
        this.ip = terminalInfo.getIp();
        this.province = terminalInfo.getProvince();
        this.city = terminalInfo.getCity();
        this.browser = terminalInfo.getBrowser();
        this.language = terminalInfo.getLanguage();
        this.location = terminalInfo.getLocation();
        this.osSystem = terminalInfo.getSystem();
        this.device = terminalInfo.getDevice();
        this.manufacturer = terminalInfo.getManufacturer();
        this.resolution = terminalInfo.getResolution();
        this.requestPath = terminalInfo.getRequestPath();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("logId=").append(logId);
        sb.append(", operateTime=").append(operateTime);
        sb.append(", module='").append(module).append('\'');
        sb.append(", operateType=").append(operateType);
        sb.append(", resourceId='").append(resourceId).append('\'');
        sb.append(", resourceType=").append(resourceType);
        sb.append(", resourceName='").append(resourceName).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", userId=").append(userId);
        sb.append(", username='").append(username).append('\'');
        sb.append(", userAvatar='").append(userAvatar).append('\'');
        sb.append(", roles='").append(roles).append('\'');
        sb.append(", detail='").append(detail).append('\'');
        sb.append(", location='").append(location).append('\'');
        sb.append(", province='").append(province).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", browser='").append(browser).append('\'');
        sb.append(", osSystem='").append(osSystem).append('\'');
        sb.append(", device='").append(device).append('\'');
        sb.append(", manufacturer='").append(manufacturer).append('\'');
        sb.append(", resolution='").append(resolution).append('\'');
        sb.append(", language='").append(language).append('\'');
        sb.append(", requestPath='").append(requestPath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}