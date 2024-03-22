/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.server.base.configuration;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.net.NetUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/6
 */
@Slf4j
public class SpringBeanAware implements BeanContextAware {


    public SpringBeanAware(SpringUtil springUtil) {
        BeanContextAwareUtils.setContextAware(this);
    }

    @Override
    public Resource[] getResources(String locationPattern) {
        return new Resource[0];
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return SpringUtil.getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        return SpringUtil.getBean(name,clazz);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return SpringUtil.getBeansOfType(clazz);
    }

    @Override
    public String applicationId() {
        return applicationContext().getId();
    }

    @Override
    public String getApplicationName() {
        return applicationContext().getApplicationName();
    }

    @Override
    public long getStartupDate() {
        return applicationContext().getStartupDate();
    }

    @Override
    public String[] activeProfiles() {
        return environment().getActiveProfiles();
    }

    @Override
    public void serviceShutdown() {
        ApplicationContext applicationContext =  SpringUtil.getApplicationContext();
        if(applicationContext instanceof ConfigurableApplicationContext){
            ((ConfigurableApplicationContext) applicationContext).close();
        }
    }

    private static ApplicationContext applicationContext(){
        return SpringUtil.getApplicationContext();
    }


    public static Environment environment() {
        Assert.notNull(applicationContext(), "application context is null.");
        return applicationContext().getEnvironment();
    }

    public String serverIpAddress() {
        Environment environment = environment();
        String ip = environment.getProperty("spring.cloud.client.ip-address");
        if(ip!=null){
            return ip;
        }else{
            ip = NetUtil.LOCAL_IP;
            log.warn("not have ip param value: {}, using localIP:{}","spring.cloud.client.ip-address",ip);
        }
        return ip;
    }


    public Integer serverPort() {
        Environment environment = environment();
        String port = environment.getProperty("server.port");
        if (port != null) {
            return Integer.valueOf(port);
        }
        return 0;
    }

}
