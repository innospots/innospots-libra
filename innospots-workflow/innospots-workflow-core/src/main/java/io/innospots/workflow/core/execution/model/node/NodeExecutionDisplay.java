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

package io.innospots.workflow.core.execution.model.node;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Smars
 * @date 2021/5/10
 */
@Getter
@Setter
@Schema(title = "Display of Node Execution ")
public class NodeExecutionDisplay implements Comparable<NodeExecutionDisplay> {

    private String nodeKey;

    private String nodeName;

    private String nodeExecutionId;

    private String flowExecutionId;

    private int sequenceNumber;

    @Schema(title = "input data")
    private List<ExecutionInput> inputs;
//    private List<Map<String, Object>> inputs;

    /**
     * result table data
     */
    @Schema(title = "execution output results array")
    private List<OutputDisplay> outputs;

    @Schema(title = "output columns fields")
    protected List<ParamField> outputFields;

    @Schema(title = "node instance columns fields, which be setup using output field or modified by console")
    protected List<ParamField> schemaFields;

    @Schema(title = "execution output")
    private Map<String, Object> logs = new LinkedHashMap<>();

    public static NodeExecutionDisplay build(NodeExecution nodeExecution, NodeInstance nodeInstance, int page, int size) {
        NodeExecutionDisplay executionDisplay = buildNotContextPage(nodeExecution, nodeInstance);
        if (CollectionUtils.isNotEmpty(nodeExecution.getOutputs())) {
            List<OutputDisplay> outputDisplays = new ArrayList<>();
            for (ExecutionOutput output : nodeExecution.getOutputs()) {
                if (CollectionUtils.isNotEmpty(output.getResults())) {
                    if (page <= 0) {
                        page = 1;
                    }
                    OutputDisplay outputPage = new OutputDisplay(output, page, size);
                    outputPage.getResults().setList(output.getResults());
                    outputDisplays.add(outputPage);
                }
            }//end for
            executionDisplay.outputs = outputDisplays;
            executionDisplay.buildOutputField();
        }
        return executionDisplay;
    }

    public static NodeExecutionDisplay build(NodeExecution nodeExecution, NodeInstance nodeInstance) {
        int size = 50;
        int page = 1;
        NodeExecutionDisplay executionDisplay = buildNotContextPage(nodeExecution, nodeInstance);
        if (CollectionUtils.isNotEmpty(nodeExecution.getOutputs())) {
            List<OutputDisplay> outputDisplays = new ArrayList<>();
            for (ExecutionOutput output : nodeExecution.getOutputs()) {
                if (CollectionUtils.isNotEmpty(output.getResults())) {
                    if (page <= 0) {
                        page = 1;
                    }
                    OutputDisplay outputPage = new OutputDisplay(output, page, size);
                    for (int i = (page - 1) * size; i < size; i++) {
                        if (i < output.getResults().size()) {
                            outputPage.addItem(output.getResults().get(i));
                        }
                    }
                    outputDisplays.add(outputPage);
                }
            }//end for
            executionDisplay.outputs = outputDisplays;
            executionDisplay.buildOutputField();
        }
        return executionDisplay;
    }

    public static NodeExecutionDisplay build(NodeExecution nodeExecution, int page, int size) {
        return build(nodeExecution, null, page, size);
    }

    private static NodeExecutionDisplay buildNotContextPage(NodeExecution nodeExecution, NodeInstance nodeInstance) {
        if (nodeExecution == null) {
            return null;
        }
        NodeExecutionDisplay executionDisplay = new NodeExecutionDisplay();
        if (nodeInstance != null) {
            executionDisplay.schemaFields = nodeInstance.getOutputFields();
            executionDisplay.nodeName = nodeInstance.getName();
        } else {
            executionDisplay.schemaFields = nodeExecution.getSchemaFields();
            executionDisplay.nodeName = nodeExecution.getNodeName();
        }

        executionDisplay.flowExecutionId = nodeExecution.getFlowExecutionId();
//        executionDisplay.inputs = nodeExecution.flatInput();
        executionDisplay.inputs = nodeExecution.getInputs();
        executionDisplay.nodeExecutionId = nodeExecution.getNodeExecutionId();
        executionDisplay.nodeKey = nodeExecution.getNodeKey();
        executionDisplay.sequenceNumber = nodeExecution.getSequenceNumber();
        executionDisplay.logs.put("nodeExecutionId", nodeExecution.getNodeExecutionId());
        executionDisplay.logs.put("name", executionDisplay.nodeName);
        executionDisplay.logs.put("nodeKey", executionDisplay.nodeKey);
        executionDisplay.logs.put("status", nodeExecution.getStatus());
        executionDisplay.logs.put("consume", nodeExecution.consume());
        executionDisplay.logs.put("startTime", nodeExecution.getStartTime());
        executionDisplay.logs.put("endTime", nodeExecution.getEndTime());
        executionDisplay.logs.put("sequence", nodeExecution.getSequenceNumber());
        executionDisplay.logs.put("outputs", JSONUtils.toJsonString(nodeExecution.outputLog()));
        executionDisplay.logs.put("message", nodeExecution.getMessage());


        return executionDisplay;
    }


    public void addLog(String key, Object value) {
        this.logs.put(key, value);
    }

    private void buildOutputField() {
        outputFields = new ArrayList<>();
        //all output data have the save fields
        if (outputs != null && !outputs.isEmpty()) {
            List<Map<String, Object>> listResult = outputs.get(0).getResults().getList();
            outputFields = ExecutionOutput.buildOutputField(listResult);
        }

        if (outputFields.isEmpty() && schemaFields != null) {
            outputFields = schemaFields;
        } else if (CollectionUtils.isEmpty(schemaFields) && CollectionUtils.isNotEmpty(outputFields)) {
            schemaFields = outputFields;
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("inputs=").append(inputs);
        sb.append(", outputs=").append(outputs);
        sb.append(", outputFields=").append(outputFields);
        sb.append(", logs='").append(logs).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(@NotNull NodeExecutionDisplay o) {
        return Integer.compare(this.sequenceNumber, o.sequenceNumber);
    }
}
