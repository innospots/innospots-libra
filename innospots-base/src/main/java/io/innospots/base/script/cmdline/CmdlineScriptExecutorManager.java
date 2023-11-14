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

import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.IScriptExecutorManager;
import io.innospots.base.script.jit.MethodBody;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/5
 */
@Slf4j
public class CmdlineScriptExecutorManager implements IScriptExecutorManager {

    protected String identifier;

    protected String scriptType;

    protected SimpleScriptSourceFileBuilder sourceFileBuilder;

    protected Map<String, CmdLineScriptExecutor> expressions = new HashMap<>();

    protected String cmdPath;

    public CmdlineScriptExecutorManager(String identifier, String scriptType, String cmdPath) {
        this.identifier = identifier;
        this.cmdPath = cmdPath;
        this.scriptType = scriptType;
    }

    public static CmdlineScriptExecutorManager build(String cmdPath, String scriptPath, String identifier, String scriptType, String suffix) {
        CmdlineScriptExecutorManager engine = new CmdlineScriptExecutorManager(identifier, scriptType, cmdPath);
        engine.fill(scriptPath, identifier, scriptType, suffix);
        return engine;
    }

    protected void fill(String scriptPath, String identifier, String scriptType, String suffix) {
        this.sourceFileBuilder = new SimpleScriptSourceFileBuilder(scriptPath, identifier, scriptType, suffix);
    }


    @Override
    public String identifier() {
        return identifier + "_" + scriptType;
    }

    @Override
    public IScriptExecutor getExecutor(String methodName) {
        if (methodName.startsWith("$")) {
            methodName = methodName.replace("$", "_");
        }
        return expressions.get(methodName);
    }

    @Override
    public void reload() throws ScriptException {
        Map<String, CmdLineScriptExecutor> tmpExp = new HashMap<>();
        File[] scriptFiles = sourceFileBuilder.getScriptFiles();
        for (File scriptFile : scriptFiles) {
            CmdLineScriptExecutor cmdLineExpression = new CmdLineScriptExecutor(cmdPath, scriptFile.getAbsolutePath());
            String filename = scriptFile.getName();
            String nodeKey = filename.substring(0, filename.indexOf('.'));
            tmpExp.put(nodeKey, cmdLineExpression);
        }
        log.debug("load sourceDirectory:{}, cmd script engine:{}", sourceFileBuilder.sourceDirectory(), tmpExp.size());
        this.expressions = tmpExp;
    }

    @Override
    public boolean build() throws ScriptException {
        if (this.sourceFileBuilder.hasSourceBody()) {
            try {
                this.sourceFileBuilder.writeToFile();
            } catch (IOException e) {
                throw ScriptException.buildCompileException(this.getClass(), ScriptType.JAVA, e, e.getMessage());
            }
        }
        if (this.expressions.isEmpty()) {
            reload();
            return true;
        }
        return true;
    }

    @Override
    public boolean isLoaded() {
        return !expressions.isEmpty();
    }

    @Override
    public void clear() {
        this.sourceFileBuilder.deleteSourceFile();
    }

    @Override
    public void register(MethodBody methodBody) {

    }

    /*

    @Override
    public void register(Class<?> returnType, String methodName, String srcBody, ParamField... params) {
        if (methodName.startsWith("$")) {
            methodName = methodName.replace("$", "_");
        }
        sourceFileBuilder.addMethod(returnType, srcBody, methodName, params);
    }

     */

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("identifier='").append(identifier).append('\'');
        sb.append(", scriptType='").append(scriptType).append('\'');
        sb.append(", expressions size=").append(expressions.size());
        sb.append(", cmdPath='").append(cmdPath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
