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

package io.innospots.script.base.aviator;

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
class AviatorScriptExecutorTest {

    @SneakyThrows
    @Test
    void test(){
        AviatorScriptExecutor executor = new AviatorScriptExecutor();
        Method sciptMethod = AviatorScriptExecutorTest.class.getMethod("scriptMethod");
        executor.initialize(sciptMethod);
        Map<String,Object> input = new HashMap<>();
        input.put("a",20);
        input.put("b",1200);
        Object res = executor.execute(input);
        System.out.println(res);
    }

    public static String scriptMethod() {
        String ps = "";
        ps += "a <100 && b >= 910";
        return ps;
    }
}