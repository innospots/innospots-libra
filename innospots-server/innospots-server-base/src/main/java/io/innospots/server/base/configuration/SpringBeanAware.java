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
import cn.hutool.extra.spring.SpringUtil;
import io.innospots.base.utils.BeanContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/6
 */
public class SpringBeanAware implements BeanContextAware {

    private SpringUtil springUtil;

    public SpringBeanAware(SpringUtil springUtil) {
        this.springUtil = springUtil;
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
        return SpringUtil.getApplicationContext().getId();
    }

    @Override
    public void serviceShutdown() {
        ApplicationContext applicationContext =  SpringUtil.getApplicationContext();
        if(applicationContext instanceof ConfigurableApplicationContext){
            ((ConfigurableApplicationContext) applicationContext).close();
        }
    }

    public static String serverIpAddress(){
        return null;
    }

    public static int serverPort(){
        return 0;
    }


}
