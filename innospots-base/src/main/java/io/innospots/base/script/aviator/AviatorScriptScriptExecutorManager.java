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

package io.innospots.base.script.aviator;

import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.IScriptExecutorManager;
import io.innospots.base.script.jit.MethodBody;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 脚本表达式引擎执行
 *
 * @author Smars
 * @date 2021/8/11
 */
public class AviatorScriptScriptExecutorManager implements IScriptExecutorManager {

    private static final Logger logger = LoggerFactory.getLogger(AviatorScriptScriptExecutorManager.class);

    private String identifier;

    private Map<String, AviatorScriptExecutor> expressionMap = new HashMap<>();

    public static AviatorScriptScriptExecutorManager build(String identifier) {
        AviatorScriptScriptExecutorManager expressionEngine = new AviatorScriptScriptExecutorManager();
        expressionEngine.identifier = identifier;
        return expressionEngine;
    }

    public static AviatorScriptExecutor scriptExpression(Method method) {
        AviatorScriptExecutor expression = null;
        try {
            String scriptBody = (String) method.invoke(null);
            expression = new AviatorScriptExecutor(scriptBody, new String[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

        return expression;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public IScriptExecutor getExecutor(String methodName) {
        return expressionMap.get(methodName);
    }

    @Override
    public void reload() throws ScriptException {

    }

    @Override
    public boolean build() throws ScriptException {
        return true;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void clear() {

    }

    /*
    @Override
    public void register(Class<?> returnType, String methodName, String srcBody, List<ParamField> params) {
        if (CollectionUtils.isNotEmpty(params)) {
            register(returnType, methodName, srcBody, params.toArray(new ParamField[]{}));
        } else {
            register(returnType, methodName, srcBody, new ParamField[0]);
        }
    }

    @Override
    public void register(Class<?> returnType, String methodName, String srcBody, ParamField... params) {
        String[] args = null;
        if (params != null) {
            args = Arrays.stream(params)
                    .map(ParamField::getCode).toArray(String[]::new);
        } else {
            args = new String[0];
        }
        AviatorScriptExecutor expression = new AviatorScriptExecutor(srcBody, args);
        expressionMap.put(methodName, expression);
    }

    @Override
    public void register(Class<?> returnType, String methodName, String srcBody) {
        register(returnType, methodName, srcBody, new ParamField[0]);
    }

     */

    @Override
    public void register(MethodBody methodBody) {
       // register(methodBody.getReturnType(), methodBody.getMethodName(), methodBody.getSrcBody(), methodBody.getParams());
    }
}
