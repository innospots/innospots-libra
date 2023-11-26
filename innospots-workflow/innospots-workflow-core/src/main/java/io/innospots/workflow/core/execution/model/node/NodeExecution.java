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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Raydian
 * @date 2020/12/20
 */
@Getter
@Setter
@Slf4j
public class NodeExecution extends NodeExecutionBase {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ExecutionInput> inputs;

    /**
     * multi branch or condition output
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<NodeOutput> outputs;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> nextNodeKeys;

    /**
     * shareContext variables
     */
    private List<Map<String, Object>> shareContext = new ArrayList<>();;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> logs = new LinkedHashMap<>();

    private File contextDataPath;


    public static NodeExecution buildNewNodeExecution(String nodeKey, Long flowInstanceId, Integer revision,
                                                      String flowExecutionId, boolean skipNodeExecution,
                                                      Integer sequenceNumber
    ) {
        NodeExecution nodeExecution = new NodeExecution();
        nodeExecution.skipNodeExecution = skipNodeExecution;
        nodeExecution.nodeKey = nodeKey;
        nodeExecution.flowInstanceId = flowInstanceId;
        nodeExecution.flowExecutionId = flowExecutionId;
        nodeExecution.revision = revision;
        nodeExecution.status = ExecutionStatus.READY;
        nodeExecution.startTime = LocalDateTime.now();
        nodeExecution.outputs = new ArrayList<>();
        nodeExecution.inputs = new ArrayList<>();
        nodeExecution.setSequenceNumber(sequenceNumber);

        return nodeExecution;
    }

    public static NodeExecution buildNewNodeExecution(String nodeKey, FlowExecution flowExecution) {
        NodeExecution nodeExecution = new NodeExecution();
        nodeExecution.sequenceNumber = flowExecution.currentSequenceNumber();
        nodeExecution.nodeExecutionId = String.join("_", "fk", flowExecution.getFlowKey(), Integer.toString(nodeExecution.getSequenceNumber()), nodeKey, String.valueOf(InnospotsIdGenerator.generateId()));
        nodeExecution.nodeKey = nodeKey;
        nodeExecution.skipNodeExecution = flowExecution.isSkipNodeExecution();
        nodeExecution.flowInstanceId = flowExecution.getFlowInstanceId();
        nodeExecution.flowExecutionId = flowExecution.getFlowExecutionId();
        nodeExecution.revision = flowExecution.getRevision();
        nodeExecution.status = ExecutionStatus.READY;
        nodeExecution.startTime = LocalDateTime.now();
        nodeExecution.outputs = new ArrayList<>();
        nodeExecution.inputs = new ArrayList<>();
        nodeExecution.flowStartTime = flowExecution.getStartTime();
        nodeExecution.execMode = flowExecution.getExecMode();
        nodeExecution.contextDataPath = flowExecution.flowExecutionDataPath();
        nodeExecution.setRecordMode(flowExecution.getRecordMode());
        return nodeExecution;
    }


    public static NodeExecution buildNewNodeExecution(
            String nodeKey, Long flowInstanceId, Integer revision,
            String flowExecutionId, boolean skipNodeExecution
    ) {
        return buildNewNodeExecution(nodeKey, flowInstanceId, revision, flowExecutionId, skipNodeExecution, 1);
    }


    public void fillExecutionContext(Map<String, Object> executionContext) {
        if (executionContext == null) {
            return;
        }
        Object o = executionContext.get("inputs");
        if (o != null) {
            this.inputs = JSONUtils.toList(String.valueOf(o).replaceAll("\\n", " "), ExecutionInput.class);
        }
        o = executionContext.get("outputs");
        if (o != null) {
            this.outputs = JSONUtils.toList(String.valueOf(o).replaceAll("\\n", " "), NodeOutput.class);
        }
    }

    public boolean nextExecute() {
        return next && CollectionUtils.isNotEmpty(nextNodeKeys);
    }

    public void addNextNodeKey(String nodeKey) {
        if (nextNodeKeys == null) {
            nextNodeKeys = new ArrayList<>();
        }
        this.nextNodeKeys.add(nodeKey);
    }

    public void addInput(ExecutionInput executionInput) {
        this.inputs.add(executionInput);
    }

    public void addOutput(NodeOutput nodeOutput) {
        outputs.add(nodeOutput);
    }

    public Map<String, Object> outputLog() {
        Map<String, Object> logs = new LinkedHashMap<>();
        if(this.outputs == null){
            return logs;
        }
        for (int i = 0; i < this.outputs.size(); i++) {
            NodeOutput nodeOutput = outputs.get(i);
            if (nodeOutput.getName() != null) {
                logs.put(nodeOutput.getName(), nodeOutput.log());
            } else {
                logs.put("output_" + i, nodeOutput.log());
            }
        }
        return logs;
    }

    public void addLog(String key, Object value) {
        this.logs.put(key, value);
    }

    public void addLog(Map<String, Object> logData) {
        if (logData != null) {
            this.logs.putAll(logData);
        }
    }

    public void clearOutput() {
        this.outputs.clear();
    }

    public void clearInput() {
        this.inputs.clear();
    }

    public void addShareContext(Map<String,Object> context){
        this.shareContext.add(context);
    }

    public void clearShareContext(){
        this.shareContext.clear();
    }

    public List<Map<String, Object>> flatOutput(String nodeKey) {
        List<Map<String, Object>> outputList = new ArrayList<>();
        for (NodeOutput nodeOutput : this.outputs) {
            if (nodeOutput.containNextNodeKey(nodeKey)) {
                outputList.addAll(nodeOutput.getResults());
            }
        }
        return outputList;
    }

    public boolean inputIsEmpty() {
        if(CollectionUtils.isEmpty(this.inputs)){
            return true;
        }
        if(this.inputs.get(0)!=null && CollectionUtils.isEmpty(this.inputs.get(0).getData())){
            return true;
        }
        return false;
    }

    public int inputSize(){
        return this.inputs.size();
    }

    public void fillTotal(){
        this.outputs.forEach(NodeOutput::fillTotal);
    }

}
