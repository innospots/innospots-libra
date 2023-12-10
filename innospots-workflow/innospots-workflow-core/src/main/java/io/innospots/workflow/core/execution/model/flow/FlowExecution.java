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

package io.innospots.workflow.core.execution.model.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.execution.ExecMode;
import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionResource;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import io.innospots.workflow.core.execution.reader.NodeOutputStaticReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Raydian
 * @date 2020/12/20
 */
@Getter
@Setter
@Slf4j
public class FlowExecution extends FlowExecutionBase {


    protected List<String> currentNodeKeys = new ArrayList<>();

    /**
     * execution context
     */
    protected Map<String, Object> contexts = new ConcurrentHashMap<>();

    /**
     * execution input
     */
    protected ExecutionInput input = new ExecutionInput();

    protected LongAdder counter = new LongAdder();

    protected List<Map<String, Object>> output = new ArrayList<>();

    /**
     *
     */
    protected String endNodeKey;

    /**
     * debug mode, the nodes should be executed
     */
    @JsonIgnore
    protected Set<String> shouldExecutes = new HashSet<>();

    private String contextDataPath;

    /**
     * the total node count in the flow
     */
    private int totalCount = 0;

    public FlowExecution() {
        this.startTime = LocalDateTime.now();
    }


    public static FlowExecution buildNewFlowExecution(String flowKey, List<Map<String, Object>> payloads){
        FlowExecution flowExecution = new FlowExecution();;
        flowExecution.setStatus(ExecutionStatus.READY);
        flowExecution.contextDataPath = WorkflowRuntimeContext.contextResourcePath;
        flowExecution.setFlowKey(flowKey);

        if (payloads != null) {
            flowExecution.input.setData(payloads);
        }
        return flowExecution;
    }

    public static FlowExecution buildNewFlowExecution(Long flwInstanceId, Integer revision) {
        return buildNewFlowExecution(flwInstanceId, revision, false, false);
    }

    public static FlowExecution buildNewFlowExecution(
            Long flwInstanceId, Integer revision, boolean skipFlowExecution, boolean skipNodeExecution) {
        return buildNewFlowExecution(flwInstanceId, revision, skipFlowExecution, skipNodeExecution, null);
    }

    public static FlowExecution buildNewFlowExecution(
            Long flowInstanceId, Integer revision,
            boolean skipFlowExecution, boolean skipNodeExecution,
            List<Map<String, Object>> payloads
    ) {
        FlowExecution flowExecution = new FlowExecution();
        //flowExecution.setFlowExecutionId(String.valueOf(InnospotIdGenerator.generateId()));
        flowExecution.setStatus(ExecutionStatus.READY);
        flowExecution.flowInstanceId = flowInstanceId;
        flowExecution.revision = revision;
        flowExecution.skipFlowExecution = skipFlowExecution;
        flowExecution.skipNodeExecution = skipNodeExecution;
        flowExecution.contextDataPath = WorkflowRuntimeContext.contextResourcePath;

        if (payloads != null) {
            flowExecution.input.setData(payloads);
        }
        return flowExecution;
    }

    public static FlowExecution buildNewFlowExecution(
            Long flwInstanceId, Integer revision, List<Map<String, Object>> payloads) {
        return buildNewFlowExecution(flwInstanceId, revision, false, false, payloads);
    }


    public FlowExecution addInput(Map<String, Object> data) {
        this.input.addInput(data);
        return this;
    }

    public FlowExecution addInput(List<Map<String, Object>> data) {
        this.input.addInput(data);
        return this;
    }

    public File flowExecutionDataPath() {
        return IExecutionContextOperator.buildFlowExecutionDataPath(this.contextDataPath, this.flowKey, this.startTime, this.flowExecutionId);
    }

    public void addOutput(Map<String, Object> item) {
        this.output.add(item);
    }

    public void addOutput(List<Map<String, Object>> items) {
        this.output.addAll(items);
    }


    public void resetCurrentNodeKey(String nodeKey) {
        this.currentNodeKeys.clear();
        this.currentNodeKeys.add(nodeKey);
    }

    public void addCurrentNodeKey(String nodeKey) {
        this.currentNodeKeys.add(nodeKey);
    }

    public void fillContext() {
        this.setLocation(valueOf(this.contexts.get(PROP_LOCATION)));
        this.setUuid(valueOf(this.contexts.get(PROP_UUID)));
        this.setUuidType(valueOf(this.contexts.get(PROP_UUID_TYPE)));
        this.setChannel(valueOf(this.contexts.get(PROP_CHANNEL)));
        this.setExecutionUri(valueOf(this.contexts.get(PROP_URI)));
    }

