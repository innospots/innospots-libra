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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.server.base.registry.enums.ServiceRole;
import io.innospots.server.base.registry.enums.ServiceStatus;
import io.innospots.server.base.registry.enums.ServiceType;
import io.innospots.base.utils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Raydian
 * @date 2020/12/14
 */
public class ServiceRegistryManager {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryManager.class);

    private ServiceRegistryDao serviceRegistryDao;

    public ServiceRegistryManager(ServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;
    }

    public List<ServiceInfo> listOnlineServices(ServiceType serviceType) {
        QueryWrapper<ServiceRegistryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ServiceRegistryEntity::getServiceStatus, ServiceStatus.ONLINE.name())
                .eq(ServiceRegistryEntity::getServiceType, serviceType.name());
        List<ServiceRegistryEntity> registryEntities = serviceRegistryDao.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(registryEntities)) {
            return BeanUtils.copyProperties(registryEntities, ServiceInfo.class);
        }

        return Collections.emptyList();
    }

    public List<ServiceInfo> listOnlineServices() {
        QueryWrapper<ServiceRegistryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ServiceRegistryEntity::getServiceStatus, ServiceStatus.ONLINE.name());
        List<ServiceRegistryEntity> registryEntities = serviceRegistryDao.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(registryEntities)) {
            return BeanUtils.copyProperties(registryEntities, ServiceInfo.class);
        }

        return Collections.emptyList();
    }

    public ServiceInfo registry(ServiceInfo serviceInfo) {

        QueryWrapper<ServiceRegistryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("domain_ip", serviceInfo.getDomainIp());
        queryWrapper.eq("port", serviceInfo.getPort());
        ServiceRegistryEntity registryEntity = serviceRegistryDao.selectOne(queryWrapper);
        LocalDateTime now = LocalDateTime.now();
        if (registryEntity != null) {
            registryEntity.setServiceStatus(ServiceStatus.ONLINE);
            registryEntity.setUpdatedTime(now);
            serviceRegistryDao.updateById(registryEntity);
            serviceInfo = BeanUtils.copyProperties(registryEntity, ServiceInfo.class);
        } else {
            registryEntity = BeanUtils.copyProperties(serviceInfo, ServiceRegistryEntity.class);
            registryEntity.setCreatedTime(now);
            registryEntity.setUpdatedTime(now);
            registryEntity.setCreatedBy("service");
            serviceRegistryDao.insert(registryEntity);
            serviceInfo = BeanUtils.copyProperties(registryEntity, ServiceInfo.class);
        }
        return serviceInfo;
    }

    public ServiceInfo offline(Long serviceId) {
        ServiceRegistryEntity registryEntity = serviceRegistryDao.selectById(serviceId);
        if (registryEntity == null) {
            logger.warn("service not exist,id:{}", serviceId);
            return null;
        }

        registryEntity.setServiceStatus(ServiceStatus.OFFLINE);
        registryEntity.setUpdatedTime(LocalDateTime.now());
        serviceRegistryDao.updateById(registryEntity);
        return BeanUtils.copyProperties(registryEntity, ServiceInfo.class);
    }

    public ServiceInfo heartbeat(Long serviceId) {
        ServiceRegistryEntity registryEntity = serviceRegistryDao.selectById(serviceId);

        if (registryEntity == null) {
            logger.warn("serverInfo is null");
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        serviceRegistryDao.updateTime(serviceId, ServiceStatus.ONLINE, now);
        registryEntity.setUpdatedTime(now);
        registryEntity.setServiceStatus(ServiceStatus.ONLINE);
        return BeanUtils.copyProperties(registryEntity, ServiceInfo.class);
    }

    public int updateServiceRole(Long serviceId, ServiceRole serviceRole) {
        ServiceRegistryEntity registryEntity = serviceRegistryDao.selectById(serviceId);

        if (registryEntity == null) {
            logger.warn("serverInfo is null");
            return 0;
        }
        registryEntity.setServiceRole(serviceRole);
        registryEntity.setUpdatedTime(LocalDateTime.now());
        return serviceRegistryDao.updateById(registryEntity);
    }

}
