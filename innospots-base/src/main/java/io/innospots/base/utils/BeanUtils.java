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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.innospots.base.exception.BaseException;
import io.innospots.base.model.response.ResponseCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * @program: innospots-root
 * @description: beanUtils
 * @author: Alexander
 * @create: 2021-01-22 22:27
 **/
@Slf4j
public class BeanUtils{

    public static void copyProperties(Object source, Object target) {
        BeanUtil.copyProperties(source,target, CopyOptions.create().ignoreNullValue());
        //org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));

    }

    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        return BeanUtil.copyProperties(source,targetClass);
    }

    /**
     * Batch of multiple object properties
     *
     * @param sourceCollection Source list
     * @param targetClass      Target class
     * @param <S>              source
     * @param <T>              target
     * @return Target collection
     */
    public static <S, T> List<T> copyProperties(Collection<S> sourceCollection, Class<T> targetClass) {
        if (CollectionUtils.isEmpty(sourceCollection)) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();
        try {
            for (S t : sourceCollection) {
                T entity = targetClass.newInstance();
                copyProperties(t, entity);
                result.add(entity);
            }
        } catch (Exception e) {
            log.error("An exception occurred in batch processing of multiple object properties", e);
            throw new BaseException(BeanUtils.class, ResponseCode.BEAN_CONVERT_ERROR, e, "An exception occurred in batch processing of multiple object properties");
        }
        return result;
    }



    public static Map<String, Object> toMap(Object object, boolean underscore, boolean ignoreNull) {
        return BeanUtil.beanToMap(object,underscore,ignoreNull);
    }

    public static Map<String, Object> toMap(Object object) {
        return BeanUtil.beanToMap(object,false,false);
    }


    public static <T> List<T> toBean(Collection<Map<String, Object>> beanCollection, Class<T> targetClass) {
        List<T> targets = new ArrayList<>();

        if (beanCollection != null && !beanCollection.isEmpty()) {
            for (Map<String, Object> beanMap : beanCollection) {
                targets.add(toBean(beanMap, targetClass));
            }
        }
        return targets;
    }

    public static <T> T toBean(Map<String, Object> beanMap, Class<T> valueType) {
        return toBean(beanMap, valueType, false);
    }

    public static <T> T toBean(Map<String, Object> beanMap, Class<T> valueType, boolean underscore) {
        return BeanUtil.mapToBean(beanMap,valueType,underscore,CopyOptions.create().ignoreError());
        //return bean;
    }

    public static <T> String getFieldName(FieldFunction<T, ?> fieldFunction, boolean isUnderscore) {
        try {
            Method funcMethod = fieldFunction.getClass().getDeclaredMethod("writeReplace");
            funcMethod.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) funcMethod.invoke(fieldFunction);
            String methodName = lambda.getImplMethodName();
            if (methodName.startsWith("get")) {
                methodName = methodName.substring(3);
            } else if (methodName.startsWith("is")) {
                methodName = methodName.substring(2);
            }
            if (StringUtils.isNotBlank(methodName)) {
                methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
                if (isUnderscore) {
                    methodName = StringConverter.camelToUnderscore(methodName);
                }
                return methodName;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    public interface FieldFunction<T, R> extends Function<T, R>, Serializable {
    }

}
