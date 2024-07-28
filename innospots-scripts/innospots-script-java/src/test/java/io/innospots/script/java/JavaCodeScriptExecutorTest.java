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

package io.innospots.script.java;

import io.innospots.base.script.java.ScriptMeta;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/18
 */
class JavaCodeScriptExecutorTest {

    @Test
    void initialize() throws NoSuchMethodException {
        JavaCodeScriptExecutor executor = new JavaCodeScriptExecutor();
        Method method2 = JavaCodeScriptExecutorTest.class.getMethod("scriptMethod2");
        executor.initialize(method2);
        Map<String,Object> input = new HashMap<>();
        input.put("1","2");
        Object obj = executor.execute(input);
        System.out.println(obj);
    }

    @Test
    void execute() throws NoSuchMethodException {
        JavaCodeScriptExecutor executor = new JavaCodeScriptExecutor();
        Method method2 = JavaCodeScriptExecutorTest.class.getMethod("scriptMethod");
        executor.initialize(method2);
        Map<String,Object> input = new HashMap<>();
        input.put("1","2");
        Object obj = executor.execute(input);
        System.out.println(obj);
    }

    @ScriptMeta(scriptType = "java",suffix = "java",returnType = Map.class,
            args = {"java.util.Map item"})
    public static String scriptMethod() {
        String ps = "";
        ps += "import java.util.Properties;";
        ps += "Properties prop = System.getProperties();\n";
        ps += "System.out.println(\"inner out: \"+prop);";
        ps += "return item;";
        return ps;
    }

    @ScriptMeta(scriptType = "java",suffix = "java",returnType = Long.class,
            args = {"java.util.Map item"})
    public static String scriptMethod2() {
        String ps = "long ts = System.currentTimeMillis();\n";
        ps += "System.out.println(ts);";
        ps += "return ts;";
        return ps;
    }

    @ScriptMeta(
            scriptType = "java",
            suffix = "java",
            args = {"java.lang.String i1", "java.lang.String i2"},
            path = "/tmp/nativejavanodetest/$JavaScriptNodeKey.java"
    )
    public static String _java_java_$JavaScriptNodeKey() {
        String var0 = "Map<String, Object> res = new HashMap();  res.put(\"res\",i1+i2); return res;";
        return var0;
    }

}