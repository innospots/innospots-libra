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

import cn.hutool.core.util.RuntimeUtil;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/5
 */
@Slf4j
public class CmdLineScriptExecutor implements IScriptExecutor {

    protected String cmdPath;
    protected String scriptPath;
    protected String[] arguments;

    public CmdLineScriptExecutor(String cmdPath, String scriptPath, String[] arguments) {
        this.cmdPath = cmdPath;
        this.scriptPath = scriptPath;
        this.arguments = arguments;
    }

    public CmdLineScriptExecutor(String cmdPath, String scriptPath) {
        this.cmdPath = cmdPath;
        this.scriptPath = scriptPath;
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
        if (arguments == null) {
            return execute("");
        }
        String[] values = new String[arguments.length];
        for (int i = 0; i < this.arguments.length; i++) {
            values[i] = Optional.ofNullable(arguments[i]).orElse(null);
        }
        return values;
    }

    @Override
    public Object execute(Object... args) throws ScriptException {
        List<String> params = new ArrayList<>();
        params.add(cmdPath);
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
    public String[] arguments() {
        return arguments;
    }
}
