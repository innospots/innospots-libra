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

import io.innospots.base.script.java.ScriptMeta;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/19
 */
class GraalJsScriptExecutorTest {

    @SneakyThrows
    @Test
    void test() {
        GraalJsScriptExecutor executor = new GraalJsScriptExecutor();
        Method method2 = GraalJsScriptExecutorTest.class.getMethod("scriptMethod2");
        executor.initialize(method2);
        Map<String,Object> input = new HashMap<>();
//        Map<String,String> i = new HashMap<>();
        input.put("k2","12345");
        input.put("k3","abc12");
        Object obj = executor.execute(input);
        System.out.println(obj.getClass());
        System.out.println(obj);
    }


    @ScriptMeta(suffix = "js")
    public static String scriptMethod() {
        String src = "print(k2); function f1(v1,v2){var item=new Object();item.p2=v1;item.p3=v2;return item;}; f1(k2,k3)";
        return src;
    }

    @ScriptMeta(suffix = "js")
    public static String scriptMethod2() {
        String src = "console.log('console out log:'+k2+k3);return k2+k3;";
        src = GraalJsScriptExecutor.wrapSource("scriptMethod2", "k2,k3", src);
        return src;
    }



}