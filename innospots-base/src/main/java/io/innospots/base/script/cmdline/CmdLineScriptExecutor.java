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

package io.innospots.base.script.cmdline;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.googlecode.aviator.AviatorEvaluator;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.Pair;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.java.ScriptMeta;
import io.innospots.base.script.jit.MethodBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/5
 */
@Slf4j
public abstract class CmdLineScriptExecutor implements IScriptExecutor {

    protected String scriptPath;
    protected String[] arguments;

    public CmdLineScriptExecutor() {
    }


    @Override
    public void initialize(Method method) {
        if(scriptPath!=null){
            return;
        }
        ScriptMeta scriptMeta = AnnotationUtil.getAnnotation(method, ScriptMeta.class);
        try {
            scriptPath = scriptMeta.path();
            String scriptBody = (String) method.invoke(null);
            File scriptFile = new File(scriptPath);
            if(scriptFile.exists()){
                if(!scriptFile.delete()){
                    log.warn("delete script file {} failed", scriptPath);
                }
            }
            FileUtil.writeBytes(scriptBody.getBytes(), scriptFile);

            Pair<Class<?>,String>[] pairs = this.argsPair(scriptMeta.args());
            arguments = Arrays.stream(pairs).map(Pair::getRight).collect(Collectors.toList()).toArray(String[]::new);
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public ExecuteMode executeMode() {
        return ExecuteMode.CMD;
    }

    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
        if (arguments == null) {
            return execute(flatInput(env));
        }
        String[] values = new String[arguments.length];
        for (int i = 0; i < this.arguments.length; i++) {
            values[i] = Optional.ofNullable(arguments[i]).orElse(null);
        }
        return execute(values);
    }

    @Override
    public Object execute(Object... args) throws ScriptException {
        List<String> params = new ArrayList<>();
        params.add(cmdPath());
        params.add(scriptPath);
        for (int i = 0; i < args.length; i++) {
            String value = args[i] != null ? String.valueOf(args[i]) : null;
            if (StringUtils.isNotBlank(value)) {
                params.add(value);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("execute cmd:{}", params);
        }

        return RuntimeUtil.execForStr(params.toArray(new String[]{}));
    }


    @Override
    public void reBuildMethodBody(MethodBody methodBody) {
        String srcBody = methodBody.getSrcBody();
        srcBody = srcBody.replaceAll("\n", "\\\\n");
        srcBody = srcBody.replaceAll("\"", "\\\\\"");
        methodBody.setSrcBody(srcBody);
    }


    @Override
    public String[] arguments() {
        return arguments;
    }

    protected String cmdPath() {
        return System.getenv(this.scriptType() + ".path");
    }

    private String parseInputScript(){
        StringBuilder sr = new StringBuilder("##!/usr/bin/env bash\n");
        sr.append("\n").append("CRT_DIR=$(cd \"$(dirname \"${BASH_SOURCE[0]}\")\" && pwd )").append("\n");
        sr.append("input_params=\"$1\"").append("\n");
        sr.append("IFS=',' read -r -a pairs <<< \"$input_params\"").append("\n");
        sr.append("for pair in \"${pairs[@]}\"; do").append("\n");
        sr.append("    IFS='=' read -r key value <<< \"$pair\"").append("\n");
        sr.append("    eval \"$key=\\\"$value\\\"\"").append("\n");
        sr.append("done").append("\n");

        return sr.toString();
    }

    private String flatInput(Map<String,Object> input){
        if(MapUtils.isEmpty(input)){
            return "";
        }
        StringBuilder buf = new StringBuilder("'");
        String inputStr = input.entrySet().stream().map(e->e.getKey()+"="+e.getValue())
                .collect(Collectors.joining(","));
        buf.append(inputStr);
        buf.append("'");
        return buf.toString();
    }
}
