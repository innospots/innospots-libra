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
import io.innospots.base.model.Pair;
import io.innospots.base.model.field.FieldValueType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/28
 */
@Slf4j
public class UdafSum {


    public static Map<Object, Number> sum(List<Map<String, Object>> items, String conditions, String numericField, String groupField) {
        Pair<Map<Object, DoubleAdder>, FieldValueType> pair = calculateSum(items, conditions, numericField, groupField);
        Map<Object, DoubleAdder> results = pair.getLeft();
        FieldValueType fieldValueType = pair.getRight();

        //return results;
        Map<Object, Number> countMap = new HashMap<>();
        for (Map.Entry<Object, DoubleAdder> entry : results.entrySet()) {
            if (fieldValueType == FieldValueType.LONG) {
                countMap.put(entry.getKey(), entry.getValue().longValue());
            } else if (fieldValueType == FieldValueType.INTEGER) {
                countMap.put(entry.getKey(), entry.getValue().intValue());
            } else if (fieldValueType == FieldValueType.DOUBLE) {
                countMap.put(entry.getKey(), entry.getValue().doubleValue());
            } else {
                countMap.put(entry.getKey(), entry.getValue().doubleValue());
            }

        }

        return countMap;
    }

    public static Pair<Map<Object, DoubleAdder>, FieldValueType> calculateSum(List<Map<String, Object>> items, String conditions, String sumField, String groupField) {
        Map<Object, DoubleAdder> results = new HashMap<>();
        Expression expression = null;

        if (StringUtils.isNotEmpty(conditions)) {
            expression = AviatorEvaluator.compile(conditions, true);
        }
        FieldValueType fieldValueType = null;
        for (Map<String, Object> item : items) {
            Object groupId = item.get(groupField);
            DoubleAdder counter = results.getOrDefault(groupId, new DoubleAdder());
            if (expression == null || (Boolean) expression.execute(item)) {
                Object number = item.get(sumField);
                if (number instanceof Integer) {
                    counter.add((Integer) number);
                    fieldValueType = FieldValueType.INTEGER;
                } else if (number instanceof Long) {
                    counter.add((Long) number);
                    fieldValueType = FieldValueType.LONG;
                } else if (number instanceof Float) {
                    counter.add((Float) number);
                    fieldValueType = FieldValueType.DOUBLE;
                } else if (number instanceof Double) {
                    counter.add((Double) number);
                    fieldValueType = FieldValueType.DOUBLE;
                } else {
                    try {
                        counter.add(Double.parseDouble(number.toString()));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                }
            }
            results.putIfAbsent(groupId, counter);
        }
        return Pair.of(results, fieldValueType);
    }

}
