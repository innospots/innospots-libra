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

import io.innospots.base.script.jit.MethodBody;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/18
 */
class ScriptExecutorManagerTest {

    @Test
    void test(){
        File parentFile = new File(new File("").getAbsolutePath()).getParentFile();
        File buildPath = new File(parentFile,".script_build_path");
        System.out.println(buildPath.getAbsolutePath());
        ScriptExecutorManager.setPath(new File(buildPath,"src").getAbsolutePath(), buildPath.getAbsolutePath());

    }

    private MethodBody buildNativeMethod(){
        MethodBody methodBody = new MethodBody();
        methodBody.setSuffix("java");
        methodBody.setReturnType(Map.class);
        methodBody.setScriptType("java");
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

        return methodBody;
    }

    private MethodBody buildCmdShellMethod(){
        MethodBody methodBody = new MethodBody();

        return methodBody;
    }

    private MethodBody buildAviatorMethod(){
        MethodBody methodBody = new MethodBody();

        return methodBody;
    }

    private MethodBody buildCmdPythonMethod(){
        MethodBody methodBody = new MethodBody();

        return methodBody;
    }

}