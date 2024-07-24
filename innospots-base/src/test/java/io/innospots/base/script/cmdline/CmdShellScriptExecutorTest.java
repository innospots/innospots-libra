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

import io.innospots.base.script.java.ScriptMeta;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/19
 */
class CmdShellScriptExecutorTest {


    @SneakyThrows
    @Test
    void test() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod");
        executor.initialize(scriptMethod);
        Object s = executor.execute("abd", "dds", "1234");
        System.out.println("out:"+s);

    }


    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class,
            path = "/tmp/test.sh")
    public static String scriptMethod() {
        String ps = "";
        ps += "echo 'abc', $1 $2";
        return ps;
    }

    @SneakyThrows
    @Test
    void test2() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod2");
        executor.initialize(scriptMethod);
        Object s = executor.execute("abd", "dds", "1234");
        System.out.println("out:"+s);

    }


    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class,
            path = "/tmp/test.sh")
    public static String scriptMethod2() {
        String ps = "";
        ps += "jps ";
        return ps;
    }

}