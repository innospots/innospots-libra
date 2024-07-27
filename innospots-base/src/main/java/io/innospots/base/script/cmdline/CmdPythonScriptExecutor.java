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

import io.innospots.base.exception.ScriptException;
import io.innospots.base.json.JSONUtils;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/16
 */
public class CmdPythonScriptExecutor extends CmdLineScriptExecutor{


    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
        //Object[] val = new Object[1];
        //val[0] = JSONUtils.toJsonString(env);
        return super.execute(env);
    }

    @Override
    public String scriptType() {
        return "python";
    }

    @Override
    public String suffix() {
        return "py";
    }

    @Override
    protected String cmdPath() {
        String cmd = super.cmdPath();
        if(cmd == null){
            cmd = "python";
        }
        return cmd;
    }
}
