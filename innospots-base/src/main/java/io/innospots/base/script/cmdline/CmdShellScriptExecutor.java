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

package io.innospots.base.script.cmdline;

import io.innospots.base.script.jit.MethodBody;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/16
 */
public class CmdShellScriptExecutor extends CmdLineScriptExecutor {

    protected String parseInputScript() {
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


    @Override
    public String scriptType() {
        return "shell";
    }

    @Override
    public String suffix() {
        return "sh";
    }

    @Override
    protected String cmdPath() {
        String cmd = super.cmdPath();
        if(cmd == null){
            cmd = "sh";
        }
        return cmd;
    }
}
