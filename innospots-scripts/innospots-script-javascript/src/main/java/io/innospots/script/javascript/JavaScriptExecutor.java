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

package io.innospots.script.javascript;

import io.innospots.base.exception.ScriptException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.jit.MethodBody;
import io.innospots.base.script.jrs223.Jsr223ScriptExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.List;
import java.util.Map;

/**
 * https://blog.csdn.net/weixin_31589331/article/details/114280833
 *
 * @author Smars
 * @date 2021/5/5
 */
@Slf4j
public class JavaScriptExecutor extends Jsr223ScriptExecutor {

    @Override
    public String scriptType() {
        return "javascript";
    }

    @Override
    public String suffix() {
        return "js";
    }


    @Override
    public Object execute(Object... args) throws ScriptException {
        if (args.length == 1 && args[0] instanceof List) {
            Bindings bindings = new SimpleBindings();
            bindings.put("items", JSONUtils.toJsonString(args[0]));
//            bindings.put("items", args[0]);
            return execute(bindings);
        } else {
            return super.execute(args);
        }
    }

    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
        Bindings bindings = new SimpleBindings();
        bindings.put("item", JSONUtils.toJsonString(env));
        return execute(bindings);
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
        String source = "";
        source += "function ";
        source += methodBody.getMethodName();
        source += " (";
        source += args;
        source += "){";
        source += srcBody;
        source += "}\\n";
        //return string
        source += "JSON.stringify(";
        source += methodBody.getMethodName();
        source += "(";
        source += "JSON.parse(";
        source += args;
        source += ")";
        source += ")";
        source += ")";
        methodBody.setSrcBody(source);
        System.out.println(source);
//        return source;

        //return srcBody;
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

}
