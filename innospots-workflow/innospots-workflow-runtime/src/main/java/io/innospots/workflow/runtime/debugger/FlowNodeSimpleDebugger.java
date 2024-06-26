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

package io.innospots.workflow.runtime.debugger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.workflow.core.execution.enums.RecordMode;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.instance.operator.WorkflowDraftOperator;
import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;
import io.innospots.workflow.core.debug.DebugPayload;
import io.innospots.workflow.core.engine.FlowEngineManager;
import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.enums.FlowStatus;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.debug.FlowNodeDebugger;
import io.innospots.workflow.core.execution.events.FlowExecutionTaskEvent;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecutionBase;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.reader.NodeExecutionReader;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.runtime.webhook.DefaultResponseBuilder;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponse;
import io.innospots.workflow.node.app.trigger.ApiTriggerNode;
import io.innospots.workflow.runtime.engine.BaseFlowEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/13
 */
@Slf4j
public class FlowNodeSimpleDebugger implements FlowNodeDebugger {


    private NodeExecutionReader nodeExecutionReader;

    private IFlowExecutionOperator flowExecutionOperator;

    private WorkflowDraftOperator workflowDraftOperator;

    private Cache<Long, String> executionCache = Caffeine.newBuilder().build();

    public FlowNodeSimpleDebugger(WorkflowDraftOperator workflowDraftOperator,
                                  NodeExecutionReader nodeExecutionReader,
                                  IFlowExecutionOperator flowExecutionOperator) {
        this.workflowDraftOperator = workflowDraftOperator;
        this.nodeExecutionReader = nodeExecutionReader;
        this.flowExecutionOperator = flowExecutionOperator;
    }

