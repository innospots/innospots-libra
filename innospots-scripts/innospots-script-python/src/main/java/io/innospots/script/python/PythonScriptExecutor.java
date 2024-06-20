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

package io.innospots.script.python;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.text.StrFormatter;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.Pair;
import io.innospots.base.script.java.ScriptMeta;
import io.innospots.base.script.jit.MethodBody;
import io.innospots.base.script.jrs223.Jsr223ScriptExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.script.Bindings;
import javax.script.Compilable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/12
 */
@Slf4j
public class PythonScriptExecutor extends Jsr223ScriptExecutor {

    private String returnVar;

    @Override
    public String scriptType() {
        return "jython";
    }

    @Override
    public String suffix() {
        return "py";
    }

    @Override
    public void initialize(Method method) {
        if (compiledScript != null) {
            return;
        }
        ScriptMeta scriptMeta = AnnotationUtil.getAnnotation(method, ScriptMeta.class);
        String scriptBody = "";
        try {
            method.setAccessible(true);
            scriptBody = (String) method.invoke(null);
            returnVar = parseReturnVariable(scriptBody);
            Compilable compilable = compilable();
            compiledScript = compilable.compile(scriptBody);
            if (scriptMeta != null) {
                Pair<Class<?>, String>[] pairs = this.argsPair(scriptMeta.args());
                arguments = Arrays.stream(pairs).map(Pair::getRight).collect(Collectors.toList()).toArray(String[]::new);
            }
        } catch (IllegalAccessException | InvocationTargetException | javax.script.ScriptException |
                ClassNotFoundException e) {
            log.error("source error:{}",scriptBody);
            log.error(e.getMessage(), e);
        }

    }


    @Override
    protected Object execute(Bindings bindings) {
        Object v = null;
        try {
            if (compiledScript == null) {
                throw ScriptException.buildInvokeException(this.getClass(), ScriptType.JAVASCRIPT.name(), "script compile fail");
            }
            compiledScript.eval(bindings);
            Object vv = bindings.get(returnVar);
            if (vv == null) {
                vv = compiledScript.getEngine().get(returnVar);
            }

            if (log.isDebugEnabled()) {
                if (vv != null) {
                    //log.debug("script out:{}, clazz:{}", v, v.getClass());
                } else {
                    log.debug("output is null.");
                }
            }
//            v = normalizeValue(v);
            v = parseObject(vv);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw ScriptException.buildInvokeException(this.getClass(), ScriptType.JAVASCRIPT.name(), e, e.getMessage());
        }
        return v;
    }



    @Override
    public void reBuildMethodBody(MethodBody methodBody) {
        String args = null;
        if (methodBody.getParams() == null || methodBody.getParams().size() == 0) {
            args = "item";
        } else {
            args = methodBody.getParams().get(0).getCode();
        }
        String srcBody = methodBody.getSrcBody();
        srcBody = srcBody.replaceAll("\n", "\\\\n");
        srcBody = srcBody.replaceAll("\"", "\\\\\"");

        methodBody.setSrcBody(srcBody);

    }


    private String parseReturnVariable(String script) {
        String[] lines = script.trim().split("\n");
        String lastLine = lines[lines.length - 1];
        String[] tokens = lastLine.split("=");
        return tokens[0].trim();
    }

}
