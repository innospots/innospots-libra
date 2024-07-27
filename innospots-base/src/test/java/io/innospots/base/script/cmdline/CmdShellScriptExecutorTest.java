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

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import io.innospots.base.script.OutputMode;
import io.innospots.base.script.java.ScriptMeta;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
        System.out.println("out:" + s);

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
        Object s = executor.execute("lVvm");
        System.out.println("out:" + s);

    }


    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class, cmd = "/bin/sh",
            path = "/tmp/test2.sh")
    public static String scriptMethod2() {
        String ps = "";
        ps += "jps -$1";
        return ps;
    }

    @SneakyThrows
    @Test
    void test3() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod3");
        executor.initialize(scriptMethod);
        Object s = executor.execute("abc=dd,cc=123,ff=91fd");
        System.out.println(s.getClass());
        System.out.println("out:" + s);

    }


    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class, cmd = "/bin/sh",
            path = "/tmp/test2.sh", outputMode = OutputMode.FIELD)
    public static String scriptMethod3() {
        String ps = "";
        ps += "echo ${CRT_DIR}";
        ps += "echo \"a: ${abc}\"\n";
        ps += "echo \"c: ${cc}\"\n";
        ps += "echo \"f: ${ff}\"\n";
        ps += "echo '----------------'\n";
        ps += "f=\"a=${abc}, b=${cc}, c=${ff}\"\n";
        ps += "echo $f\n";
        return ps;
    }

    @SneakyThrows
    @Test
    void test4() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod4");
        executor.initialize(scriptMethod);
        Object s = executor.execute("abc=dd,cc=123,ff=91fd");
        //System.out.println(s.getClass());
        System.out.println("out:" + s);

    }

    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class, cmd = "/bin/sh",
            path = "/tmp/test4.sh", outputMode = OutputMode.LOG)
    public static String scriptMethod4() {
        String ps = "";
        ps += "echo ${CRT_DIR}\n";
        ps += "echo \"a: ${abc}\"\n";
        ps += "echo \"c: ${cc}\"\n";
        ps += "echo \"f: ${ff}\"\n";
        ps += "echo '----------------'\n";
        ps += "f=\"a=${abc}, b=${cc}, c=${ff}\"\n";
        ps += "echo $f\n";
        return ps;
    }

    @SneakyThrows
    @Test
    void test5() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod5");
        executor.initialize(scriptMethod);
        Map<String, Object> env = new HashMap<>();
        //Object s = executor.execute("abc=dd,cc=123,ff=91fd");
        env.put("abc", "dd");
        env.put("ff", "91fd");
        env.put("cc", "123");
        env.put("dd", "dd12");
        Object s = executor.execute(env);
        /*
        Object s = executor.execute(env,
                inputStream -> {
            String ss = IoUtil.read(inputStream,Charset.defaultCharset());
                    //System.out.println(ss);
            return ss;
                });

         */
        //System.out.println(s.getClass());
        System.out.println("out:" + s);

    }

    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class, cmd = "/bin/sh",
            path = "/tmp/test5.sh", outputMode = OutputMode.LOG)
    public static String scriptMethod5() {
        String ps = "";
        ps += "echo ${CRT_DIR}\n";
        ps += "echo \"a: ${abc}\"\n";
        ps += "echo \"c: ${cc}\"\n";
        ps += "echo \"f: ${ff}\"\n";
        ps += "echo '----------------'\n";
        ps += "f=\"a=${abc}, b=${cc}, c=${ff}\"\n";
        ps += "echo $f\n";
        return ps;
    }

    @SneakyThrows
    @Test
    void test6() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod6");
        executor.initialize(scriptMethod);
        Map<String, Object> env = new HashMap<>();
        //Object s = executor.execute("abc=dd,cc=123,ff=91fd");
        env.put("abc", "dd");
        env.put("ff", "91fd");
        env.put("cc", "123");
        env.put("dd", "dd12");

        Object s = executor.execute(env,
                line -> {
                    StringBuilder ss = new StringBuilder();
                    try {
                        TimeUnit.MILLISECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                    System.out.println("ss:" + line);
                    return line;
                });


        //System.out.println(s.getClass());
        System.out.println("out:" + s);

    }

    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class, cmd = "/bin/sh",
            path = "/tmp/test6.sh", outputMode = OutputMode.STREAM)
    public static String scriptMethod6() {
        String ps = "";
        ps += "echo ${CRT_DIR}\n";
        ps += "echo \"a: ${abc}\"\n";
        ps += "echo \"c: ${cc}\"\n";
        ps += "echo \"f: ${ff}\"\n";
        ps += "echo '----------------'\n";
        ps += "f=\"a=${abc}, b=${cc}, c=${ff}\"\n";
        ps += "echo $f\n";
        return ps;
    }

    @SneakyThrows
    @Test
    void test7() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod7");
        executor.initialize(scriptMethod);
        Map<String, Object> env = new HashMap<>();
        env.put("abc", "dd");
        env.put("ff", "91fd");
        env.put("cc", "123");
        env.put("dd", "dd12");

        Object s = executor.execute(env,
                line -> {
                    StringBuilder ss = new StringBuilder();
                    try {
                        TimeUnit.MILLISECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                    System.out.println("ss:" + line);
                    return line;
                });


        //System.out.println(s.getClass());
        System.out.println("------------");
        System.out.println("out:" + s);

    }

    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class, cmd = "/bin/sh",
            path = "/tmp/test7.sh", outputMode = OutputMode.LOG)
    public static String scriptMethod7() {
        String ps = "jstat -gc 33769 1s 10";
        return ps;
    }

    @SneakyThrows
    @Test
    void test8() {
        CmdShellScriptExecutor executor = new CmdShellScriptExecutor();
        Method scriptMethod = CmdShellScriptExecutorTest.class.getMethod("scriptMethod8");
        executor.initialize(scriptMethod);
        Map<String, Object> env = new HashMap<>();
        env.put("abc", "dd");
        env.put("ff", "91fd");
        env.put("cc", "123");
        env.put("dd", "dd12");

        Object s = executor.execute(env,
                line -> {
                    StringBuilder ss = new StringBuilder();
                    try {
                        TimeUnit.MILLISECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                    System.out.println("ss:" + line);
                    return line;
                },5);


        //System.out.println(s.getClass());
        System.out.println("------------");
        System.out.println("out:" + s);

    }

    @ScriptMeta(scriptType = "shell", suffix = "sh", returnType = String.class, cmd = "/bin/sh",
            path = "/tmp/test7.sh", outputMode = OutputMode.LOG)
    public static String scriptMethod8() {
        String ps = "jps -lVvm\n";
        ps += "sleep 9\n";
        ps += "echo -9 .";
        return ps;
    }


}