    @Override
    public Map<String, NodeExecutionDisplay> execute(Long workflowInstanceId, String nodeKey, List<Map<String, Object>> inputs) {
        WorkflowBaseBody workflowBaseBody = workflowDraftOperator.saveCacheToDraft(workflowInstanceId);

        inputs = convertApiInput(inputs,workflowBaseBody);

        IFlowEngine flowEngine = FlowEngineManager.eventFlowEngine();
        BuildProcessInfo buildProcessInfo = flowEngine.prepare(workflowInstanceId, 0, true);
        log.info("build info:{}", buildProcessInfo);
        Map<String, NodeExecutionDisplay> result = new LinkedHashMap<>();
        if (buildProcessInfo.getStatus() != FlowStatus.LOADED) {
            log.error("flow prepare failed, {}", buildProcessInfo);
            if (buildProcessInfo.getBuildException() != null) {
                NodeExecutionDisplay display = new NodeExecutionDisplay();
                display.setNodeKey(nodeKey);
                display.addLog("startTime", DateTimeUtils.normalizeDateTime(buildProcessInfo.getStartTime()));
                display.addLog("endTime", DateTimeUtils.normalizeDateTime(buildProcessInfo.getEndTime()));
                display.addLog("status", ExecutionStatus.FAILED);
                display.addLog("error", buildProcessInfo.errorMessage());
                result.put(nodeKey, display);
            } else {
                for (Map.Entry<String, Exception> exceptionEntry : buildProcessInfo.getErrorInfo().entrySet()) {
                    NodeExecutionDisplay display = new NodeExecutionDisplay();
                    display.addLog("startTime", DateTimeUtils.normalizeDateTime(buildProcessInfo.getStartTime()));
                    display.addLog("endTime", DateTimeUtils.normalizeDateTime(buildProcessInfo.getEndTime()));
                    display.addLog("status", ExecutionStatus.FAILED);
                    display.setNodeKey(exceptionEntry.getKey());
                    display.addLog("error", buildProcessInfo.getBuildMessage(exceptionEntry.getKey()));
                    result.put(display.getNodeKey(), display);
                }
            }
            //return result;
        }

        FlowExecution flowExecution = fillFlowExecution(inputs, workflowInstanceId);


        //endNodeKey
        log.info("flow execution: {}", flowExecution);
        //executionCache.put(workflowInstanceId,flowExecution.getFlowExecutionId());
        flowExecution.setEndNodeKey(nodeKey);
        flowEngine.execute(flowExecution);

        workflowBaseBody = workflowDraftOperator.getDraftWorkflow(workflowInstanceId);
        Map<String, NodeInstance> nodeCache = null;
        try {
            nodeCache = workflowBaseBody.getNodes().stream().collect(Collectors.toMap(NodeInstance::getNodeKey, Function.identity()));
        } catch (Exception e) {
            log.error(e.getMessage());
            for (NodeInstance node : workflowBaseBody.getNodes()) {
                log.error(node.toString());
            }
            throw e;
        }
        List<NodeExecution> nodeExecutions = new ArrayList<>(flowExecution.getNodeExecutions().values());
        nodeExecutions.sort(Comparator.comparingInt(NodeExecutionBase::getSequenceNumber));
        LinkedHashMap<String, String> outMap = new LinkedHashMap<>();
        for (NodeExecution nodeExecution : nodeExecutions) {
            NodeInstance nodeInstance = nodeCache.get(nodeExecution.getNodeKey());
            NodeExecutionDisplay executionDisplay = NodeExecutionDisplay.build(nodeExecution,nodeInstance);
            result.put(nodeExecution.getNodeKey(), executionDisplay);
            outMap.put(nodeInstance.simpleInfo(), nodeExecution.getNodeExecutionId());
        }

        if (log.isDebugEnabled()) {
            log.debug("node executions:{}", outMap);
            log.debug("execute path: {}", String.join("-->", outMap.keySet()));
        }
        NodeInstance nodeInstance = nodeCache.get(nodeKey);
        NodeExecutionDisplay executionDisplay = result.get(nodeKey);
        if (executionDisplay != null && nodeInstance != null && flowExecution.getStatus() == ExecutionStatus.COMPLETE) {
            if(CollectionUtils.isEmpty(nodeInstance.getOutputFields())){
                nodeInstance.setOutputFields(executionDisplay.getOutputFields());
            }
            workflowDraftOperator.saveFlowInstanceToCache(workflowBaseBody);

            workflowDraftOperator.saveCacheToDraft(workflowInstanceId);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        //update out field
        executionCache.invalidate(workflowInstanceId);
        return result;
//        return nodeExecutionDisplayReader.readExecutionByFlowExecutionId(workflowInstanceId,flowExecution.getFlowExecutionId(),null);
    }

    private List<Map<String, Object>> convertApiInput(List<Map<String, Object>> rawInputs,WorkflowBaseBody workflowBaseBody) {
        List<Map<String, Object>> nList = new ArrayList<>();
        NodeInstance node = workflowBaseBody.getNodes().stream().filter(ni-> "WEBHOOK".equals(ni.getCode())).findFirst().orElse(null);;
        if(node!=null){
            if(CollectionUtils.isNotEmpty(rawInputs)){
                for (Map<String, Object> rawInput : rawInputs) {
                    if(!rawInput.isEmpty()){
                        nList.add(convertToMap(rawInput));
                    }
                }//end for
            }
            if(nList.isEmpty()){
                Map<String,Object> rawInput = (Map<String, Object>) node.getData().get("webhook_config");
                nList.add(convertToMap(rawInput));
            }
        }
        return nList;
    }

    private Map<String,Object> convertToMap(Map<String,Object> rawInput){
        Map<String,Object> input = new LinkedHashMap<>();
        input.putAll(rawInput);
        if (rawInput.containsKey("contentType") &&
                (rawInput.containsKey("params") || rawInput.containsKey("headers") || rawInput.containsKey("body"))){
            Object v = rawInput.get("params");
            Map<String, Object> params = convertData(v);
            input.put("params", params);
            v = rawInput.get("headers");
            Map<String, Object> headers = convertData(v);
            input.put("headers", headers);
            v = rawInput.get("body");
            Map<String, Object> body = convertData(v);
            input.put("body", body);
        }
        return input;
    }

    private Map<String, Object> convertData(Object data) {
        Map<String, Object> mm = new HashMap<>();
        if (data instanceof List) {
            List<Map<String, Object>> item = (List<Map<String, Object>>) data;
            for (Map<String, Object> m : item) {
                mm.put(String.valueOf(m.get("name")), m.get("value"));
            }
        }
        return mm;
    }

    @Override
    public NodeExecutionDisplay execute(DebugPayload debugPayload) {
        return NodeDebugger.execute(debugPayload);
    }

    @Override
    public ExecutionResource updateTestFile(MultipartFile uploadFile, boolean force) {
        return NodeDebugger.updateTestFile(uploadFile, force);
    }


    @Override
    public FlowExecution currentExecuting(Long workflowInstanceId) {
        String flowExecutionId = executionCache.getIfPresent(workflowInstanceId);
        if (flowExecutionId == null) {
            return null;
        }
        return flowExecutionOperator.getFlowExecutionById(flowExecutionId, false);
    }

    @Override
    public Map<String, NodeExecutionDisplay> readNodeExecutions(Long workflowInstanceId, List<String> nodeKeys) {

        return nodeExecutionReader.readLatestNodeExecutionByFlowInstanceId(workflowInstanceId, 0, nodeKeys);
    }

    @Override
    public WorkflowResponse testWebhook(String flowKey, Map<String, Object> input) {
        return testWebhook(flowKey, Lists.newArrayList(input));
    }

    @Override
    public WorkflowResponse testWebhook(String flowKey, List<Map<String, Object>> inputs) {
        WorkflowBaseBody workflowBaseBody = workflowDraftOperator.getDraftWorkflow(flowKey);
        BaseFlowEngine flowEngine = (BaseFlowEngine) FlowEngineManager.eventFlowEngine();
        Long workflowInstanceId = workflowBaseBody.getWorkflowInstanceId();
        BuildProcessInfo buildProcessInfo = flowEngine.prepare(workflowInstanceId, 0, false);
        log.info("build info:{}", buildProcessInfo);
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setFlowKey(flowKey);
        workflowResponse.setRevision(0);
        if (buildProcessInfo.getStatus() != FlowStatus.LOADED) {
            log.error("flow prepare failed, {}", buildProcessInfo);
            workflowResponse.setResponseTime(LocalDateTime.now());
            return workflowResponse;
        }

        FlowExecution flowExecution = fillFlowExecution(inputs, workflowInstanceId);
        flowExecution.setRecordMode(RecordMode.ASYNC);

        WorkflowRuntimeContext workflowRuntimeContext = WorkflowRuntimeContext.build(flowExecution);

        //endNodeKey
        log.info("flow execution: {}", flowExecution);
        flowEngine.execute(flowExecution);
        Flow flow = flowEngine.flow(workflowInstanceId, 0);
        ApiTriggerNode triggerNode = (ApiTriggerNode) flow.startNodes().get(0);
        //update out field
        return new DefaultResponseBuilder().build(workflowRuntimeContext, triggerNode.getFlowWebhookConfig());
    }

    @Override
    public FlowExecution stop(String flowExecutionId) {
        IFlowEngine flowEngine = FlowEngineManager.eventFlowEngine();
        FlowExecution flowExecution = flowEngine.stop(flowExecutionId);
        if (flowExecution == null) {
            flowExecution = flowExecutionOperator.getFlowExecutionById(flowExecutionId, false);
            flowExecution.setStatus(ExecutionStatus.STOPPED);

            EventBusCenter.async(FlowExecutionTaskEvent.build(flowExecution));
        }
        return flowExecution;
    }

    @Override
    public FlowExecution stopByFlowKey(String flowKey) {
        return FlowEngineManager.eventFlowEngine().stopByFlowKey(flowKey);
    }

    private FlowExecution fillFlowExecution(List<Map<String, Object>> inputs, Long workflowInstanceId) {
        FlowExecution flowExecution = null;
        if (CollectionUtils.isEmpty(inputs) || inputs.get(0).isEmpty()) {
            FlowExecution lastFlowExecution = flowExecutionOperator.getLatestFlowExecution(workflowInstanceId, 0, true);
            if (lastFlowExecution != null) {
                flowExecution = FlowExecution.buildNewFlowExecution(
                        workflowInstanceId, 0, false, false);
                flowExecution.setInput(lastFlowExecution.getInput());
            }
        }

        if (flowExecution == null) {
            flowExecution = FlowExecution.buildNewFlowExecution(
                    workflowInstanceId, 0, false, false, inputs);
        }
        flowExecution.setRecordMode(RecordMode.SYNC);
        return flowExecution;
    }
}
