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

import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.jit.MethodBody;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * script execute node
 *
 * @author Smars
 * @date 2021/4/20
 */
public class ScriptJavaNode extends ScriptBaseNode {

    @Override
    public MethodBody buildScriptMethodBody() {
        String src = this.valueString(FIELD_ACTION_SCRIPT);
        String scriptType = scriptType();
        ParamField pf = new ParamField();
        pf.setCode("item");
        pf.setValueType(FieldValueType.MAP);
        pf.setName("item");
        if (StringUtils.isEmpty(src) || StringUtils.isEmpty(scriptType)) {
            return null;
        }
        MethodBody methodBody = new MethodBody();
        methodBody.setReturnType(Object.class);
        methodBody.setScriptType(scriptType);
        methodBody.setMethodName(ni.expName());
        methodBody.setParams(List.of(pf));

        String newSrc = src;

        if (CollectionUtils.isNotEmpty(ni.getInputFields())) {
            String paramSrc = "";

            for (ParamField inputField : ni.getInputFields()) {
                FieldValueType valueType = inputField.getValueType();
                if (valueType == null) {
                    valueType = FieldValueType.OBJECT;
                }

                String var = inputField.getCode();
                if (Object.class.equals(valueType.getClazz())) {
                    paramSrc += valueType.getClazz().getName()
                            + " " + var + " = item.get( \\\"" + var + "\\\"); ";
                } else {
                    paramSrc += valueType.getClazz().getName()
                            + " " + var + " = (" + valueType.getClazz().getName() + ")item.get( \\\"" + var + "\\\"); ";
                }

            }//end for
            newSrc = "";
            int importIdx = src.lastIndexOf("import");
            if (importIdx > -1) {
                int endIdx = src.indexOf(";", importIdx);
                newSrc += src.substring(0, endIdx + 1);

                src = src.substring(endIdx + 1);
            }

            newSrc += paramSrc;

            newSrc += src;
        }

        methodBody.setSrcBody(newSrc);
        return methodBody;
    }

}
