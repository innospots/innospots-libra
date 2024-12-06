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

package io.innospots.workflow.core.node.executor;


import cn.hutool.core.exceptions.ExceptionUtil;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.script.base.ExecutorManagerFactory;
import io.innospots.script.base.ScriptExecutorManager;
import io.innospots.script.base.jit.MethodBody;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.enums.BuildStatus;
import io.innospots.workflow.core.execution.AsyncExecutors;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.logger.FlowLoggerFactory;
import io.innospots.workflow.core.logger.IFlowLogger;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
public abstract class BaseNodeExecutor implements INodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BaseNodeExecutor.class);

    public static final String FIELD_SCRIPT_TYPE = "script_type";
    public static final String FIELD_ACTION_SCRIPT = "action_script";
    public static final String FIELD_ACTION = "actionScript";

    protected List<INodeExecutionListener> nodeExecutionListeners;

    @Getter
    protected BuildStatus buildStatus = BuildStatus.NONE;

    @Getter
    protected Exception buildException;

    @Setter
    protected NodeInstance ni;

    protected String flowIdentifier;

    @Setter
    protected IFlowLogger flowLogger;


    protected abstract void initialize();

    public List<ParamField> inputFields(){
        return ni.getInputFields();
    }

    public List<ParamField> outputFields(){
        return ni.getOutputFields();
    }


    public BaseNodeExecutor build() {
        buildStatus = BuildStatus.BUILDING;
        try {
            if (flowLogger == null) {
                flowLogger = FlowLoggerFactory.getLogger();
            }
            flowLogger.flowInfo("build node executor: {}-{}", ni.getCode(), ni.getName());
            initialize();
            buildStatus = BuildStatus.DONE;
        } catch (Exception e) {
            flowLogger.flowError("node build fail, nodeKey:{}, {},err: {}", this.nodeKey(), e.getMessage(), ExceptionUtil.stacktraceToString(e, 1024));
            logger.error("node build fail, nodeKey:{}, {},err: {}", this.nodeKey(), e.getMessage(), ExceptionUtil.stacktraceToString(e, 1024));
            buildStatus = BuildStatus.FAIL;
            buildException = e;
            //throw e;
        }
        return this;
    }


    protected NodeExecution prepare(FlowExecution flowExecution) {

        NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution(
                nodeKey(),
                flowExecution);
        nodeExecution.setNodeCode(ni.getCode());
        nodeExecution.setNodeName(ni.getName());
        flowExecution.addNodeExecution(nodeExecution);
        if (this.buildStatus != BuildStatus.DONE) {
            nodeExecution.end(buildException != null ? buildException.getMessage() : "build fail", ExecutionStatus.FAILED);
        } else {
            nodeExecution.setInputs(this.buildExecutionInput(flowExecution));
        }
        return nodeExecution;
    }

    @Override
    public NodeExecution execute(FlowExecution flowExecution) {
        CCH.executionId(flowExecution.getFlowExecutionId());
        if (flowLogger == null) {
            flowLogger = FlowLoggerFactory.getLogger();
        }
        flowExecution.resetCurrentNodeKey(this.nodeKey());
        NodeExecution nodeExecution = prepare(flowExecution);
        if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
            after(nodeExecution);
        } else {
            before(nodeExecution);
            if (ni.isAsync()) {
                AsyncExecutors.execute(() -> innerExecute(nodeExecution, flowExecution));
            } else {
                innerExecute(nodeExecution, flowExecution);
            }

        }
        CCH.executionId(flowExecution.getFlowExecutionId());
