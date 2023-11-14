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

import io.innospots.base.model.Pair;
import io.innospots.base.model.field.FieldValueType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

import static io.innospots.base.script.function.aggregation.UdafSum.calculateSum;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/28
 */
@Slf4j
public class UdafAvg {


    public static Map<Object, Number> avg(List<Map<String, Object>> items, String conditions, String numericField, String groupField) {
        Pair<Map<Object, DoubleAdder>, FieldValueType> pair = calculateSum(items, conditions, numericField, groupField);
        Map<Object, DoubleAdder> results = pair.getLeft();
        FieldValueType fieldValueType = pair.getRight();

        Map<Object, Number> countMap = new HashMap<>();
        int size = items.size();
        for (Map.Entry<Object, DoubleAdder> entry : results.entrySet()) {
            if (fieldValueType == FieldValueType.LONG) {
                countMap.put(entry.getKey(), entry.getValue().longValue() / size);
            } else if (fieldValueType == FieldValueType.INTEGER) {
                countMap.put(entry.getKey(), entry.getValue().intValue() / size);
            } else {
                countMap.put(entry.getKey(), entry.getValue().doubleValue() / size);
            }
        }

        return countMap;
    }

}
