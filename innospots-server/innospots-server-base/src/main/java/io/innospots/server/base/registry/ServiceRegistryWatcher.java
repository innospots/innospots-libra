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

import io.innospots.base.utils.ServiceActionHolder;
import io.innospots.server.base.registry.enums.ServiceRole;
import io.innospots.base.watcher.AbstractWatcher;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2021/1/14
 */
public class ServiceRegistryWatcher extends AbstractWatcher {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryWatcher.class);

    private ServiceRegistryManager serviceRegistryManager;

    public ServiceRegistryWatcher(ServiceRegistryManager serviceRegistryManager) {
        this.serviceRegistryManager = serviceRegistryManager;
    }

    @Override
    public boolean check() {
        return ServiceRegistryHolder.isRegistry();
    }

    /**
     * service alive check
     *
     * @return
     */
    @Override
    public int execute() {

        if (ServiceRegistryHolder.isRegistry()) {
            heartbeat();
        }

        checkAvailableService();
        if(ServiceRegistryHolder.getCurrentServer()!=null){
            ServiceActionHolder.setServiceLeader(ServiceRegistryHolder.isLeader());
            ServiceActionHolder.setServiceType(ServiceRegistryHolder.serverType());
            ServiceActionHolder.setServerKey(ServiceRegistryHolder.getCurrentServer().getServerKey());
        }
        return checkIntervalSecond;
    }

    @Override
    protected void prepare() {
        super.prepare();
    }

    private void heartbeat() {
        try {
            ServiceInfo serviceInfo = ServiceRegistryHolder.getCurrentServer();
            serviceInfo = serviceRegistryManager.heartbeat(serviceInfo.getServerId());

            if (Objects.isNull(serviceInfo)) {
                return;
            }
            ServiceRegistryHolder.register(serviceInfo);
            if (logger.isDebugEnabled()) {
                logger.debug("service status heartbeat, serverInfo:{}", serviceInfo);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * check all available service
     * 检查所有有效节点状态，每个节点都检查，避免单点问题
     */
    private void checkAvailableService() {
        //查找所有有效节点
        List<ServiceInfo> serviceInfos = serviceRegistryManager.listOnlineServices(ServiceRegistryHolder.serverType());
        if (CollectionUtils.isEmpty(serviceInfos)) {
            logger.error("not have online status service, serviceType:{}", ServiceRegistryHolder.serverType());
            return;
        }
        //按启动时间从早到晚排序，最先启动的排在前面
        Collections.sort(serviceInfos);

        //检查主节点
        checkServiceRole(serviceInfos);

        //检查节点超时时间
        for (ServiceInfo serviceInfo : serviceInfos) {
            //如果节点失效
            if (ServiceRegistryHolder.isInvalidService(serviceInfo)) {
                serviceRegistryManager.offline(serviceInfo.getServerId());
                logger.info("the status of service is updated to offline,{}", serviceInfo);
            }
        }

        if (ServiceRegistryHolder.isRegistry()) {
            serviceInfos.sort(Comparator.comparing(ServiceInfo::getServerKey));
            ServiceRegistryHolder.setPosition(serviceInfos.indexOf(ServiceRegistryHolder.getCurrentServer()));
            ServiceRegistryHolder.setAvailableServicesSize(serviceInfos.size());
        }

    }

    /**
     * 检查主节点是否存在
     *
     * @param serviceInfos
     */
    private void checkServiceRole(List<ServiceInfo> serviceInfos) {
        //check leader service exist or not
        List<ServiceInfo> leaderServices = serviceInfos.stream().filter(
                        item -> Objects.equals(item.getServiceType(), ServiceRegistryHolder.serverType()) &&
                                item.getServiceRole() == ServiceRole.LEADER)
                .collect(Collectors.toList());

        if (leaderServices.isEmpty()) {
            //leader is not exist
            ServiceInfo leader = serviceInfos.get(0);
            leader.setServiceRole(ServiceRole.LEADER);
            logger.warn("leader service is not register, using least createdTime serverNode as Leader: {}", leader);

            serviceRegistryManager.updateServiceRole(leader.getServerId(), ServiceRole.LEADER);
            //如果current service is leader，update ServiceRegistryHolder
            if (ServiceRegistryHolder.getCurrentServer() != null &&
                    ServiceRegistryHolder.getCurrentServer().getServerId().equals(leader.getServerId())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("current service：{}", leader);
                }
                ServiceRegistryHolder.register(leader);
            }
        } else if (leaderServices.size() > 1) {
            logger.error("the more than one leader in the system，please check sys_service_registry, {}", leaderServices);
            Collections.sort(leaderServices);
            //关闭多余主节点，此部分逻辑走不到，作为安全保障处理
            for (int i = 1; i < leaderServices.size(); i++) {
                ServiceInfo serviceInfo = leaderServices.get(i);
                serviceInfo.setServiceRole(ServiceRole.FOLLOWER);
                serviceRegistryManager.updateServiceRole(serviceInfo.getServerId(), ServiceRole.FOLLOWER);
                logger.info("set service role to follower:{}", serviceInfo);
            }
        }
    }


}
