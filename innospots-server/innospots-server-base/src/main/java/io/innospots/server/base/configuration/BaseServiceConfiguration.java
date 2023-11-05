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

package io.innospots.server.base.configuration;

import io.innospots.base.events.EventBusCenter;
import io.innospots.server.base.exception.GlobalExceptionHandler;
import io.innospots.base.function.definition.FunctionDefinitionOperator;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.registry.ServiceRegistryDao;
import io.innospots.base.registry.ServiceRegistryManager;
import io.innospots.base.registry.ServiceRegistryStarter;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.InnospotIdGenerator;
import io.innospots.base.watcher.WatcherSupervisor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


/**
 * 应用服务基础配置bean
 *
 * @author Smars
 * @date 2021/6/21
 */
@MapperScan(basePackages = {"io.innospots.server.base.registry"})
@EntityScan(basePackages = {"io.innospots.server.base.registry"})
@Configuration
@Import({CCH.class})
public class BaseServiceConfiguration {


    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ServiceRegistryManager serviceRegistryManager(ServiceRegistryDao serviceRegistryDao) {
        return new ServiceRegistryManager(serviceRegistryDao);
    }

    @Bean
    public FunctionDefinitionOperator functionDefinitionOperator() {
        return new FunctionDefinitionOperator();
    }

    @Bean
    public ServiceRegistryStarter serviceRegistryStarter(InnospotConfigProperties configProperties, WatcherSupervisor watcherSupervisor, ServiceRegistryManager serviceRegistryManager) {
        return new ServiceRegistryStarter(configProperties, watcherSupervisor, serviceRegistryManager);
    }

    @Bean
    public WatcherSupervisor watcherSupervisor(InnospotConfigProperties innospotConfigProperties) {
        return new WatcherSupervisor(innospotConfigProperties.getWatcherSize());
    }

    @Bean
    public EventBusCenter eventBusCenter() {
        return EventBusCenter.getInstance();
    }

    @Bean
    public BeanContextAware applicationContextUtils() {
        return new BeanContextAware();
    }

    @Bean
    public InnospotIdGenerator idGenerator(BeanContextAware beanContextAware) {
        return InnospotIdGenerator.build(BeanContextAware.serverIpAddress(), BeanContextAware.serverPort());
    }


    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {

        return JSONUtils.customBuilder();
    }


}
