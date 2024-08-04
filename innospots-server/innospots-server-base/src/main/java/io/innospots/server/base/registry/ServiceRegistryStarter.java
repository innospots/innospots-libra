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
import io.innospots.base.watcher.WatcherSupervisor;
import io.innospots.server.base.configuration.BaseServerConfiguration;
import io.innospots.server.base.configuration.InnospotsServerProperties;
import io.innospots.server.base.registry.enums.ServiceRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;


/**
 * @author Raydian
 * @date 2020/12/14
 */
@Slf4j
@Order(1)
public class ServiceRegistryStarter implements ApplicationRunner {


    private final ServiceRegistryManager serviceRegistryManager;

    private final WatcherSupervisor watcherSupervisor;

    private final InnospotsServerProperties configProperties;


    public ServiceRegistryStarter(
            InnospotsServerProperties configProperties,
            WatcherSupervisor watcherSupervisor,
            ServiceRegistryManager serviceRegistryManager) {
        this.watcherSupervisor = watcherSupervisor;
        this.serviceRegistryManager = serviceRegistryManager;
        this.configProperties = configProperties;
    }

    @Override
    public void run(ApplicationArguments args) {

        try {
            BaseServerConfiguration.buildPath(configProperties);
            ServiceRegistryHolder.setDebugMode(configProperties.isDebugMode());
            ServiceInfo serviceInfo = ServiceRegistryHolder.buildCurrentNewService();
            if (configProperties.isEnableRegistry()) {
                serviceInfo = serviceRegistryManager.registry(serviceInfo);
                ServiceRegistryWatcher serviceRegistryWatcher = new ServiceRegistryWatcher(serviceRegistryManager);
                watcherSupervisor.registry(serviceRegistryWatcher);
            } else {
                log.warn("not open service registry watcher and register service info to registry table:{}", serviceInfo);
                serviceInfo.setServiceRole(ServiceRole.LEADER);
            }
            ServiceRegistryHolder.register(serviceInfo);
            ServiceRegistryHolder.registerStartupTime();
            log.info("service has registered: {}",serviceInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            BeanContextAwareUtils.serviceShutdown();
        }

    }

    public void close() {
        if (serviceRegistryManager != null && configProperties.isEnableRegistry()) {
            try {
                if (ServiceRegistryHolder.isRegistry()) {
                    log.info("offline service:{}", ServiceRegistryHolder.getCurrentServer());
                    serviceRegistryManager.offline(ServiceRegistryHolder.getCurrentServer().getServerId());
                    ServiceRegistryHolder.unregister();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

}
