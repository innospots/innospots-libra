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

package io.innospots.base.script;

import io.innospots.base.exception.ScriptException;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/11/13
 */
public class Jsr223ScriptExecutor implements IScriptExecutor {

    @Override
    public void initialize(Method method) {

    }

    @Override
    public ExecuteMode executeMode() {
        return null;
    }

    @Override
    public String scriptType() {
        return null;
    }

    @Override
    public String suffix() {
        return null;
    }

    @Override
    public Object execute(Map<String, Object> env) throws ScriptException {
        return null;
    }

    @Override
    public Object execute(Object... args) {
        return null;
    }

    @Override
    public String[] arguments() {
        return new String[0];
    }
}
