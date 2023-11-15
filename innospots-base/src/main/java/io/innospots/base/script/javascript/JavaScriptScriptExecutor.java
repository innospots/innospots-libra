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

package io.innospots.base.script.javascript;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://blog.csdn.net/weixin_31589331/article/details/114280833
 *
 * @author Smars
 * @date 2021/5/5
 */
@Slf4j
public class JavaScriptScriptExecutor implements IScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptScriptExecutor.class);

    private CompiledScript compiledScript;

    private String[] arguments;

    public JavaScriptScriptExecutor(CompiledScript compiledScript, String[] arguments) {
        this.compiledScript = compiledScript;
        this.arguments = arguments;
    }

    @Override
    public void initialize(Method method) {

    }

    @Override
    public ExecuteMode executeMode() {
        return null;
    }

    @Override
    public String scriptType() {
        return null;
    }

    @Override
    public String suffix() {
        return null;
    }

    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
        Bindings bindings = compiledScript.getEngine().createBindings();
        //bindings.putAll(env);
        // 设置在js中的item的调用执行
        bindings.put("item", JSONUtils.toJsonString(env));
//        bindings.put("item", new LinkedHashMap<>(env));

        return execute(bindings);
    }

    private Object execute(Bindings bindings) {
        Object v = null;
        try {
            v = compiledScript.eval(bindings);
            if (log.isDebugEnabled()) {
                if (v != null) {
                    //log.debug("script out:{}, clazz:{}", v, v.getClass());
                } else {
                    log.debug("output is null.");
                }
            }
            //处理整型数值问题
//            v = normalizeValue(v);
            v = parseObject(v);
            /*
            if (v instanceof Map) {
                v = normalizeValue(v);
            } else if (v instanceof Double && (Double) v == ((Double) v).intValue()) {
                v = ((Double) v).intValue();
            }
             */
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw ScriptException.buildInvokeException(this.getClass(), ScriptType.JAVASCRIPT, e, e.getMessage());
        }
        return v;
    }

    /*
    private Object parseValue(Map<String, Object> values) {
        Object vv;
        boolean isArray = values.keySet().stream().allMatch(v -> v.matches("[\\d]+"));

        if (isArray) {
            List<Object> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                list.add(normalizeValue(entry.getValue()));
            }
            vv = list;
        } else {
            vv = normalizeValue(values);
        }
        return vv;
    }
     */

    private Object parseObject(Object value) {
        if (value == null) {
            return null;
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

    @Override
    public Object execute(Object... args) throws ScriptException {
        if (args.length == 1 && args[0] instanceof List) {
            Bindings bindings = compiledScript.getEngine().createBindings();
            bindings.put("items", JSONUtils.toJsonString(args[0]));
//            bindings.put("items", args[0]);
            return execute(bindings);
        }else if(args.length == 1){
            if(args[0] instanceof Map){
                Map<String,Object> m = (Map<String, Object>) args[0];
                return execute(m);
            }else{
                return execute(JSONUtils.objectToMap(args[0]));
            }
        } else {
            Map<String, Object> env = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                env.put(arguments[i], args[i]);
            }
            return execute(env);
        }
    }

    @Override
    public String[] arguments() {
        return arguments;
    }

    public CompiledScript getCompiledScript() {
        return compiledScript;
    }
}
