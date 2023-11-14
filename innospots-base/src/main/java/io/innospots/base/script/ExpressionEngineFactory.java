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

package io.innospots.base.script;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.script.aviator.AviatorScriptScriptExecutorManager;
import io.innospots.base.script.javascript.JavaScriptScriptExecutorManager;
import io.innospots.base.script.python.PythonScriptExecutorManager;
import io.innospots.base.script.shell.ShellScriptExecutorManager;

import java.util.concurrent.TimeUnit;

/**
 * @author Raydian
 * @date 2020/12/31
 */
public class ExpressionEngineFactory {


    private static Cache<String, IScriptExecutorManager> engineCache =
            Caffeine.newBuilder()
                    .expireAfterAccess(3, TimeUnit.HOURS)
                    .build();

    public static IScriptExecutorManager buildNewEngine(ScriptType scriptType) {
        return null;
    }

    public static void clear(String identifier) {
        engineCache.invalidate(identifier);
    }

    public static GenericScriptExecutorManager build(String identifier) throws ScriptException {
        GenericScriptExecutorManager engine = (GenericScriptExecutorManager) engineCache.getIfPresent(identifier);
        if (engine == null) {
            //engine = GenericScriptExecutorManager.build(identifier);
            engine.reload();
            engineCache.put(identifier, engine);
        }
        return engine;
    }

    public static IScriptExecutorManager getEngine(String identifier, ScriptType scriptType) {
        IScriptExecutorManager engine;
        if(scriptType == ScriptType.JAVA || scriptType==ScriptType.JAVASCRIPT){
            engine = engineCache.getIfPresent(identifier);
        }else{
            engine = engineCache.getIfPresent(identifier+ "_" + scriptType);
        }
        return engine;
    }

    public static IScriptExecutorManager build(String identifier, ScriptType scriptType) throws ScriptException {
        IScriptExecutorManager engine = engineCache.getIfPresent(identifier + "_" + scriptType);
        if (engine != null) {
            return engine;
        }

        switch (scriptType) {
            case JAVA:
                //engine = JavaScriptExecutorManager.build(identifier);
                break;
            case FORMULA:
                engine = AviatorScriptScriptExecutorManager.build(identifier);
                break;
            case CONDITION:
                //engine = JavaScriptExecutorManager.build(identifier);
                engine.reload();
                break;
            case JAVASCRIPT:
                engine = JavaScriptScriptExecutorManager.build(identifier);
                break;
            case PYTHON:
                //String scriptPath = JavaScriptExecutorManager.getClassPath() + "/script";
                //engine = PythonScriptExecutorManager.build(scriptPath, identifier);
                break;
            case SHELL:
                //scriptPath = JavaScriptExecutorManager.getClassPath() + "/script";
                //engine = ShellScriptExecutorManager.build(scriptPath, identifier);
                break;
            case SCALA:
            case GROOVY:
            default:
                break;
        }

        if (engine != null) {
            engineCache.put(identifier + "_" + scriptType, engine);
        }


        return engine;
    }


}
