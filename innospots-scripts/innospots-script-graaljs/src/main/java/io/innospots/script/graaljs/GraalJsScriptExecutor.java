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

package io.innospots.script.graaljs;

import io.innospots.base.script.ExecuteMode;
import io.innospots.base.script.IScriptExecutor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/18
 */
public class GraalJsScriptExecutor implements IScriptExecutor {

    private String[] arguments;

    private Engine engine;

    private Source scriptSource;

    @Override
    public void initialize(Method method) {
        engine = Engine.create();
        String srcBody = null;
        scriptSource = Source.create("js",srcBody);
    }

    @Override
    public ExecuteMode executeMode() {
        return ExecuteMode.SCRIPT;
    }

    @Override
    public String scriptType() {
        return "graal.js";
    }

    @Override
    public String suffix() {
        return "js";
    }

    @Override
    public Object execute(Map<String, Object> env) {
        try (Context context = Context.newBuilder().allowAllAccess(true).engine(this.engine).build()) {
            Value bindings = context.getBindings("js");
            env.forEach(bindings::putMember);
            Value value = context.eval(scriptSource);

            if (value.isBoolean()) {
                return value.asBoolean();
            }
            else if (value.isNumber()) {
                return value.asInt();
            }
            else if (value.isString()) {
                return value.asString();
            }
            return value;
        }
        catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String[] arguments() {
        return arguments;
    }
}
