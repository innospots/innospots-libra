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

package io.innospots.script.python;

import io.innospots.script.base.jrs223.Jsr223ScriptExecutor;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/12
 */
public class GroovyScriptExecutor extends Jsr223ScriptExecutor {

    @Override
    public String scriptType() {
        return "groovy";
    }

    @Override
    public String suffix() {
        return "groovy";
    }

    @Override
    protected Bindings createBindings(Map<String, Object> env) {
        Bindings bindings = new SimpleBindings();
        bindings.put("item", env);
        return bindings;
    }

    @Override
    protected String parseReturnVariable(String script) {
        return null;
    }
}
