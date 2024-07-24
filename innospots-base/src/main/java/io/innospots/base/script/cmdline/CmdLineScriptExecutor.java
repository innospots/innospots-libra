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
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.googlecode.aviator.AviatorEvaluator;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.Pair;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.OutputMode;
import io.innospots.base.script.java.ScriptMeta;
import io.innospots.base.script.jit.MethodBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
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
    protected String cmd;
    protected OutputMode outputMode;

    public CmdLineScriptExecutor() {
    }


    @Override
    public void initialize(Method method) {
        if (scriptPath != null) {
            return;
        }
        ScriptMeta scriptMeta = AnnotationUtil.getAnnotation(method, ScriptMeta.class);
        outputMode = scriptMeta.outputMode();
        try {
            scriptPath = scriptMeta.path();
            String scriptBody = (String) method.invoke(null);
            File scriptFile = new File(scriptPath);
            if (scriptFile.exists()) {
                if (!scriptFile.delete()) {
                    log.warn("delete script file {} failed", scriptPath);
                }
            }

            cmd = scriptMeta.cmd();
            if (StringUtils.isEmpty(cmd)) {
                cmd = System.getProperty(scriptType() + ".path");
            }

            scriptBody = parseInputScript() + scriptBody;
            FileUtil.writeBytes(scriptBody.getBytes(), scriptFile);

            Pair<Class<?>, String>[] pairs = this.argsPair(scriptMeta.args());
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
        Object[] values = new String[arguments.length];
        for (int i = 0; i < this.arguments.length; i++) {
            values[i] = env.get(arguments[i]);
        }
        return execute(values);
    }

    @Override
    public Object execute(Object... args) throws ScriptException {
        return executeProcess(null, 0, buildCmdParams(args));
    }

    public Object execute(Map<String, Object> env, long timeout) {
        return execute(env, null, timeout);
    }

    /*
    public void execute(Map<String, Object> env,long timeoutSecond) {
        Process process = null;
        Timer timer = null;
        TimerTask timerTask = null;
        String[] args = buildCmdParams(flatInput(env));
        try {
            process = RuntimeUtil.exec(args);

            if (timeoutSecond > 0) {
                timer = new Timer();
                timerTask = timeoutTask(process);
                timer.schedule(timerTask, TimeUnit.SECONDS.toMillis(timeoutSecond));
            }
            InputStream in = process.getInputStream();
            outputConsumer.accept(in);
            process.waitFor();
        } catch (Exception e) {
            throw ScriptException.buildInvokeException(getClass(), scriptType(), e, args);
        } finally {
            if (timerTask != null) {
                timerTask.cancel();
            }
            if (timer != null) {
                timer.cancel();
            }
        }
    }

     */

    public Object execute(Map<String, Object> env, Function<InputStream, Object> outputFunction) {
        return execute(env, outputFunction, 0);
    }

    public Object execute(Map<String, Object> env, Function<InputStream, String> outputFunction, long timeoutSecond) {
        return executeProcess(outputFunction, timeoutSecond, buildCmdParams(flatInput(env)));
    }

    private Object executeProcess(Function<InputStream, String> outputFunction, long timeoutSecond, String[] args) {
        Process process = null;
//        String[] args = buildCmdParams(flatInput(env));
        String output = null;
        try {
            process = RuntimeUtil.exec(args);
            CompletableFuture<Process> cf = process.onExit();
            if (timeoutSecond > 0) {
                cf.orTimeout(timeoutSecond, TimeUnit.SECONDS);
            }
            InputStream in = process.getInputStream();
            if (outputFunction == null) {
                outputFunction = outputFunction(in);
            }
            output = outputFunction.apply(in);
            process = cf.get();
        } catch (Exception e) {
            throw ScriptException.buildInvokeException(getClass(), scriptType(), e, args);
        } finally {
            if (process != null && process.isAlive()) {
                RuntimeUtil.destroy(process);
            }
        }

        return processOutput(output);
    }

    private Function<InputStream, String> outputFunction(InputStream in) {
        return inputStream -> IoUtil.read(in, Charset.defaultCharset());
    }

    private TimerTask timerTask(Process process) {
        return new TimerTask() {
            @Override
            public void run() {
                RuntimeUtil.destroy(process);
            }
        };
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
        return cmd;
    }


    protected String[] buildCmdParams(Object... args) {
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
        return params.toArray(new String[]{});
    }

    private String parseInputScript() {
        StringBuilder sr = new StringBuilder("##!/usr/bin/env bash\n");
        sr.append("\n").append("CRT_DIR=$(cd \"$(dirname \"${BASH_SOURCE[0]}\")\" && pwd )").append("\n");
        sr.append("input_params=\"$1\"").append("\n");
        sr.append("IFS=',' read -r -a pairs <<< \"$input_params\"").append("\n");
        sr.append("for pair in \"${pairs[@]}\"; do").append("\n");
        sr.append("    IFS='=' read -r key value <<< \"$pair\"").append("\n");
        sr.append("    eval \"$key=\\\"$value\\\"\"").append("\n");
        sr.append("done").append("\n\n");

        return sr.toString();
    }

    private String flatInput(Map<String, Object> input) {
        if (MapUtils.isEmpty(input)) {
            return "";
        }
        StringBuilder buf = new StringBuilder("'");
        String inputStr = input.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
        buf.append(inputStr);
        buf.append("'");
        return buf.toString();
    }

    private Object processOutput(String output) {
        Object v = null;
        switch (outputMode) {
            case FIELD:
            case OVERWRITE:
            case PAYLOAD:
                List<String> lines = output.lines().collect(Collectors.toList());
                String outLine = lines.get(lines.size() - 1);
                Map<String, Object> m = convertStringToMap(outLine);
                if (m.isEmpty() && outputMode == OutputMode.FIELD) {
                    v = output;
                } else {
                    v = m;
                }
                break;
            case LOG:
            case STREAM:
            default:
        }
        return v;
    }

    private static Map<String, Object> convertStringToMap(String input) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (input != null && !input.isEmpty()) {
            String[] pairs = input.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    map.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return map;
    }
}
