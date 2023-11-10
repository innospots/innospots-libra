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

package io.innospots.libra.kernel.module.logger.aspect;


import cn.hutool.core.bean.BeanUtil;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.base.model.user.UserInfo;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.user.SysUserReader;
import io.innospots.libra.base.terminal.TerminalRequestContextHolder;
import io.innospots.libra.kernel.module.logger.entity.SysOperateLogEntity;
import io.innospots.libra.kernel.module.logger.operator.SysLogOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * logger aop aspect
 *
 * @author Smars
 */
@Slf4j
@Aspect
@Component
public class OperateLogAspect {

    private final SysLogOperator sysLogOperator;
    private final SysUserReader sysUserReader;


    public OperateLogAspect(SysLogOperator sysLogOperator, SysUserReader sysUserReader) {
        this.sysLogOperator = sysLogOperator;
        this.sysUserReader = sysUserReader;
    }

    @Pointcut("@annotation(io.innospots.libra.base.log.OperationLog)")
    public void controllerAspect() {

    }

    @Before("controllerAspect()&&@annotation(operationLog)")
    public void doBefore(JoinPoint joinPoint, OperationLog operationLog) {
        if (log.isDebugEnabled()) {
            log.debug("before advice：{}", joinPoint);
        }
    }

    @AfterThrowing(pointcut = "@annotation(operationLog)", throwing = "e")
    public void throwError(JoinPoint joinPoint, OperationLog operationLog, Throwable e) {
        saveRecord(joinPoint, operationLog, null, e);
    }

    /**
     * after advice, where is used to aspect the method that record log.
     */
    @AfterReturning(pointcut = "@annotation(operationLog)", returning = "obj")
    public void after(JoinPoint joinPoint, OperationLog operationLog, Object obj) {
        saveRecord(joinPoint, operationLog, obj, null);
    }

    private void saveRecord(JoinPoint joinPoint, OperationLog operationLog, Object obj, Throwable throwable) {
        try {
            String targetName = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            Class targetClass = Class.forName(targetName);
            ModuleMenu moduleMenu = AnnotationUtils.findAnnotation(targetClass, ModuleMenu.class);
            String resourceName = targetClass.getSimpleName().replace("Controller", "");

            SysOperateLogEntity operateLog = new SysOperateLogEntity();
            String resourceId = this.getResourceIdByParam(joinPoint, operationLog);
            if (resourceId == null) {
                resourceId = this.getResourceIdByResult(operationLog, obj);
            }

            operateLog.setResourceId(resourceId);

            if (moduleMenu != null) {
                operateLog.setModule(moduleMenu.menuKey());
            }
            operateLog.setResourceName(resourceName);
//            operateLog.setResourceType(operationLog.resourceType());

            operateLog.setOperateType(operationLog.operateType());

            if (throwable != null) {
                operateLog.setDetail(methodName + ", msg:" + throwable.getMessage() + ", exception:" + throwable.getClass().getSimpleName());
            } else {
                operateLog.setDetail(methodName);
            }
            if (operateLog.getDetail().length() > 255) {
                operateLog.setDetail(operateLog.getDetail().substring(0, 249) + "...");
            }
            operateLog.setUsername(CCH.authUser());
            Integer userId = CCH.userId();
            operateLog.setUserId(userId);
            operateLog.setOperateTime(LocalDateTime.now());
            operateLog.fill(TerminalRequestContextHolder.getTerminal());

            UserInfo userInfo = sysUserReader.getUserInfo(userId);
            if (userInfo != null) {
                operateLog.setRoles(String.join(",", userInfo.getRoleNames()));
                operateLog.setUserAvatar(userInfo.getAvatarKey());
            }

            sysLogOperator.save(operateLog);
            if (log.isDebugEnabled()) {
                log.debug("operate log:{}", operationLog);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * get resourceId by result
     *
     * @param operationLog
     * @param obj
     * @return
     */
    private String getResourceIdByResult(OperationLog operationLog, Object obj) {
        String resourceId = null;
        if (StringUtils.isNotBlank(operationLog.primaryField()) && obj != null) {
            Object data = obj;
            if (obj instanceof InnospotResponse) {
                data = ((InnospotResponse<?>) obj).getBody();
            }
            Object value = BeanUtil.getFieldValue(data, operationLog.primaryField());
            if (value != null) {
                resourceId = value.toString();
            }
        }
        return resourceId;
    }

    /**
     * get resourceId by param
     *
     * @param joinPoint
     * @param operationLog
     * @return
     */
    private String getResourceIdByParam(JoinPoint joinPoint, OperationLog operationLog) {
        String resourceId = null;

        if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0 && operationLog.idParamPosition() >= 0) {
            try {
                Object obj = joinPoint.getArgs()[operationLog.idParamPosition()];
                if (obj instanceof Number) {
                    resourceId = obj.toString();
                } else if (StringUtils.isNotBlank(operationLog.primaryField())) {
                    obj = BeanUtil.getFieldValue(obj, operationLog.primaryField());
                    if (obj instanceof Number) {
                        resourceId = obj.toString();
                    } else if (obj != null) {
                        resourceId = obj.toString();
                    }
                } else if (obj != null) {
                    resourceId = obj.toString();
                }
            } catch (Exception e) {
            }
        }
        return resourceId;
    }
}