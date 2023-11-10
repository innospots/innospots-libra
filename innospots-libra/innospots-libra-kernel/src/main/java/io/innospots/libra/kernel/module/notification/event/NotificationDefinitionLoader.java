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

package io.innospots.libra.kernel.module.notification.event;

import io.innospots.libra.base.events.NotificationAnnotation;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.base.menu.ResourceItem;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/8/4
 */
public class NotificationDefinitionLoader {


    public static Map<String, NotificationGroup> load() {
        List<LibraExtensionProperties> appPropertiesList = new LibraClassPathExtPropertiesLoader().load(false);
        appPropertiesList.forEach(LibraExtensionProperties::fillI18n);
        Set<String> packageSet = appPropertiesList.stream().filter(f -> f.getBasePackages() != null && f.getBasePackages().length > 0).flatMap(f -> Arrays.stream(f.getBasePackages())).collect(Collectors.toSet());
        String[] packages = packageSet.toArray(new String[]{});
        Map<String, NotificationGroup> notifications = new LinkedHashMap<>();
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().forPackages(packages).setScanners(Scanners.MethodsAnnotated));

        Set<Method> methodSet = reflections.getMethodsAnnotatedWith(NotificationAnnotation.class);
        for (Method method : methodSet) {
            NotificationAnnotation eventTag = AnnotationUtils.findAnnotation(method, NotificationAnnotation.class);
            if (eventTag == null) {
                continue;
            }
            String event = StringUtils.isEmpty(eventTag.code()) ? method.getName() : eventTag.code();
            String moduleKey = StringUtils.isEmpty(eventTag.module()) ? method.getDeclaringClass().getSimpleName() : eventTag.module();
            LibraExtensionProperties extensionProperties = selectApp(appPropertiesList, method);
            if (extensionProperties != null) {
                NotificationGroup notificationGroup = notifications.getOrDefault(extensionProperties.getExtKey(), new NotificationGroup());
                notificationGroup.setExtName(extensionProperties.getName());
                notificationGroup.setExtKey(extensionProperties.getExtKey());
                notifications.putIfAbsent(notificationGroup.getExtKey(), notificationGroup);
                NotificationModule notificationModule = notificationGroup.findModule(moduleKey);
                if (notificationModule == null) {
                    notificationModule = new NotificationModule();
                    notificationModule.setModuleKey(moduleKey);
                    ResourceItem resourceItem = extensionProperties.getModules().stream().filter(item -> moduleKey.equals(item.getItemKey())).findFirst().orElse(null);
                    if (resourceItem != null) {
                        notificationModule.setModuleName(resourceItem.getName());
                    }
                    notificationGroup.addModule(notificationModule);
                }
                NotificationEvent notificationEvent = new NotificationEvent(notificationGroup.getExtName(), notificationGroup.getExtKey(),
                        notificationModule.getModuleName(), notificationModule.getModuleKey(),
                        eventTag.name(), event);
                notificationModule.addEvents(notificationEvent);
            }//end if
        }//end for

        return notifications;
    }

    private static LibraExtensionProperties selectApp(List<LibraExtensionProperties> libraAppProperties, Method method) {
        Class clazz = method.getDeclaringClass();
        String clazzName = clazz.getPackage().getName();
        for (LibraExtensionProperties libraAppProperty : libraAppProperties) {
            for (String basePackage : libraAppProperty.getBasePackages()) {
                if (clazzName.startsWith(basePackage)) {
                    return libraAppProperty;
                }
            }
        }//end for

        return null;
    }


}
