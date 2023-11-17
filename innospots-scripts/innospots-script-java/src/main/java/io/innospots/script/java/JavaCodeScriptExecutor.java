/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.script.java;

import cn.hutool.core.annotation.AnnotationUtil;
import io.innospots.base.model.Pair;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.java.ScriptMeta;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IScriptEvaluator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/18
 */
@Slf4j
public class JavaCodeScriptExecutor implements IScriptExecutor {

    private String[] arguments;

    private IScriptEvaluator scriptEvaluator;

    @Override
    public void initialize(Method method) {
        ScriptMeta scriptMeta = AnnotationUtil.getAnnotation(method, ScriptMeta.class);
        try {
            scriptEvaluator = CompilerFactoryFactory.getDefaultCompilerFactory(this.getClass().getClassLoader()).newScriptEvaluator();
            this.arguments = scriptMeta.args();
            scriptEvaluator.setReturnType(scriptMeta.returnType());
            Pair<Class<?>,String>[] argsPair = this.argsPair(scriptMeta.args());
            String[] params = new String[argsPair.length];
            Class[] classes = new Class[argsPair.length];
            for (int i = 0; i < argsPair.length; i++) {
                params[i] = argsPair[i].getRight();
                classes[i] = argsPair[i].getLeft();
            }
            scriptEvaluator.setParameters(params, classes);
            String scriptBody = (String) method.invoke(null);
            scriptEvaluator.cook(scriptBody);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExecuteMode executeMode() {
        return ExecuteMode.SCRIPT;
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
    public Object execute(Map<String, Object> env) {
        try {
            return scriptEvaluator.evaluate(env);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] arguments() {
        return arguments;
    }
}
