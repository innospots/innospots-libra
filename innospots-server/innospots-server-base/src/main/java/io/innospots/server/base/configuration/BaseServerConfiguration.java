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

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.base.watcher.WatcherStarter;
import io.innospots.base.watcher.WatcherSupervisor;
import io.innospots.server.base.exception.GlobalExceptionHandler;
import io.innospots.server.base.registry.ContextClosedEventListener;
import io.innospots.server.base.registry.ServiceRegistryDao;
import io.innospots.server.base.registry.ServiceRegistryManager;
import io.innospots.server.base.registry.ServiceRegistryStarter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.io.File;


/**
 * 应用服务基础配置bean
 *
 * @author Smars
 * @date 2021/6/21
 */
@MapperScan(basePackages = {"io.innospots.server.base.registry"})
@EntityScan(basePackages = {"io.innospots.server.base.registry"})
@Configuration
@EnableConfigurationProperties({InnospotsServerProperties.class})
@Import({CCH.class})
public class BaseServerConfiguration {

    /*
    @Bean
    public MeterBinder processMemoryMetrics() {
        return new ProcessMemoryMetrics();
    }

    @Bean
    public MeterBinder processThreadMetrics() {
        return new ProcessThreadMetrics();
    }

     */

    public static void buildPath(InnospotsServerProperties configProperties) {
        ScriptExecutorManager.setRetainSource(configProperties.isDebugMode());
        ScriptExecutorManager.setPath(configProperties.getScriptBuildPath() + File.separator + "src", configProperties.getScriptBuildPath());
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ServiceRegistryManager serviceRegistryManager(ServiceRegistryDao serviceRegistryDao) {
        return new ServiceRegistryManager(serviceRegistryDao);
    }


    @Bean
    public ServiceRegistryStarter serviceRegistryStarter(InnospotsServerProperties configProperties, WatcherSupervisor watcherSupervisor, ServiceRegistryManager serviceRegistryManager) {
        return new ServiceRegistryStarter(configProperties, watcherSupervisor, serviceRegistryManager);
    }

    @Bean
    public WatcherSupervisor watcherSupervisor(InnospotsServerProperties configProperties) {
        return new WatcherSupervisor(configProperties.getWatcherSize());
    }

    @Bean
    public WatcherStarter watcherStarter(WatcherSupervisor watcherSupervisor){
        return new WatcherStarter(watcherSupervisor);
    }

    @Bean
    public EventBusCenter eventBusCenter() {
        return EventBusCenter.getInstance();
    }

//    @Bean
//    public SpringUtil springUtil(){
//        return new SpringUtil();
//    }

    @Bean
    public BeanContextAware applicationContextUtils(SpringUtil springUtil) {
        return new SpringBeanAware(springUtil);
    }

    @Bean
    public InnospotsIdGenerator idGenerator(BeanContextAware springBeanAware) {
        return InnospotsIdGenerator.build(springBeanAware.serverIpAddress(), springBeanAware.serverPort());
    }

    @Bean
    @Primary
    public ObjectMapper jackson2ObjectMapper() {
        ObjectMapper objectMapper = JSONUtils.customMapper();
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addDeserializer(Registration.class,new RegistrationDeserializer());
//        objectMapper.registerModule(simpleModule);
//        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public ContextClosedEventListener contextClosedEventListener(){
        return new ContextClosedEventListener();
    }

}
