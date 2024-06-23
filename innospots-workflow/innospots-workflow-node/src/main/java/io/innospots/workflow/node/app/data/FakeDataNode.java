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

package io.innospots.workflow.node.app.data;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.i18n.LocaleMessageUtils;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

import java.util.*;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/12/19
 */
@Slf4j
public class FakeDataNode extends BaseNodeExecutor {

    public static final String FIELD_FAKE_DATA = "fake_data";
    public static final String FIELD_ITEM_SIZE = "item_size";
    public static final String WAITING_MILLIS = "waiting_mills";

    private Map<String, Object> fakeData;
    private Integer itemSize;
    private Faker faker;
    private Integer waitingMills;

    @Override
    protected void initialize() {
        String fakeString = validString(FIELD_FAKE_DATA);
        fakeData = JSONUtils.toMap(fakeString);
        itemSize = valueInteger(FIELD_ITEM_SIZE);
        if (itemSize == null) {
            itemSize = 1;
        }
        faker = new Faker(LocaleMessageUtils.getLocale());
        waitingMills = valueInteger(WAITING_MILLIS);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        for (int i = 0; i < itemSize; i++) {
            Map<String, Object> item = new HashMap<>();
            for (Map.Entry<String, Object> entry : fakeData.entrySet()) {
                try {
                    Object value = fillFakeData(entry.getValue());
                    item.put(entry.getKey(), value);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    throw e;
                }
            }
            nodeOutput.addResult(item);
        }
        if (waitingMills != null) {
            try {
                Thread.sleep(waitingMills);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    private Object fillFakeData(Object value) {
        if (value instanceof String) {
            String exp = (String) value;
            String str = faker.expression((String) value);
            if (exp.contains("nextDouble")) {
                return Double.parseDouble(str);
            } else if (exp.contains("nextFloat")) {
                return Float.parseFloat(str);
            } else if (exp.contains("nextInt")) {
                return Integer.parseInt(str);
            } else if (exp.contains("nextLong")) {
                return Long.parseLong(str);
            } else if (exp.contains("nextBoolean")) {
                return Boolean.parseBoolean(str);
            } else if (exp.contains("#{Number.")) {
                return Long.parseLong(str);
            }
            return str;
        } else if (value instanceof Collection) {
            List<Object> items = new ArrayList<>();
            for (Object item : ((Collection<?>) value)) {
                items.add(fillFakeData(item));
            }
            return items;
        } else if (value instanceof Map) {
            Map<String, Object> item = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Object v = fillFakeData(entry.getValue());
                item.put((String) entry.getKey(), v);
            }
            return item;
        }
        return value;
    }
}
