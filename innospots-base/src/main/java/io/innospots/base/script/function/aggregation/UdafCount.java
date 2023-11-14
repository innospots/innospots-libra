/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.base.script.function.aggregation;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/28
 */
public class UdafCount {


    public static Map<Object, Integer> count(List<Map<String, Object>> items, String conditions, String groupField) {
        Map<Object, LongAdder> results = new HashMap<>();
        Expression expression = null;

        if (StringUtils.isNotEmpty(conditions)) {
            expression = AviatorEvaluator.compile(conditions, true);
        }

        for (Map<String, Object> item : items) {
            Object groupId = item.get(groupField);
            LongAdder counter = results.getOrDefault(groupId, new LongAdder());
            if (expression == null || (Boolean) expression.execute(item)) {
                counter.increment();
            }
            results.putIfAbsent(groupId, counter);
        }
        //return results;
        Map<Object, Integer> countMap = new HashMap<>();
        for (Map.Entry<Object, LongAdder> entry : results.entrySet()) {
            countMap.put(entry.getKey(), entry.getValue().intValue());
        }

        return countMap;
    }

}
