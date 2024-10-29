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

package io.innospots.script.base.jrs223;

import cn.hutool.core.annotation.AnnotationUtil;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.Pair;
import io.innospots.script.base.ExecuteMode;
import io.innospots.script.base.IScriptExecutor;
import io.innospots.script.base.java.ScriptMeta;
import io.innospots.script.base.jit.MethodBody;
import lombok.extern.slf4j.Slf4j;

import javax.script.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2023/11/13
 */
@Slf4j
public abstract class Jsr223ScriptExecutor implements IScriptExecutor {

//    protected Compilable compilable;

    protected CompiledScript compiledScript;

    protected String[] arguments;

    protected String returnVar;

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

    protected Compilable compilable() {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        return (Compilable) engineManager.getEngineByName(scriptType());
    }

    @Override
    public ExecuteMode executeMode() {
        return ExecuteMode.SCRIPT;
    }

    @Override
    public Object execute(Object... args) throws ScriptException {
        return execute(convert(args));
    }

    protected Map<String, Object> convert(Object... args){
        if(args.length == 1){
            if(args[0] instanceof Map){
                return (Map<String, Object>) args[0];
            }
            return JSONUtils.objectToMap(args[0]);
        }else{
            Map<String, Object> env = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                env.put(arguments[i], args[i]);
            }
            return env;
        }
    }

    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
        return execute(createBindings(env));
    }

    protected Bindings createBindings(Map<String, Object> env) {
        return new SimpleBindings(env);
    }

    protected Object executeScript(Bindings bindings) throws javax.script.ScriptException {
        Object vv = compiledScript.eval(bindings);
        if(returnVar!=null){
            vv = bindings.get(returnVar);
        }
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
        return vv;
    }

    protected Object execute(Bindings bindings) {
        Object v = null;
        try {
            if (compiledScript == null) {
                throw ScriptException.buildInvokeException(this.getClass(), ScriptType.JAVASCRIPT.name(), "script compile fail");
            }
            Object vv = executeScript(bindings);
//            v = normalizeValue(v);
            v = parseObject(vv);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw ScriptException.buildInvokeException(this.getClass(), ScriptType.JAVASCRIPT.name(), e, e.getMessage());
        }
        return v;
    }


    @Override
    public String[] arguments() {
        return arguments;
    }


    protected Object parseObject(Object value) {
        if (value == null) {
            return null;
        }
        if(value instanceof Map){
            return new LinkedHashMap((Map)value);
        }
        String json = value.toString();
        if (json.startsWith("[")) {
            return JSONUtils.toList(json, Map.class);
        } else if (json.startsWith("{")) {
            return JSONUtils.parseObject(json, Map.class);
        } else {
            return value;
        }

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


    protected String parseReturnVariable(String script) {
        String[] lines = script.trim().split("\n");
        String lastLine = lines[lines.length - 1];
        String var = null;
        if (lastLine.startsWith("return")) {
            var = lastLine.substring(7).trim();
        }else {
            String[] split = lastLine.split("=");
            var = split[0];
        }
        return var;
    }

    private Object normalizeValue(Object value) {
        if (value instanceof Map) {
            Map<String, Object> mm = (Map<String, Object>) value;
            boolean isArray = mm.keySet().stream().allMatch(v -> v.matches("[\\d]+"));
            if (isArray) {
                List<Object> list = new ArrayList<>();
                for (Map.Entry<String, Object> entry : mm.entrySet()) {
                    list.add(normalizeValue(entry.getValue()));
                }
                value = list;
            } else {
                Map<String, Object> m = new HashMap<>();
                for (Map.Entry<String, Object> entry : mm.entrySet()) {
                    if (entry.getValue() instanceof Double) {
                        Double d = (Double) entry.getValue();
                        if (d == d.intValue()) {
                            m.put(entry.getKey(), d.intValue());
                        } else {
                            m.put(entry.getKey(), entry.getValue());
                        }
                    } else {
                        m.put(entry.getKey(), normalizeValue(entry.getValue()));
                    }
                }
                value = m;
            }
        } else if (value instanceof Double && (Double) value == ((Double) value).intValue()) {
            value = ((Double) value).intValue();
        }
        return value;
    }


}
