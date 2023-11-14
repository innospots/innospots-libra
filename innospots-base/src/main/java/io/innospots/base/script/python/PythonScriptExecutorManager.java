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

package io.innospots.base.script.python;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.script.cmdline.CmdlineScriptExecutorManager;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/11
 */
public class PythonScriptExecutorManager extends CmdlineScriptExecutorManager {

    public PythonScriptExecutorManager(String identifier, String cmdPath) {
        super(identifier, ScriptType.PYTHON.name(), cmdPath);
    }

    public static PythonScriptExecutorManager build(String cmdPath, String scriptPath, String identifier) {
        PythonScriptExecutorManager engine = new PythonScriptExecutorManager(identifier, cmdPath);
        engine.fill(scriptPath, identifier, ScriptType.PYTHON.name(), "py");
        return engine;
    }

    public static PythonScriptExecutorManager build(String scriptPath, String identifier) {
        return build("python", scriptPath, identifier);
    }

}
