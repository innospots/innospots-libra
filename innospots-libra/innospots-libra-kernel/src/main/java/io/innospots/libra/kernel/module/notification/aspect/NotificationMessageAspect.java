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

package io.innospots.libra.kernel.module.notification.aspect;


import io.innospots.base.utils.BeanContextAware;
import io.innospots.libra.base.event.MessageEvent;
import io.innospots.libra.base.event.NotificationAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * message aop aspect
 *
 * @author Smars
 */
@Slf4j
@Aspect
@Component
public class NotificationMessageAspect {

    @Pointcut("@annotation(io.innospots.libra.base.event.NotificationAnnotation)")
    public void controllerAspect() {

    }

    @Before("controllerAspect()&&@annotation(notificationAnnotation)")
    public void doBefore(JoinPoint joinPoint, NotificationAnnotation notificationAnnotation) {
        if (log.isDebugEnabled()) {
            log.debug("before advice：{}", joinPoint);
        }
    }

    /**
     * after advice, where is used to aspect the method that record log.
     */
    @AfterReturning(pointcut = "controllerAspect()", returning = "obj")
    public void after(JoinPoint joinPoint, Object obj) {
        try {
            String targetName = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            Object[] arguments = joinPoint.getArgs();
            Class targetClass = Class.forName(targetName);
            Method[] methods = targetClass.getMethods();
            NotificationAnnotation notificationAnnotation = null;
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Class[] clazz = method.getParameterTypes();
                    if (clazz.length == arguments.length) {
                        notificationAnnotation = method.getAnnotation(NotificationAnnotation.class);
                        break;
                    }
                }
            }
            if (notificationAnnotation == null) {
                return;
            }
            BeanContextAware.sendAppEvent(new MessageEvent(notificationAnnotation.code(), notificationAnnotation.title(),
                    notificationAnnotation.content(), notificationAnnotation.name(), notificationAnnotation.extName(), notificationAnnotation.module()));

        } catch (Exception e) {
            log.error("log exception: {}", e.getMessage());
        }
    }
}
