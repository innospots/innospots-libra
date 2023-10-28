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

package io.innospots.base.utils;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.lang.Assert;
import io.innospots.base.events.EventBody;
import io.innospots.base.events.EventBusCenter;

import java.util.Map;

/**
 * @author Raydian
 * @date 2020/12/15
 */
public class BeanContextAwareUtils {

    private static BeanContextAware contextAware;

    public static void setContextAware(BeanContextAware contextAware) {
        BeanContextAwareUtils.contextAware = contextAware;
    }

    public static boolean isLoaded(){
        return contextAware!=null;
    }

    public static BeanContextAware beanContextAware() {
        return contextAware;
    }

    public static Resource[] getResources(String locationPattern){
        Assert.notNull(contextAware, "application context is null.");
        return contextAware.getResources(locationPattern);
    }

    /**
     * send event
     *
     * @param eventBody
     */
    public static void sendAppEvent(EventBody eventBody) {
        EventBusCenter.getInstance().post(eventBody);
    }

    public static <T> T getBean(Class<T> clazz) {
        Assert.notNull(contextAware, "application context is null.");
        return beanContextAware().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        Assert.notNull(contextAware, "application context is null.");
        return beanContextAware().getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        Assert.notNull(contextAware, "application context is null.");
        return beanContextAware().getBeansOfType(clazz);
    }

    public static String applicationId() {
        return contextAware.applicationId();
    }

    public static void serviceShutdown() {
        contextAware.serviceShutdown();
    }
}
