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

package io.innospots.server.base.registry;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.server.base.registry.enums.ServiceRole;
import io.innospots.server.base.registry.enums.ServiceStatus;
import io.innospots.server.base.registry.enums.ServiceType;
import lombok.Getter;
import lombok.Setter;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * service application will be update server node information in this table,
 * when these application has been started.
 *
 * @author Raydian
 * @date 2020/11/30
 */
@Entity
@Table(name = ServiceRegistryEntity.TABLE_NAME,
        indexes = {@Index(name = "idx_ip_port",columnList = "domainIp,port",unique = true)})
@TableName(value = ServiceRegistryEntity.TABLE_NAME)
@Setter
@Getter
public class ServiceRegistryEntity {

    public static final String TABLE_NAME = "sys_service_registry";

    /**
     * ServerNodeId
     */
    @Id
    @TableId
    @Column(length = 32)
    private Long serverId;

    /**
     * serviceName
     */
    @Column(length = 128)
    private String serviceName;

    /**
     * domain or ip address
     */
    @Column(length = 16)
    private String domainIp;

    /***
     * service port
     */
    @Column(length = 8)
    protected Integer port;

    /**
     * server status
     */
    @Column(nullable = false,length = 16)
//    @Enumerated(EnumType.STRING)
    private String serviceStatus;
//    private ServiceStatus serviceStatus;

    @Column(nullable = false,length = 32)
    private String serviceType;

    /**
     * serverRole
     */
    @Column(nullable = false,length = 16)
//    @Enumerated(EnumType.STRING)
//    private ServiceRole serviceRole;
    private String serviceRole;

    /**
     * set service group name
     */
    @Column(length = 16)
    private String groupName;

    @Column(length = 32)
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    @Column
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column
    private LocalDateTime updatedTime;

    @TableField(fill = FieldFill.INSERT)
    @Column(length = 32)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(length = 32)
    private String updateBy;

}
