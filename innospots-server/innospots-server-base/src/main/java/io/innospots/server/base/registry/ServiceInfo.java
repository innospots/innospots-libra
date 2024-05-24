/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License; you may not use this file except in compliance with
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.model.BaseModelInfo;
import io.innospots.server.base.registry.enums.ServiceRole;
import io.innospots.server.base.registry.enums.ServiceStatus;
import io.innospots.server.base.registry.enums.ServiceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * @author Raydian
 * @date 2020/11/29
 */
@Getter
@Setter
public class ServiceInfo extends BaseModelInfo implements Comparable<ServiceInfo> {


    //server node primary id
    protected Long serverId;

    //service name
    protected String serviceName;

    //server status
    protected ServiceStatus serviceStatus;

    //ip address
    protected String domainIp;

    //port
    protected Integer port;

    //server type, app,schedule,executor
    protected String serviceType;

    //server role
    protected ServiceRole serviceRole;

    protected String groupName;

    protected String tags;

//    @ApiModelProperty("startup time
//    @JsonFormat(
//            pattern = "yyyy-MM-dd HH:mm:ss"
//    )
//    protected LocalDateTime createTime;
//
//    @ApiModelProperty("update time
//    @JsonFormat(
//            pattern = "yyyy-MM-dd HH:mm:ss"
//    )
//    private LocalDateTime updateTime;

    @Override
    public int compareTo(ServiceInfo o) {
        if (Objects.nonNull(createdTime) && Objects.nonNull(o.createdTime)) {
            return createdTime.compareTo(o.createdTime);
        } else {
            return Long.compare(serverId, o.serverId);
        }
    }

    public String getServerKey() {
        return this.domainIp + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInfo that = (ServiceInfo) o;
        return domainIp.equals(that.domainIp) && port.equals(that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainIp, port);
    }

    /**
     * @return
     */
    @JsonIgnore
    public long getUpdateIntervalSecond() {
        long intervalSecond = 0L;
        if (Objects.nonNull(updatedTime)) {
            intervalSecond = LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()) - updatedTime.toEpochSecond(OffsetDateTime.now().getOffset());
        }
        return intervalSecond;
    }

    @Override
    public String toString() {
        return "{" + "serviceName='" + serviceName + '\'' +
                ", domainIp='" + domainIp + '\'' +
                ", port=" + port +
                ", serviceStatus=" + serviceStatus +
                ", serviceType=" + serviceType +
                ", serviceRole=" + serviceRole +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
