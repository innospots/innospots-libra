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

import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.DataFakerUtils;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.InputField;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/12/19
 */
@Slf4j
public class FakeFieldNode extends BaseNodeExecutor {

    public static final String FIELD_FAKE_FIELDS = "fake_fields";
    public static final String FIELD_ITEM_SIZE = "item_size";
    public static final String WAITING_MILLIS = "waiting_mills";

    private List<InputField> fakeFields;
    private Integer itemSize;
    private DataFakerUtils dataFakerUtils;
    private Integer waitingMills;

    @Override
    protected void initialize() {
        List<Map<String,Object>> fakeMapList = valueMapList(FIELD_FAKE_FIELDS);
        fakeFields = BeanUtils.toBean(fakeMapList, InputField.class);
        itemSize = valueInteger(FIELD_ITEM_SIZE);
        if (itemSize == null) {
            itemSize = 1;
        }
        dataFakerUtils = DataFakerUtils.build();
        waitingMills = valueInteger(WAITING_MILLIS);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = new ExecutionOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        for (int i = 0; i < itemSize; i++) {
            try {
                Map<String, Object> item = new LinkedHashMap<>();
                for (InputField fakeField : fakeFields) {
                    String fieldName = fakeField.getCode();
                    Object value = dataFakerUtils.gen(fakeField.getValue());
                    value = fillFakeData(fakeField.getValue(),value);
                    item.put(fieldName,value);
                }
                nodeOutput.addResult(item);
            }catch (Exception e){
                log.error(e.getMessage());
                throw e;
            }
        }
        if (waitingMills != null) {
            try {
                Thread.sleep(waitingMills);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    private Object fillFakeData(String exp,Object value) {
        String str = (String) value;
        if (value instanceof String) {
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
        }
        return str;
    }
}