    public void addContext(Map<String, Object> item) {
        if (item != null) {
            this.contexts.putAll(item);
        }
    }

    public void addContext(String key, Object value) {
        this.contexts.put(key, value);
    }


    public boolean shouldExecute(String nodeKey) {
        return (endNodeKey == null || shouldExecutes.contains(nodeKey));
    }

    public Integer currentSequenceNumber() {
        counter.increment();
        return counter.intValue();
    }

    public void resetSequenceNumber(int number) {
        this.counter.reset();
        this.counter.add(number);
    }

    public List<ExecutionInput> getInputs(List<String> prevNodeKeys, String target) {
        List<ExecutionInput> executionInputs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(prevNodeKeys)) {
            for (String nodeKey : prevNodeKeys) {
                NodeExecution nodeExecution = getNodeExecution(nodeKey);
                if (nodeExecution == null) {
                    continue;
                }
                ExecutionInput executionInput = extractInput(nodeKey, target);
//                input.setData(nodeExecution.getMiddles());
                //executionInput.setData(nodeExecution.flatOutput(target));
                if (executionInput != null) {
                    executionInputs.add(executionInput);
                }
            }
        } else if (input != null && CollectionUtils.isNotEmpty(input.getData())) {
            executionInputs.add(input);
        }
        return executionInputs;
    }

    private ExecutionInput extractInput(String nodeKey, String targetNodeKey) {
        NodeExecution nodeExecution = getNodeExecution(nodeKey);
        if (nodeExecution == null) {
            return null;
        }
        ExecutionInput executionInput = new ExecutionInput(nodeKey);
        List<NodeOutput> nodeOutputs = null;
        if (this.execMode == ExecMode.MEMORY) {
            nodeOutputs = nodeExecution.getOutputs();
        } else {
            nodeOutputs = NodeOutputStaticReader.readNodeOutputs(this.flowExecutionId, nodeExecution.getNodeExecutionId(), targetNodeKey);
        }

        List<Map<String, Object>> outputList = new ArrayList<>();
        for (NodeOutput nodeOutput : nodeOutputs) {
            if (nodeOutput.containNextNodeKey(targetNodeKey)) {
                if(nodeOutput.getResults()!=null){
                    for (int i = 0; i < nodeOutput.getResults().size(); i++) {
                        outputList.add(nodeOutput.getResults().get(i));
                        List<ExecutionResource> res = nodeOutput.itemResources(i);
                        if(res!=null){
                            executionInput.addResource(res);
                            if(res.size() > 1){
                                log.warn("item have multi file resources:{}",res);
                            }
                        }
                    }//end for output item
                }else if(nodeOutput.getResources()!=null){
                    for (List<ExecutionResource> resources : nodeOutput.getResources().values()) {
                        executionInput.addResource(resources);
                    }
                }
            }//end if targetKey
        }
        if (!outputList.isEmpty()) {
            executionInput.setData(outputList);
        } else {
            return null;
        }

        return executionInput;
    }

    public void fillExecutionContext(Map<String, Object> executionContext) {
        Object o = executionContext.get("inputs");
        if (o != null) {
            this.input = JSONUtils.parseObject(String.valueOf(o), ExecutionInput.class);
        }
        o = executionContext.get("outputs");
        if (o != null) {
            this.output = JSONUtils.parseObject(String.valueOf(o), List.class);
        }
    }


    public List<NodeExecution> getLastNodeExecution() {
        List<NodeExecution> nodeExecutions = new ArrayList<>();
        if (endNodeKey != null && this.currentNodeKeys.contains(endNodeKey)) {
            nodeExecutions.add(this.getNodeExecution(endNodeKey));
        } else if (!currentNodeKeys.isEmpty()) {
            for (String currentNodeKey : this.currentNodeKeys) {
                nodeExecutions.add(this.getNodeExecution(currentNodeKey));
            }
        }
        return nodeExecutions;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("executionId='").append(flowExecutionId).append('\'');
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", status=").append(status);
        sb.append(", counter=").append(counter.intValue());
        sb.append(", currentNodeKeys=").append(currentNodeKeys);
        sb.append('}');
        return sb.toString();
    }

    private String valueOf(Object v) {
        if (v == null) {
            return null;
        }
        return String.valueOf(v);
    }

}
