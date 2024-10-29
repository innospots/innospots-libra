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

package io.innospots.script.base.aviator;

import cn.hutool.core.annotation.AnnotationUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import io.innospots.base.model.Pair;
import io.innospots.script.base.ExecuteMode;
import io.innospots.script.base.IScriptExecutor;
import io.innospots.script.base.java.ScriptMeta;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * aviator 表达式引擎编译器
 *
 * @author Smars
 * @date 2021/8/11
 */
@Slf4j
public class AviatorExpressionExecutor implements IScriptExecutor {

    private Expression expression;

    private String[] arguments;

    public AviatorExpressionExecutor() {
    }

    public AviatorExpressionExecutor(String[] arguments,String statement) {
        this.arguments = arguments;
        expression = AviatorEvaluator.compile(statement, true);
    }

    public AviatorExpressionExecutor(String statement) {
        expression = AviatorEvaluator.compile(statement, true);
    }

    @Override
    public void initialize(Method method) {
        if(expression!=null){
            return;
        }
        ScriptMeta scriptMeta = AnnotationUtil.getAnnotation(method, ScriptMeta.class);
        try {
            String scriptBody = (String) method.invoke(null);
            expression = AviatorEvaluator.compile(scriptBody, true);
            if(scriptMeta!=null){
                Pair<Class<?>,String>[] pairs = this.argsPair(scriptMeta.args());
                arguments = Arrays.stream(pairs).map(Pair::getRight).collect(Collectors.toList()).toArray(String[]::new);
            }
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public ExecuteMode executeMode() {
        return ExecuteMode.SCRIPT;
    }

    @Override
    public String scriptType() {
        return "aviator";
    }

    @Override
    public String suffix() {
        return "as";
    }

    @Override
    public Object execute(Map<String, Object> env) {
        return expression.execute(env);
    }

    @Override
    public String[] arguments() {
        return arguments;
    }
}
