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

package io.innospots.workflow.node.app.logic;

import com.google.common.base.Enums;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.node.instance.NodeInstance;
import io.innospots.workflow.node.app.utils.NodeInstanceUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API End Node
 *
 * @author Smars
 * @date 2021/9/19
 */
public class EndNode extends BaseAppNode {

    private ReturnValueType returnValueType;

    public static final String RETURN_VALUE_TYPE = "return_value_type";

    private static final String RESPONSE_FIELD = "response_fields";

    private List<NodeParamField> responseFields;


    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        validFieldConfig(nodeInstance, RETURN_VALUE_TYPE);
        returnValueType = Enums.getIfPresent(ReturnValueType.class, nodeInstance.valueString(RETURN_VALUE_TYPE)).orNull();

        if (returnValueType == ReturnValueType.FIELD) {
            responseFields = NodeInstanceUtils.buildParamFields(nodeInstance, RESPONSE_FIELD);
        }
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        switch (returnValueType) {
            case INPUT:
                super.invoke(nodeExecution);
                break;
            case FIRST_ITEM:
                NodeOutput nodeOutput = this.buildOutput(nodeExecution);
                for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                    for (Map<String, Object> item : executionInput.getData()) {
                        nodeOutput.addResult(item);
                        break;
                    }
                }
                break;
            case FIELD:
                nodeOutput = this.buildOutput(nodeExecution);
                Map<String, Object> nItem = new LinkedHashMap<>();
                for (ExecutionInput input : nodeExecution.getInputs()) {
                    for (Map<String, Object> item : input.getData()) {
                        if (CollectionUtils.isNotEmpty(responseFields)) {
                            for (NodeParamField field : responseFields) {
                                nItem.put(field.getCode(), item.get(field.getCode()));
                            }
                        }
                    }
                }//end for input
                nodeOutput.addResult(nItem);
                break;
            case EMPTY:
                break;
            default:
        }
    }

    @Override
    protected void end(NodeExecution nodeExecution, FlowExecution flowExecution) {
        for (NodeOutput nodeOutput : nodeExecution.getOutputs()) {
            flowExecution.addOutput(nodeOutput.getResults());
        }
    }

    public enum ReturnValueType {
        /**
         *
         */
        INPUT,
        FIRST_ITEM,
        EMPTY,
        FIELD;
    }
}
