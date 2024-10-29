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

package io.innospots.script.base.cmdline;

import cn.hutool.core.io.resource.ResourceUtil;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.json.JSONUtils;
import io.innospots.script.base.jit.MethodBody;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/16
 */
public class CmdPythonScriptExecutor extends CmdLineScriptExecutor{

    private static final String PY_TEMPLATE_FILE = "scripts/python_cmd_template.py";

    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
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

    @Override
    public void reBuildMethodBody(MethodBody methodBody) {
        String srcBody = methodBody.getSrcBody();
        srcBody = srcBody.replaceAll("\n", "\\\\n");
        srcBody = srcBody.replaceAll("\"", "\\\\\"");
        methodBody.setSrcBody(srcBody);
    }

    @Override
    protected String parseInputScript(String scriptBody) {
        String tmpl = ResourceUtil.readUtf8Str(PY_TEMPLATE_FILE);
        return tmpl.replace("${SCRIPT_BODY}", scriptBody);
    }

    @Override
    protected Object processOutput(String output) {
        return super.processOutput(output);
    }


    @Override
    protected Map<String, Object> convertStringToMap(String input) {
        if(input!=null && input.startsWith("{")){
            return JSONUtils.toMap(input);
        }
        return super.convertStringToMap(input);
    }
}
