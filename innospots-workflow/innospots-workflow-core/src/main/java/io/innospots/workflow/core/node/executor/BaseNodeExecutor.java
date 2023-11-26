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
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.jit.MethodBody;
import io.innospots.workflow.core.enums.BuildStatus;
import io.innospots.workflow.core.execution.*;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.events.NodeExecutionTaskEvent;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionResource;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
public abstract class BaseNodeExecutor implements INodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BaseNodeExecutor.class);

    public static final String FIELD_SCRIPT_TYPE = "script_type";
    public static final String FIELD_ACTION_SCRIPT = "action_script";

    protected List<INodeExecutionListener> nodeExecutionListeners;

    protected BuildStatus buildStatus = BuildStatus.NONE;

    protected Exception buildException;

    protected NodeInstance ni;

    protected String flowIdentifier;


    protected abstract void initialize();


    public BaseNodeExecutor build() {
        buildStatus = BuildStatus.BUILDING;
        try {
            initialize();
            buildStatus = BuildStatus.DONE;
        } catch (Exception e) {
            logger.error("node build fail, nodeKey:{}, {}", this.nodeKey(), e.getMessage());
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
        flowExecution.addNodeExecution(nodeExecution);
        if (this.buildStatus != BuildStatus.DONE) {
            nodeExecution.end(buildException.getMessage(), ExecutionStatus.FAILED, false);
        } else {
            nodeExecution.setInputs(this.buildExecutionInput(flowExecution));
        }
        return nodeExecution;
    }

    @Override
    public NodeExecution execute(FlowExecution flowExecution) {
        flowExecution.resetCurrentNodeKey(this.nodeKey());
        NodeExecution nodeExecution = prepare(flowExecution);
        if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
            after(nodeExecution);
        } else {
            before(nodeExecution);
            nodeExecution.setStatus(ExecutionStatus.STARTING);
            if (ni.isAsync()) {
                AsyncExecutors.execute(() -> innerExecute(nodeExecution, flowExecution));
            } else {
                innerExecute(nodeExecution, flowExecution);
            }

        }
        end(nodeExecution, flowExecution);

        return nodeExecution;
    }


    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        invoke(nodeExecution);
    }

    protected Object processItem(Map<String, Object> item) {
        return item;
    }

    protected ExecutionResource processResource(ExecutionResource resource) {
        return resource;
    }

    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = buildOutput(nodeExecution);

        if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                if (CollectionUtils.isNotEmpty(executionInput.getData())) {
                    for (Map<String, Object> item : executionInput.getData()) {
                        Object result = processItem(item);
                        processOutput(result, nodeOutput);
                    }//end for
                } else {
                    Object result = processItem(null);
                    processOutput(result, nodeOutput);
                }
                if (CollectionUtils.isNotEmpty(executionInput.getResources())) {
                    List<ExecutionResource> outputResources = new ArrayList<>();
                    for (int i = 0; i < executionInput.getResources().size(); i++) {
                        ExecutionResource resource = processResource(executionInput.getResources().get(i));
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
            processOutput(result, nodeOutput);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("node execution, nodeOutput:{} {}", nodeOutput, nodeExecution);
        }
    }


    public void innerExecute(NodeExecution nodeExecution) {
        this.innerExecute(nodeExecution, null);
    }

    public void innerExecute(NodeExecution nodeExecution, FlowExecution flowExecution) {
        boolean isFail;
        nodeExecution.setStatus(ExecutionStatus.RUNNING);
        int tryTimes = 0;
        String msg = "";
        do {
            isFail = false;
            try {
                invoke(nodeExecution, flowExecution);
                if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
                    isFail = true;
                }
            } catch (Exception e) {
                isFail = true;
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
            nodeExecution.setNext(true);
        }
        if (nodeExecution.getStatus() == null) {
            nodeExecution.end(msg, ExecutionStatus.COMPLETE, true);
        } else if (nodeExecution.getStatus().isDone()) {
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
    protected void processOutput(Object result, NodeOutput nodeOutput) {
        if (result == null) {
            return;
        }
        if (result instanceof Map) {
            Map<String, Object> respMap = (Map<String, Object>) result;
            nodeOutput.addResult(respMap);
        } else if (result instanceof Collection) {
            Collection resCol = (Collection) result;
            nodeOutput.addResult(resCol);

        } else {
            if (CollectionUtils.isNotEmpty(ni.getOutputFields())) {
                for (ParamField outputField : ni.getOutputFields()) {
                    Map<String, Object> res = new HashMap<>();
                    res.put(outputField.getCode(), result);
                    nodeOutput.addResult(res);
                    break;
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
        if (nodeExecutionListeners != null) {
            for (INodeExecutionListener executionListener : nodeExecutionListeners) {
                executionListener.start(nodeExecution);
            }
        }
    }

    protected void after(NodeExecution nodeExecution) {
        nodeExecution.fillTotal();
        if (nodeExecutionListeners != null) {
            for (INodeExecutionListener nodeExecutionListener : nodeExecutionListeners) {
                if (nodeExecution.getStatus() == ExecutionStatus.COMPLETE) {
                    nodeExecutionListener.complete(nodeExecution);
                } else if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
                    nodeExecutionListener.fail(nodeExecution);
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
        EventBusCenter.getInstance().asyncPost(NodeExecutionTaskEvent.build(flowExecution, nodeExecution));
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
        validFieldConfig(field);
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

    protected NodeOutput buildOutput(NodeExecution execution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        execution.addOutput(nodeOutput);
        return nodeOutput;
    }


    public List<String> nextNodeKeys() {
        return ni.getNextNodeKeys();
    }

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


    public Map<String, List<String>> nodeAnchors() {
        return ni.getNodeAnchors();
    }

    @Override
    public String nodeKey() {
        return ni.getNodeKey();
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public Exception getBuildException() {
        return buildException;
    }


    public MethodBody buildScriptMethodBody() {
        String src = this.valueString(FIELD_SCRIPT_TYPE);
        String scriptType = scriptType();
        if (StringUtils.isEmpty(src) || StringUtils.isEmpty(scriptType)) {
            return null;
        }
        MethodBody methodBody = new MethodBody();
        methodBody.setReturnType(Object.class);
        methodBody.setScriptType(scriptType);
        if (CollectionUtils.isNotEmpty(ni.getInputFields())) {
            methodBody.setParams(ni.getInputFields());
        }
        methodBody.setMethodName(ni.expName());
        methodBody.setSrcBody(src);
        return methodBody;
    }

    protected String scriptType() {
        return this.valueString(FIELD_SCRIPT_TYPE);
    }

}
