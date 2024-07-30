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

package io.innospots.workflow.node.app.script;

import io.innospots.base.script.OutputMode;
import io.innospots.base.script.jit.MethodBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/11
 */
@Slf4j
public class CmdLinePythonNode extends CmdLineScriptNode {


    @Override
    public MethodBody buildScriptMethodBody() {
        String src = this.valueString(FIELD_ACTION_SCRIPT);
        OutputMode oMode = OutputMode.valueOf(this.valueString(FIELD_OUTPUT_MODE));
        String scriptType = scriptType();
        if (StringUtils.isEmpty(src) || StringUtils.isEmpty(scriptType)) {
            return null;
        }

        MethodBody methodBody = new MethodBody();
        methodBody.setOutputMode(oMode);
        String cmdPath = this.valueString(FIELD_CMD_PATH);
        methodBody.setCmdPath(cmdPath);
        methodBody.setReturnType(Object.class);
        methodBody.setScriptType(scriptType);
        methodBody.setMethodName(ni.expName());

        /*
        String newSrc = "import sys\nimport ast\n\n";
        StringBuilder srb = new StringBuilder(newSrc);
        srb.append("args = ''").append("\n");
        srb.append("if len(sys.argv) > 1:").append("\n");
        srb.append("  ").append("args = sys.argv[1]").append("\n");
        srb.append(src);
        srb.append("\n\n");
        newSrc = srb.toString();

         */

        methodBody.setSrcBody(src);
        return methodBody;
    }

    @Override
    public String scriptType() {
        return "python";
    }

}
