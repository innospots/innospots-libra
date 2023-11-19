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

package io.innospots.script.graaljs;

import cn.hutool.core.annotation.AnnotationUtil;
import io.innospots.base.model.Pair;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.java.ScriptMeta;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import javax.script.Compilable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/18
 */
@Slf4j
public class GraalJsScriptExecutor implements IScriptExecutor {

    private String[] arguments;

    private Engine engine;

    private Source scriptSource;

    @Override
    public void initialize(Method method) {
        ScriptMeta scriptMeta = AnnotationUtil.getAnnotation(method, ScriptMeta.class);
        try {
            method.setAccessible(true);
            String scriptBody = (String) method.invoke(null);
            engine = Engine.create();
            scriptSource = Source.create("js",scriptBody);
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
        return "graal.js";
    }

    @Override
    public String suffix() {
        return "js";
    }

    @Override
    public Object execute(Map<String, Object> env) {
        try (Context context = Context.newBuilder().allowAllAccess(true).engine(this.engine).build()) {
            Value bindings = context.getBindings("js");
            env.forEach(bindings::putMember);
            Value value = context.eval(scriptSource);

            if (value.isBoolean()) {
                return value.asBoolean();
            }
            else if (value.isNumber()) {
                return value.asInt();
            }
            else if (value.isString()) {
                return value.asString();
            }
            return value;
        }
        catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String[] arguments() {
        return arguments;
    }
}
