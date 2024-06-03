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

package io.innospots.workflow.core.node.executor;

import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/17
 */
@Getter
@Setter
@Slf4j
public class StateNode extends BaseNodeExecutor {


    private static final String FIELDS = "fields";

    private static final String HAS_INPUT_FIELD = "has_input_field";

    private StateNodeType stateNodeType;

    private List<NodeParamField> inputFields;

    private boolean hasInputField;

    @Override
    protected void initialize() {
        stateNodeType = StateNodeType.valueOf(this.ni.getCode());
        if (stateNodeType == StateNodeType.START) {
            hasInputField = ni.valueBoolean(HAS_INPUT_FIELD);
            if (hasInputField) {
                inputFields = NodeInstanceUtils.buildParamFields(ni, FIELDS);
            }
        }
    }

    /*
    @Override
    public void invoke(NodeExecution nodeExecution) {
        if(stateNodeType == StateNodeType.START){
            if(hasInputField){
                NodeOutput nodeOutput = this.buildOutput(nodeExecution);
                if(nodeExecution.inputIsEmpty()){
                    log.warn("start input is empty, using default sample input:{}",inputFields);
                    Map<String,Object> nItem = new LinkedHashMap<>();
                    for (NodeParamField inputField : inputFields) {
                        if(inputField.getValue()!=null && StringUtils.isNotEmpty(inputField.getValue().toString())){
                            nItem.put(inputField.getCode(),inputField.getValue());
                        }
                    }
                    if(log.isDebugEnabled()){
                        log.debug("start node input:{}",nItem);
                    }
                    nodeOutput.addResult(nItem);
                }else{
                    for (ExecutionInput input : nodeExecution.getInputs()) {
                        for (Map<String, Object> item : input.getData()) {
                            if(CollectionUtils.isNotEmpty(inputFields)){
                                Map<String,Object> nItem = new LinkedHashMap<>();
                                for (NodeParamField inputField : inputFields) {
                                    Object v = item.getOrDefault(inputField.getCode(),inputField.getValue());
                                    if(v!=null &&StringUtils.isNotEmpty(v.toString())){
                                        nItem.put(inputField.getCode(),v);
                                    }
                                }
                                if(log.isDebugEnabled()){
                                    log.debug("start node input:{}",nItem);
                                }
                                nodeOutput.addResult(nItem);
                            }
                        }//end for data
                    }//end input
                }
            }else{
                super.invoke(nodeExecution);
            }
        }else{
            super.invoke(nodeExecution);
        }
    }

     */

    @Override
    protected Object processItem(Map<String, Object> item) {
        if (hasInputField && CollectionUtils.isEmpty(inputFields)) {
            return item;
        }
        Map<String, Object> nItem = new LinkedHashMap<>();
        for (NodeParamField inputField : inputFields) {
            Object v = item != null ? item.getOrDefault(inputField.getCode(), inputField.getValue()) : inputField.getValue();
            if (v != null && StringUtils.isNotEmpty(v.toString())) {
                nItem.put(inputField.getCode(), v);
            }
        }//end inputFields
        if (log.isDebugEnabled()) {
            log.debug("start node input:{}", nItem);
        }
        return nItem;
    }

    @Override
    public void processNextKeys(NodeExecution nodeExecution) {
        switch (stateNodeType) {
            case END:
                nodeExecution.setNext(false);
                nodeExecution.setNextNodeKeys(null);
                nodeExecution.setStatus(ExecutionStatus.COMPLETE);
                break;
            case PAUSE:
                nodeExecution.setNextNodeKeys(null);
                nodeExecution.setNext(false);
                nodeExecution.setStatus(ExecutionStatus.STOPPED);
                break;
            default:
                super.processNextKeys(nodeExecution);
                break;
        }
    }

    public enum StateNodeType {
        /**
         * 开始
         */
        START,
        /**
         * 结束
         */
        END,
        /**
         * 暂停
         */
        PAUSE,
        /**
         * 下一个循环
         */
        NEXT_LOOP;
    }
}
