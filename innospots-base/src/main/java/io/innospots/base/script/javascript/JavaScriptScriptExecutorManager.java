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

package io.innospots.base.script.javascript;/**
 * @author Smars
 * @date 2021/4/2
 */

import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.IScriptExecutorManager;
import io.innospots.base.script.jit.MethodBody;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaScriptScriptExecutorManager implements IScriptExecutorManager {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptScriptExecutorManager.class);

    private static ScriptEngine scriptEngine;

    private String identifier;

    private Map<String, JavaScriptScriptExecutor> expressionMap = new HashMap<>();

    static {
        scriptEngine = scriptEngine();
    }

    private static ScriptEngine scriptEngine() {
        if (scriptEngine == null) {
            ScriptEngineManager engineManager = new ScriptEngineManager();
            scriptEngine = engineManager.getEngineByName("javascript");
        }
        return scriptEngine;
    }

    public static JavaScriptScriptExecutorManager build(String identifier) {
        JavaScriptScriptExecutorManager expressionEngine = new JavaScriptScriptExecutorManager();
        expressionEngine.identifier = identifier;
        return expressionEngine;
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
    public void reload() {

    }

    @Override
    public boolean build() {

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
    public void register(Class<?> returnType, String methodName, String srcBody, ParamField... params) {
        String srcScript = buildMethodBody(returnType, methodName, srcBody, params);
        JavaScriptScriptExecutor expression = buildExpression(srcScript);
        if (expression != null) {
            expressionMap.put(methodName, expression);
        }
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



    public static JavaScriptScriptExecutor scriptExpression(Method method) {
        JavaScriptScriptExecutor expression = null;
        try {
            String scriptBody = (String) method.invoke(null);
            expression = buildExpression(scriptBody);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

        return expression;
    }


    public static JavaScriptScriptExecutor buildExpression(String methodBody) {
        Compilable compilable = (Compilable) scriptEngine();
        JavaScriptScriptExecutor scriptExpression = null;
        try {
            CompiledScript compiledScript = compilable.compile(methodBody);
            String[] args = methodBody.substring(methodBody.indexOf("(") + 1, methodBody.indexOf(")")).split(",");
            scriptExpression = new JavaScriptScriptExecutor(compiledScript, args);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return scriptExpression;
    }


    public static String buildMethodBody(Class<?> returnType, String methodName, String srcBody, ParamField... params) {

        String args = null;
        if (params == null || params.length == 0) {
            args = "item";
        } else {
            args = params[0].getCode();
        }
        srcBody = srcBody.replaceAll("\n", "\\\\n");
        srcBody = srcBody.replaceAll("\"", "\\\\\"");
        String source = "";
        source += "function ";
        source += methodName;
        source += " (";
        source += args;
        source += "){";
        source += srcBody;
        source += "}\\n";
        //return string
        source += "JSON.stringify(";
        source += methodName;
        source += "(";
        source += "JSON.parse(";
        source += args;
        source += ")";
        source += ")";
        source += ")";
        System.out.println(source);
        return source;
        //return srcBody;
    }


}
