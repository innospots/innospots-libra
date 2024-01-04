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

package io.innospots.schedule.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/2
 */
class ParamParserTest {


    @Test
    void test(){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("key,","abc");
        map.put("key2",2356);
        map.put("key3", BigDecimal.valueOf(11));
        map.put("key4","#{'Hello World'.concat('!')}");
//        map.put("key5","#{new String('hello world').toUpperCase()}");
        map.put("key6","#{new String('hello world').toUpperCase()}");
//        map.put("key71","#{systemProperties.size}");
        map.put("key7","random number is #{T(java.lang.Math).random()}");
        map.put("key8","#{T(java.time.LocalDate).now().format(T(java.time.format.DateTimeFormatter).ISO_LOCAL_DATE)}");
        map.put("key9","#{T(java.time.LocalDate).now()}");
        System.out.println(ParamParser.toValueMap(map));
        LocalDate localDate = LocalDate.now();
        localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

}