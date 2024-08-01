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

import cn.hutool.core.util.StrUtil;
import io.innospots.base.script.OutputMode;
import io.innospots.base.script.java.ScriptMeta;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/19
 */
class CmdPythonScriptExecutorTest {


    @SneakyThrows
    @Test
    void test() {
        CmdPythonScriptExecutor executor = new CmdPythonScriptExecutor();
        Method scriptMethod = CmdPythonScriptExecutorTest.class.getMethod("scriptMethod");
        executor.initialize(scriptMethod);
        Map<String,Object> m = new HashMap<>();
        m.put("aa", "1");
        m.put("bb", "22");
        m.put("cc", "33");
        Object s = executor.execute(m);
        System.out.println("out:"+s);
    }


    @ScriptMeta(scriptType = "python", suffix = "py", returnType = String.class,
            path = "/tmp/hello.py",outputMode = OutputMode.OVERWRITE)
    public static String scriptMethod() {
        String ss = String.join("\n",
                "def process_item(item):",
                w("td = {\"Alice\": 112, \"Beth\": \"9102\", \"Cecil\": \"3258\"};"),
                w("#print(td)"),
                w("return td")
                );

        return ss;
    }



    @SneakyThrows
    @Test
    void test2() {
        CmdPythonScriptExecutor executor = new CmdPythonScriptExecutor();
        Method scriptMethod = CmdPythonScriptExecutorTest.class.getMethod("scriptMethod2");
        executor.initialize(scriptMethod);
        Map<String,Object> m = new HashMap<>();
        m.put("aa", "1");
        m.put("bb", "22");
        m.put("cc", "33");
        Object s = executor.execute(m);
        System.out.println(s.getClass());
        System.out.println("out:"+s);

    }


    @ScriptMeta(scriptType = "python", suffix = "py", returnType = String.class,
            path = "/tmp/hello2.py",outputMode = OutputMode.OVERWRITE)
    public static String scriptMethod2() {
        String ss = String.join("\n",
                "def process_item(item):",
                w("td = {\"Alice\": 112, \"Beth\": \"9102\", \"Cecil\": \"3258\"};"),
                w("return item")
        );

        return ss;
    }

    @SneakyThrows
    @Test
    void test3() {
        CmdPythonScriptExecutor executor = new CmdPythonScriptExecutor();
        Method scriptMethod = CmdPythonScriptExecutorTest.class.getMethod("scriptMethod3");
        executor.initialize(scriptMethod);
        Map<String,Object> m = new HashMap<>();
        m.put("aa", "1");
        m.put("bb", "22");
        m.put("cc", "33");
        Object s = executor.execute(m, (line)->{
            System.out.println("line:"+line);
            return  line;
        });
        System.out.println(s.getClass());
        System.out.println("out:"+s);

    }

    @SneakyThrows
    @Test
    void test31() {
        CmdPythonScriptExecutor executor = new CmdPythonScriptExecutor();
        Method scriptMethod = CmdPythonScriptExecutorTest.class.getMethod("scriptMethod3");
        executor.initialize(scriptMethod);
        Map<String,Object> m = new HashMap<>();
        Object s = executor.execute(m, (line)->{
            System.out.println("line:"+line);
            return  line;
        });
        System.out.println(s.getClass());
        System.out.println("out:"+s);

    }


    @ScriptMeta(scriptType = "python", suffix = "py", returnType = String.class,
            path = "/tmp/hello3.py",outputMode = OutputMode.OVERWRITE)
    public static String scriptMethod3() {
        String ss = String.join("\n",
                "import datetime",
                "def process_item(item):",
                w("print('')"),
                w("print('dd')"),
                w("print('')"),
                w("return item")
        );

        return ss;
    }


    private static String w(String s){
        return "    " + s;
    }

}