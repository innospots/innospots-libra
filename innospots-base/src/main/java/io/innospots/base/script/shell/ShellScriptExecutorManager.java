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

package io.innospots.base.script.shell;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.script.cmdline.CmdlineScriptExecutorManager;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/11
 */
public class ShellScriptExecutorManager extends CmdlineScriptExecutorManager {

    public ShellScriptExecutorManager(String identifier, String cmdPath) {
        super(identifier, ScriptType.SHELL.name(), cmdPath);
    }

    public static ShellScriptExecutorManager build(String cmdPath, String scriptPath, String identifier) {
        ShellScriptExecutorManager engine = new ShellScriptExecutorManager(identifier, cmdPath);
        engine.fill(scriptPath, identifier, ScriptType.SHELL.name(), "sh");
        return engine;
    }

    public static ShellScriptExecutorManager build(String scriptPath, String identifier) {
        return build("sh", scriptPath, identifier);
    }

}
