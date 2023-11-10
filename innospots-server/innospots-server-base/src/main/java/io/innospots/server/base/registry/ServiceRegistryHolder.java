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

import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.server.base.configuration.SpringBeanAware;
import io.innospots.server.base.registry.enums.ServiceRole;
import io.innospots.server.base.registry.enums.ServiceStatus;
import io.innospots.server.base.registry.enums.ServiceType;
import io.innospots.base.utils.BeanContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Raydian
 * @date 2020/12/14
 */
public class ServiceRegistryHolder {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryHolder.class);

    private static ServiceInfo serviceInfo;

    private static boolean leader;

    private static LocalDateTime startupTime;

    public static long maxValidTimeSecond = 55;

    private static ServiceType serviceType;

    private static int position = -1;

    private static int availableServicesSize = 0;

    private static boolean debugMode;

    public static void registerStartupTime() {
        startupTime = LocalDateTime.now();
    }

    public static void serverType(ServiceType serviceType) {
        ServiceRegistryHolder.serviceType = serviceType;
    }

    public static Integer[] currentShardingKeys(int maxShardingKeys) {
        if (position < 0 || availableServicesSize <= 0) {
            return null;
        }
        int cap = Math.round(maxShardingKeys * 1f / availableServicesSize);

        List<Integer> shardingKeys = new ArrayList<>();
        int s = position * cap;
        int e = (position + 1) == availableServicesSize ? maxShardingKeys : (position + 1) * cap;
        for (int i = s; i < e; i++) {
            shardingKeys.add(i);
        }
        return shardingKeys.toArray(new Integer[]{});
    }

    public static ServiceType serverType() {
        return serviceType;
    }

    public static int getAvailableServicesSize() {
        return availableServicesSize;
    }

    public static void setAvailableServicesSize(int availableServicesSize) {
        ServiceRegistryHolder.availableServicesSize = availableServicesSize;
    }

    public static int getPosition() {
        return position;
    }

    public static void setPosition(int position) {
        ServiceRegistryHolder.position = position;
    }

    public static ServiceInfo buildCurrentNewService() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setDomainIp(BeanContextAwareUtils.serverIpAddress());
        serviceInfo.setServiceType(serviceType);
        serviceInfo.setPort(BeanContextAwareUtils.serverPort());
        serviceInfo.setServiceRole(ServiceRole.FOLLOWER);
        serviceInfo.setServiceStatus(ServiceStatus.ONLINE);
        serviceInfo.setUpdatedTime(LocalDateTime.now());
        serviceInfo.setServiceName(BeanContextAwareUtils.applicationId());
        return serviceInfo;
    }

    /**
     * 节点已注册，并且更新时间有效,小于最小心跳
     *
     * @return
     */
    public static boolean isRegistry() {
        return serviceInfo != null;
    }

    public static LocalDateTime getStartupTime() {
        return startupTime;
    }

    /***
     * 注册 或 更新current serverInfo
     * @param serviceInfo
     */
    public static void register(ServiceInfo serviceInfo) {
        logger.debug("update service info:{}", serviceInfo);
        ServiceRegistryHolder.serviceInfo = serviceInfo;
        leader = ServiceRegistryHolder.serviceInfo.serviceRole == ServiceRole.LEADER;

    }

    public static void unregister() {
        serviceInfo = null;
    }

    public static void leader(boolean leader) {
        ServiceRegistryHolder.leader = leader;
    }


    /**
     * 主节点，并且更新时间有效
     *
     * @return
     */
    public static boolean isLeader() {
        return (debugMode && serviceInfo != null) || (leader && serviceInfo != null && serviceInfo.getUpdateIntervalSecond() < maxValidTimeSecond);
    }

    public static ServiceInfo getCurrentServer() {
        return serviceInfo;
    }

    /**
     * 判断节点是否已经失效
     */
    public static boolean isInvalidService(ServiceInfo serviceInfo) {
        return serviceInfo.getUpdateIntervalSecond() > maxValidTimeSecond;
    }

    public static void setDebugMode(boolean debugMode) {
        ServiceRegistryHolder.debugMode = debugMode;
    }
}