//        CCH.sessionId(flowExecution.getFlowExecutionId());
        end(nodeExecution, flowExecution);
        CCH.removeExecutionId();
        return nodeExecution;
    }

    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        invoke(nodeExecution);
    }

    protected Object processItem(Map<String, Object> item) {
        return processItem(item, null);
    }

    protected Object processItem(Map<String, Object> item, NodeExecution nodeExecution) {
        return item;
    }

    protected ExecutionResource processResource(ExecutionResource resource) {
        return resource;
    }

    protected ExecutionResource processResource(ExecutionResource resource, NodeExecution nodeExecution) {
        return resource;
    }

    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = buildOutput(nodeExecution);

        if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                flowLogger.flowInfo("key: {}, name: {}, node input: {}", nodeExecution.getNodeKey(),this.nodeName(), executionInput.log().toString());
                if (CollectionUtils.isNotEmpty(executionInput.getData())) {
                    for (Map<String, Object> item : executionInput.getData()) {
                        Object result = processItem(item, nodeExecution);
                        processOutput(nodeExecution, result, nodeOutput);
                    }//end for
                } else {
                    Object result = processItem(null, nodeExecution);
                    processOutput(nodeExecution, result, nodeOutput);
                }
                if (CollectionUtils.isNotEmpty(executionInput.getResources())) {
                    List<ExecutionResource> outputResources = new ArrayList<>();
                    for (int i = 0; i < executionInput.getResources().size(); i++) {
                        ExecutionResource resource = processResource(executionInput.getResources().get(i), nodeExecution);
                        outputResources.add(resource);
                    }
                    outputResources = IExecutionContextOperator.saveExecutionResources(outputResources, nodeExecution.getContextDataPath());
                    for (int i = 0; i < outputResources.size(); i++) {
                        nodeOutput.addResource(i, outputResources.get(i));
                    }
                }
            }//end execution input
        } else {
            Object result = processItem(null);
            processOutput(nodeExecution, result, nodeOutput);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("node execution, nodeOutput:{} {}", nodeOutput, nodeExecution);
        }
    }

    public void innerExecute(NodeExecution nodeExecution, FlowExecution flowExecution) {
        CCH.executionId(flowExecution.getFlowExecutionId());
        boolean isFail;
        nodeExecution.setStatus(ExecutionStatus.RUNNING);
        flowLogger.flowInfo("execution: {}, key: {}, name: {}", nodeExecution.getStatus(), this.ni.getNodeKey(), this.ni.getName());
        int tryTimes = 0;
        String msg = "";
        do {
            isFail = false;
            try {
                invoke(nodeExecution, flowExecution);
                List<ExecutionOutput> nodeOutputs = nodeExecution.getOutputs();
                if (CollectionUtils.isNotEmpty(nodeOutputs)) {
                    String outMsg = nodeOutputs.stream().map(out -> out.log().toString()).collect(Collectors.joining(" <=> "));
                    flowLogger.nodeInfo(this.nodeKey(), this.nodeName(), "outputs:{}", outMsg);
                } else {
                    flowLogger.nodeInfo(this.nodeKey(), this.nodeName(), "outputs:{}", "empty.");
                }
                if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
                    isFail = true;
                }
            } catch (Throwable e) {
                isFail = true;
                flowLogger.nodeError(this.nodeKey(), this.ni.getName(), "err:{}", e.getMessage());
                logger.error("node inner execute error:{}", nodeExecution, e);
                nodeExecution.clearOutput();
                msg = ExceptionUtil.stacktraceToString(e, 2048);
            }

            if (isFail && ni.isRetryOnFail()) {
                tryTimes++;
                if (ni.getRetryWaitTimeMills() > 0) {
                    try {
                        Thread.sleep(ni.getRetryWaitTimeMills());
                    } catch (InterruptedException ex) {
                        logger.error(ex.getMessage());
                    }
                }
            }
        } while (isFail && BuildStatus.FAIL != this.buildStatus && ni.isRetryOnFail() && tryTimes < ni.getMaxTries());

        // not update node status when the node execute (exp: IntervalNode, node status will be updated)

        if (isFail && nodeExecution.getStatus() != ExecutionStatus.FAILED) {
            nodeExecution.setStatus(ExecutionStatus.FAILED);
        }

        if (!isFail || ni.isContinueOnFail()) {
            processNextKeys(nodeExecution);
            if(nodeExecution.getNext() == null){
                nodeExecution.setNext(true);
            }
        }
        boolean isDone = nodeExecution.getStatus() != null && nodeExecution.getStatus().isDone();
        if (nodeExecution.getStatus() == null || !isDone) {
            nodeExecution.end(msg, ExecutionStatus.COMPLETE);
        } else {
            nodeExecution.end(msg);
        }
        //after process node execution
        after(nodeExecution);
    }

    @Override
    public List<ExecutionInput> buildExecutionInput(FlowExecution flowExecution) {
        return flowExecution.getInputs(ni.getPrevNodeKeys(), this.nodeKey());
    }

    //    @Override
    protected void processOutput(NodeExecution nodeExecution, Object result, ExecutionOutput nodeOutput) {
        if (result == null) {
            return;
        }
        if (result instanceof Map) {
            Map<String, Object> respMap = (Map<String, Object>) result;
            //flowLogger.item(nodeExecution.getFlowExecutionId(), nodeExecution.getNodeExecutionId(), respMap);
            nodeOutput.addResult(respMap);
        } else if (result instanceof Collection) {
            Collection resCol = (Collection) result;
            /*
            for (Object o : resCol) {
                flowLogger.item(nodeExecution.getFlowExecutionId(), nodeExecution.getNodeExecutionId(), (Map<String, Object>) o);
            }
             */

            nodeOutput.addResult(resCol);

        } else {
            if (CollectionUtils.isNotEmpty(ni.getOutputFields())) {
                if (result instanceof Map) {
                    Map<String, Object> res = new HashMap<>();
                    Map<String, Object> item = (Map<String, Object>) result;
                    for (ParamField outputField : ni.getOutputFields()) {
                        res.put(outputField.getCode(), item.get(outputField.getCode()));
                    }
                    //flowLogger.item(nodeExecution.getFlowExecutionId(), nodeExecution.getNodeExecutionId(), res);
                    nodeOutput.addResult(res);
                } else {
                    Map<String, Object> res = new HashMap<>();
                    for (ParamField outputField : ni.getOutputFields()) {
                        res.put(outputField.getCode(), result);
                        break;
                    }
                    //flowLogger.item(nodeExecution.getFlowExecutionId(), nodeExecution.getNodeExecutionId(), res);
                    nodeOutput.addResult(res);
                }

            } else {
                logger.warn("node: {}, The output field is not set, the result of the node is not a Map structure, the result is not saved in the node execution. The output result type of the node is: {}",
                        this.nodeKey(), result.getClass().getName());
            }
        }
    }

    @Override
    public void processNextKeys(NodeExecution nodeExecution) {
        nodeExecution.setNextNodeKeys(ni.getNextNodeKeys());
    }


    public void addNodeExecutionListener(List<INodeExecutionListener> nodeExecutionListeners) {
        if (nodeExecutionListeners == null) {
            return;
        }
        if (this.nodeExecutionListeners == null) {
            this.nodeExecutionListeners = new ArrayList<>();
        }
        this.nodeExecutionListeners.addAll(nodeExecutionListeners);
    }

    protected void before(NodeExecution nodeExecution) {
        nodeExecution.setStatus(ExecutionStatus.STARTING);
        if (nodeExecutionListeners != null) {
            for (INodeExecutionListener executionListener : nodeExecutionListeners) {
                executionListener.start(nodeExecution);
            }
        }
        flowLogger.nodeStatus(nodeExecution.getStatus().name(), this.ni.getNodeKey(), this.ni.getName());
    }

    protected void after(NodeExecution nodeExecution) {
        nodeExecution.fillTotal();
        nodeExecution.setSchemaFields(ni.getOutputFields());
        if (nodeExecutionListeners != null) {
            for (INodeExecutionListener nodeExecutionListener : nodeExecutionListeners) {
                if (nodeExecution.getStatus() == ExecutionStatus.COMPLETE) {
                    nodeExecutionListener.complete(nodeExecution);
                } else if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
                    nodeExecutionListener.fail(nodeExecution);
                } else if(nodeExecution.getStatus().isDone()){
                    nodeExecutionListener.complete(nodeExecution);
                } else {
                    logger.error("nodeExecutionListeners other status:{} nodeExecution:{}", nodeExecution.getStatus(), nodeExecution);
                }
            }
        }
    }


    protected void end(NodeExecution nodeExecution, FlowExecution flowExecution) {
        if (nodeExecution.getStatus() == ExecutionStatus.PENDING) {
            flowExecution.setStatus(nodeExecution.getStatus());
        } else if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
            flowExecution.setStatus(ExecutionStatus.FAILED);
            flowExecution.setMessage(nodeExecution.getMessage());
        }
        flowLogger.nodeStatus(nodeExecution.getStatus().name(), this.ni.getNodeKey(), this.ni.getName());

        //EventBusCenter.getInstance().asyncPost(NodeExecutionTaskEvent.build(flowExecution, nodeExecution));
    }


    protected Integer valueInteger(String field) {
        return ni.valueInteger(field);
    }

    protected Object value(String field) {
        return ni.value(field);
    }

    protected String valueString(String field) {
        return ni.valueString(field);
    }

    protected Double valueDouble(String key) {
        return ni.valueDouble(key);
    }

    protected Long valueLong(String field) {
        return ni.valueLong(field);
    }

    protected Boolean valueBoolean(String field) {
        return ni.valueBoolean(field);
    }

    protected Map<String, Object> valueMap(String field) {
        return ni.valueMap(field);
    }

    protected Integer validInteger(String field) {
        validFieldConfig(field);
        return ni.valueInteger(field);
    }

    protected List<Map<String, Object>> valueMapList(String field) {
        //validFieldConfig(field);
        return (List<Map<String, Object>>) ni.value(field);
    }


    protected Object validObject(String field) {
        validFieldConfig(field);
        return ni.value(field);
    }

    protected String validString(String field) {
        validFieldConfig(field);
        return ni.valueString(field);
    }

    protected Long validLong(String field) {
        validFieldConfig(field);
        return ni.valueLong(field);
    }

    protected Map<String, Object> validMap(String field) {
        validFieldConfig(field);
        return ni.valueMap(field);
    }


    protected Boolean validBoolean(String field) {
        validFieldConfig(field);
        return ni.valueBoolean(field);
    }

    protected void validFieldConfig(String field) {
        if (!ni.containsKey(field)) {
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + nodeKey() + ", field:" + field);
        }
    }


    protected void validSourceNodeSize(int size) {
        if (ni.getPrevNodeKeys() == null || ni.getPrevNodeKeys().size() < size) {
            int v = ni.getPrevNodeKeys() == null ? 0 : ni.getPrevNodeKeys().size();
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + nodeKey() + ", sourceNode size expected value is:" + size + ", actually the size is " + v);
        }
    }

    protected void validInputs(List<ExecutionInput> inputs, int size) {
        if (CollectionUtils.isEmpty(inputs) || inputs.size() < 2) {
            int v = inputs == null ? 0 : inputs.size();
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + nodeKey() + ", sourceNode size expected value is:" + size + ", actually the size is " + v);
        }
    }

    protected ExecutionOutput buildOutput(NodeExecution execution) {
        ExecutionOutput nodeOutput = new ExecutionOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        execution.addOutput(nodeOutput);
        return nodeOutput;
    }


    @Override
    public List<String> nextNodeKeys() {
        return ni.getNextNodeKeys() == null ? Collections.emptyList() : ni.getNextNodeKeys();
    }

    @Override
    public List<String> prevNodeKeys() {
        return ni.getPrevNodeKeys();
    }

    public String simpleInfo() {
        return ni.simpleInfo();
    }

    public String nodeCode() {
        return ni.getCode();
    }

    public String nodeType() {
        return ni.getNodeType();
    }


    @Override
    public Map<String, List<String>> nodeAnchors() {
        return ni.getNodeAnchors();
    }

    @Override
    public String nodeKey() {
        return ni.getNodeKey();
    }

    protected String nodeName() {
        return ni.getName();
    }

    public int timeoutMills() {
        return ni.getTimeoutMills();
    }


    public List<MethodBody> buildScriptMethods() {
        String src = this.valueString(FIELD_ACTION_SCRIPT);
        String scriptType = scriptType();
        if (StringUtils.isEmpty(src) || StringUtils.isEmpty(scriptType)) {
            return Collections.emptyList();
        }
        MethodBody methodBody = new MethodBody();
        methodBody.setReturnType(Object.class);
        methodBody.setScriptType(scriptType);
        /*
        if (CollectionUtils.isNotEmpty(ni.getInputFields())) {
            methodBody.setParams(ni.getInputFields());
        }
         */
        methodBody.setMethodName(ni.expName());
        methodBody.setSrcBody(src);
        return List.of(methodBody);
    }


    protected String scriptType() {
        return this.valueString(FIELD_SCRIPT_TYPE);
    }

    protected ScriptExecutorManager executorManager() {
        ScriptExecutorManager executorManager = ExecutorManagerFactory.getCache(this.flowIdentifier);
        if (executorManager == null) {
            throw ScriptException.buildExecutorException(this.getClass(), this.scriptType(), "script executor manager is null,", this.flowIdentifier);
        }
        return executorManager;
    }

    protected ExecutionResource saveResourceToLocal(byte[] fileBytes, String fileName, NodeExecution nodeExecution) {
        return IExecutionContextOperator.buildExecutionResource(fileBytes, fileName, nodeExecution.getContextDataPath());
    }

    protected ExecutionResource saveResourceToLocal(String url, String subFix, NodeExecution nodeExecution) throws MalformedURLException {
        return IExecutionContextOperator.buildExecutionResource(url, subFix, nodeExecution.getContextDataPath());
    }

    protected <T> T getBean(Class<T> clazz) {
        return BeanContextAwareUtils.getBean(clazz);
    }

}
