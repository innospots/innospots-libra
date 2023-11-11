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

package io.innospots.libra.kernel.aspect;

import io.innospots.base.exception.ValidatorException;
import io.innospots.base.i18n.LocaleMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alfred
 * @date 2021-03-12
 */

//@Slf4j
//@Aspect
//@Component
@Deprecated
public class ValidatorAspect {

    @Pointcut(
            "execution(public * io.innospots.*.*.controller.*.*(..)) && args(..,org.springframework.validation.BindingResult) " +
                    "|| " +
                    "execution(public * io.innospots.libra.kernel.module.*.controller.*.*(..)) && args(..,org.springframework.validation.BindingResult) "
    )
    public void validMethod() {
    }

    @Before("validMethod()")
    public void doBefore(JoinPoint joinPoint) {
        Object[] paramValues = joinPoint.getArgs();

        BindingResult bindingResult = null;
        for (Object value : paramValues) {
            if (value instanceof BindingResult) {
                bindingResult = (BindingResult) value;
                break;
            }
        }

        if (bindingResult != null) {
            if (bindingResult.hasErrors()) {
                throw buildInvalidException(bindingResult);
            }
        }
    }

    private ValidatorException buildInvalidException(BindingResult bindingResult) {
        List<String> messages = new ArrayList<>();
        Set<String> modules = new LinkedHashSet<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            Object[] args;
            if (fieldError.getArguments() != null) {
                args = new Object[fieldError.getArguments().length + 2];
                for (int i = 1; i < fieldError.getArguments().length; i++) {
                    args[i + 2] = fieldError.getArguments()[i];
                }
            } else {
                args = new Object[2];
            }
            //bean name
            args[0] = fieldError.getObjectName();
            //bean field
            args[1] = fieldError.getField();
            //bean value
            args[2] = fieldError.getRejectedValue();
            modules.add(String.valueOf(args[0]));
            String msg = null;
            //codes definitions: ValidType.beanName.fieldName,ValidType.fieldName,ValidType.javaType,ValidType
            //codes: NotBlank.datasource.dyType,NotBlank.dbType, NotBlank.java.util.String, NotBlank
            if (fieldError.getCodes() != null) {
                for (String code : fieldError.getCodes()) {
                    msg = LocaleMessageUtils.message(code, args, "");
                    if (StringUtils.isNotEmpty(msg)) {
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(msg)) {
                msg = fieldError.getDefaultMessage();
            }
            messages.add(msg);
        }//end for
        return ValidatorException.buildInvalidException(String.join(",", modules),
                String.join(",", messages));
    }

}
