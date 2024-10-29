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

package io.innospots.script.base;

import io.innospots.script.base.jit.MethodBody;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/18
 */
class ScriptExecutorManagerTest {

    ScriptExecutorManager executorManager;

    @Test
    void test(){
        File parentFile = new File(new File("").getAbsolutePath()).getParentFile();
        File buildPath = new File(parentFile,".script_build_path");
        System.out.println(buildPath.getAbsolutePath());
        ScriptExecutorManager.setPath(new File(buildPath,"src").getAbsolutePath(), buildPath.getAbsolutePath());
        executorManager =ScriptExecutorManager.newInstance("TestScript1");
        executorManager.register(buildNativeMethod());
//        executorManager.register(buildJavaMethod());
//        executorManager.register(buildJavaScriptMethod());
        executorManager.register(buildCmdPythonMethod());
        executorManager.register(buildCmdShellMethod());
        executorManager.register(buildAviatorMethod());
        executorManager.build();
        executorManager.reload();
    }

    @Test
    void testExe(){
        test();
        Map<String,Object> input = new HashMap<>();
        input.put("a",20);
        input.put("b",1200);
        IScriptExecutor scriptExecutor = executorManager.getExecutor("avCall");
        Object object = scriptExecutor.execute(input);
        System.out.println(object);
    }

    private MethodBody buildNativeMethod(){
        MethodBody methodBody = new MethodBody();
        methodBody.setSuffix("java");
        methodBody.setReturnType(Map.class);
        methodBody.setScriptType("javaNative");
        methodBody.setMethodName("javaCall");
        String src = "long ts = System.currentTimeMillis();";
        src += "System.out.println(ts);";
        src += "item.put(\"ts\",ts);";
        src += "return item;";
        methodBody.setSrcBody(src);
        return methodBody;
    }

    private MethodBody buildJavaScriptMethod(){
        MethodBody methodBody = new MethodBody();
        methodBody.setSuffix("js");
        methodBody.setReturnType(Map.class);
        methodBody.setScriptType("javascript");
        methodBody.setMethodName("jsCall");
        String src = "print(item.k2); " +
                "var itm = new Object(); " +
                "itm.a=12;" +
                "itm.b='bs';" +
                "var p={};" +
                "p.k=11;p.l='9s';" +
                "print(item);" +
                " return itm;";
        methodBody.setSrcBody(src);

        return methodBody;
    }

    private MethodBody buildJavaMethod(){
        MethodBody methodBody = new MethodBody();
        methodBody.setSuffix("java");
        methodBody.setReturnType(Map.class);
        methodBody.setScriptType("java");
        methodBody.setMethodName("javaSc");
        String ps = "";
        ps += "import java.util.Properties;";
        ps += "Properties prop = System.getProperties();\n";
        ps += "System.out.println(\"inner out: \"+prop);";
        ps += "return item;";
        methodBody.setSrcBody(ps);
        return methodBody;
    }

    private MethodBody buildCmdShellMethod(){
        MethodBody methodBody = new MethodBody();
        methodBody.setSuffix("sh");
        methodBody.setReturnType(String.class);
        methodBody.setScriptType("shell");
        methodBody.setMethodName("shellCall");
        String ps = "";
        ps += "echo 'abc', $1 $2";
        methodBody.setSrcBody(ps);
        return methodBody;
    }

    private MethodBody buildAviatorMethod(){
        MethodBody methodBody = new MethodBody();
        methodBody.setSuffix("aviator");
        methodBody.setReturnType(Boolean.class);
        methodBody.setScriptType("AviatorScript");
        methodBody.setMethodName("avCall");
        String ps = "";
        ps += "a <100 && b >= 910";
        methodBody.setSrcBody(ps);
        return methodBody;
    }

    private MethodBody buildCmdPythonMethod(){
        MethodBody methodBody = new MethodBody();
        methodBody.setSuffix("py");
        methodBody.setReturnType(String.class);
        methodBody.setScriptType("python");
        methodBody.setMethodName("pyCall");
        String script = "td = {\"Alice\": 112, \"Beth\": \"9102\", \"Cecil\": \"3258\"}; print(td)";
        methodBody.setSrcBody(script);
        return methodBody;
    }

}