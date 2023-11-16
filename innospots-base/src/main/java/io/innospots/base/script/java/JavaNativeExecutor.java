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

package io.innospots.base.script.java;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/4/2
 */
public class JavaNativeExecutor implements IScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JavaNativeExecutor.class);

    private Method method;
    private String[] arguments;


    private void reloadArgs(Method method) {
        this.arguments = new String[method.getParameterAnnotations().length];
        this.method = method;
        for (int i = 0; i < method.getParameterAnnotations().length; i++) {
            Annotation[] annotations = method.getParameterAnnotations()[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof ParamName) {
                    this.arguments[i] = ((ParamName) annotation).value();
                    break;
                }
            }
            if (this.arguments[i] == null) {
                logger.warn("method parameter not set ParamName Annotation: {}", method.getName());
                this.arguments[i] = method.getParameters()[i].getName();
            }
        }
    }

    @Override
    public void initialize(Method method) {
        this.method = method;
        this.reloadArgs(method);
    }

    @Override
    public ExecuteMode executeMode() {
        return ExecuteMode.NATIVE;
    }

    @Override
    public String scriptType() {
        return "java";
    }

    @Override
    public String suffix() {
        return "java";
    }

    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
        Object[] values = null;
        if (arguments.length == 0 || (arguments.length == 1 && "item".equals(arguments[0]))) {
            values = new Object[1];
            values[0] = env;
        } else {
            values = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                values[i] = env.get(arguments[i]);
            }
        }
        return execute(values);
    }

    @Override
    public Object execute(Object... args) throws ScriptException {
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            throw ScriptException.buildInvokeException(this.getClass(), ScriptType.JAVA, e, e.getMessage());
        }
    }

    @Override
    public String[] arguments() {
        return arguments;
    }
}
