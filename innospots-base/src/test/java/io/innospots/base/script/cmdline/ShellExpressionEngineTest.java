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

import cn.hutool.core.lang.Assert;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.script.IScriptExecutor;
import org.junit.jupiter.api.Test;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/25
 */
class ShellExpressionEngineTest {


    @Test
    void test1() {
        String cmd = "sh";
        String identifier = "Flow_190_0";
        ScriptType scriptType = ScriptType.SHELL;
        String scriptPath = "/tmp";
        String suffix = "sh";
//        ShellScriptExecutorManager engine = ShellScriptExecutorManager.build(cmd, scriptPath, identifier);
//        String script = "echo 'hello world!'";
        String script = "echo $1 $2;";
        String method = "_fn56UQ5vW";
//        engine.deleteBuildFile();
//        engine.register(Void.class, method, script);
//        engine.compile();
//        IScriptExecutor expression = engine.getExecutor(method);
//        Assert.notNull(expression, "expression not null.");
//        if (expression != null) {
//            Object obj = expression.execute("11", 22, "33");
//            System.out.println("----------");
//            System.out.println(obj);
//        }
    }

}