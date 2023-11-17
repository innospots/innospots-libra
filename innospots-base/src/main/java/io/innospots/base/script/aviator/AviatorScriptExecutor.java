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

package io.innospots.base.script.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.jrs223.Jsr223ScriptExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * aviator 表达式引擎编译器
 *
 * @author Smars
 * @date 2021/8/11
 */
public class AviatorScriptExecutor extends Jsr223ScriptExecutor {

    @Override
    public String scriptType() {
        return "AviatorScript";
    }
    @Override
    public String suffix() {
        return "aviator";
    }

}
