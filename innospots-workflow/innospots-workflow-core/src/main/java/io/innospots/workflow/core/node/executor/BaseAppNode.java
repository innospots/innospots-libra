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
import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.re.ExpressionEngineFactory;
import io.innospots.base.re.IExpression;
import io.innospots.base.re.IExpressionEngine;
import io.innospots.base.re.jit.MethodBody;
import io.innospots.workflow.core.enums.BuildStatus;
import io.innospots.workflow.core.execution.*;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.builder.INodeBuilder;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
public abstract class BaseAppNode implements INodeBuilder, INodeExecutor {


    private static final Logger logger = LoggerFactory.getLogger(BaseAppNode.class);

    protected List<INodeExecutionListener> nodeExecutionListeners;


    protected BuildStatus buildStatus = BuildStatus.NONE;

    protected Exception buildException;

    protected IExpression<Object> expression;

    protected Map<String, IExpression<Object>> actionScripts;

    protected NodeInstance ni;

    public List<String> nextNodeKeys() {
        return ni.getNextNodeKeys();
    }

    public List<String> prevNodeKeys() {
        return ni.getPrevNodeKeys();
    }

    public String simpleInfo() {
        return ni.simpleInfo();
    }

    public String nodeCode(){
        return ni.getCode();
    }

    public String nodeType() {
        return ni.getNodeType();
    }


    public Map<String, List<String>> nodeAnchors() {
        return ni.getNodeAnchors();
    }

    public static BaseAppNode buildAppNode(String flowIdentifier, NodeInstance nodeInstance) {
        BaseAppNode appNode = null;
        try {
            appNode = newInstance(nodeInstance);
            appNode.build(flowIdentifier, nodeInstance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            logger.error(e.getMessage(),e);
//            throw InnospotException.buildException(BaseAppNode.class, ResponseCode.INITIALIZING, e);
        }
        return appNode;
    }

    public static BaseAppNode newInstance(NodeInstance nodeInstance) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        BaseAppNode appNode = null;
        appNode = (BaseAppNode) Class.forName(nodeInstance.getNodeType()).getDeclaredConstructor().newInstance();
        appNode.ni = nodeInstance;
        return appNode;
    }


    protected void initialize(NodeInstance nodeInstance) {
        //this.ni = nodeInstance;
        //this.fillNodeInfo(nodeInstance);
    }

    @Override
    public void build(String flowIdentifier, NodeInstance nodeInstance) {
        buildStatus = BuildStatus.BUILDING;
        try {
            initialize(nodeInstance);
            buildExpression(flowIdentifier, nodeInstance);
            buildStatus = BuildStatus.DONE;
        } catch (Exception e) {
            logger.error("node build fail, nodeKey:{}, {}", this.nodeKey(), e.getMessage());
            buildStatus = BuildStatus.FAIL;
            buildException = e;
            //throw e;
        }

    }

    protected void buildExpression(String flowIdentifier, NodeInstance nodeInstance) throws ScriptException {
        List<MethodBody> methodBodies = nodeInstance.expMethods();
        if (methodBodies.isEmpty()) {
            return;
        }
        this.actionScripts = new HashMap<>();
        for (MethodBody methodBody : methodBodies) {
            IExpressionEngine expressionEngine = ExpressionEngineFactory.getEngine(flowIdentifier, methodBody.getScriptType());
            if (expressionEngine == null) {
                logger.error("expression engine is null, identifier:{}, scriptType:{}", flowIdentifier, methodBody.getScriptType());
                this.buildStatus = BuildStatus.FAIL;
                continue;
            }
            IExpression exp = expressionEngine.getExpression(methodBody.getMethodName());
            if (NodeInstance.FIELD_DEFAULT_ACTION.equals(methodBody.getFormName())) {
                this.expression = exp;
            } else {
                this.actionScripts.put(methodBody.getFormName(), exp);
            }
        }
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

    public void addNodeExecutionListener(INodeExecutionListener nodeExecutionListener) {
        if (this.nodeExecutionListeners == null) {
            this.nodeExecutionListeners = new ArrayList<>();
        }
        this.nodeExecutionListeners.add(nodeExecutionListener);
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

    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        invoke(nodeExecution);
    }

    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();

        if (expression != null) {
            nodeOutput.addNextKey(ni.getNextNodeKeys());
            if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
                for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                    if (CollectionUtils.isNotEmpty(executionInput.getData())) {
                        for (Map<String, Object> data : executionInput.getData()) {
                            Object result = expression.execute(data);
                            processOutput(result, nodeOutput);
                        }//end for
                    } else {
                        Object result = expression.execute();
                        processOutput(result, nodeOutput);
                    }

                }//end execution input
            } else {
                Object result = expression.execute();
                processOutput(result, nodeOutput);
            }
            nodeExecution.addOutput(nodeOutput);
        } else {//end if
            nodeOutput.addNextKey(ni.getNextNodeKeys());
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                nodeOutput.addResult(executionInput.getData());
            }//end execution input
            nodeExecution.addOutput(nodeOutput);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("node execution, nodeOutput:{} {}", nodeOutput, nodeExecution);
        }
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

    protected void end(NodeExecution nodeExecution, FlowExecution flowExecution) {
        if (nodeExecution.getStatus() == ExecutionStatus.PENDING) {
            flowExecution.setStatus(nodeExecution.getStatus());
        } else if (nodeExecution.getStatus() == ExecutionStatus.FAILED) {
            flowExecution.setStatus(ExecutionStatus.FAILED);
            flowExecution.setMessage(nodeExecution.getMessage());
        }
        EventBusCenter.getInstance().asyncPost(NodeExecutionTaskEvent.build(flowExecution,nodeExecution));
//        flowExecution.addNodeExecution(nodeExecution);
    }


    public void innerExecute(NodeExecution nodeExecution) {
        this.innerExecute(nodeExecution, null);
    }

    public void innerExecute(NodeExecution nodeExecution, FlowExecution flowExecution) {
        boolean isFail = false;
        ExecutionStatus status;
        nodeExecution.setStatus(ExecutionStatus.RUNNING);
        int tryTimes = 0;
        String msg = "";
        boolean next;
        do {
            if(this.buildStatus == BuildStatus.FAIL){
                isFail = true;
                if(this.buildException !=null){
                    msg = ExceptionUtil.stacktraceToString(this.buildException,2048);
                }
                break;
            }
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
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } while (isFail && ni.isRetryOnFail() && tryTimes < ni.getMaxTries());

        next = !isFail || ni.isContinueOnFail();
        if (isFail) {
            status = ExecutionStatus.FAILED;
        } else {
            status = ExecutionStatus.COMPLETE;
        }
        // not update node status when the node execute (exp: IntervalNode, node status will be updated)
        if (nodeExecution.getStatus().equals(ExecutionStatus.RUNNING)) {
            nodeExecution.setStatus(status);
        }

        //process result into nodeOutput, and save to nodeExecution
        //processResult(result, nodeExecution, flowExecution);

        if (next) {
            processNextKeys(nodeExecution);
        }

        if (nodeExecution.getStatus() == ExecutionStatus.FAILED || nodeExecution.getStatus() == ExecutionStatus.COMPLETE) {
            nodeExecution.end(msg, status, next);
        }

        //after process node execution
        after(nodeExecution);
    }

    @Override
    public String nodeKey() {
        return ni.getNodeKey();
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    protected Integer valueInteger(String field) {
        validFieldConfig(field);
        return ni.valueInteger(field);
    }

    protected Object value(String field) {
        validFieldConfig(field);
        return ni.value(field);
    }

    protected String valueString(String field) {
        validFieldConfig(field);
        return ni.valueString(field);
    }

    protected Long valueLong(String field) {
        validFieldConfig(field);
        return ni.valueLong(field);
    }

    protected Boolean valueBoolean(String field) {
        validFieldConfig(field);
        return ni.valueBoolean(field);
    }

    protected Map<String, Object> valueMap(String field) {
        validFieldConfig(field);
        return ni.valueMap(field);
    }

    protected void validFieldConfig(String field) {
        if (!ni.containsKey(field)) {
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + nodeKey() + ", field:" + field);
        }
    }

    protected void validFieldConfig(NodeInstance nodeInstance, String field) {
        if (!nodeInstance.containsKey(field)) {
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

    public Exception getBuildException() {
        return buildException;
    }

    protected NodeOutput buildOutput(NodeExecution execution){
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        execution.addOutput(nodeOutput);
        return nodeOutput;
    }

    /*
    protected IExpression getDefaultExpression() {
        if (this.actionScripts != null) {
            if (this.actionScripts.containsKey(NodeInstance.FIELD_DEFAULT_ACTION)) {
                return this.actionScripts.get(NodeInstance.FIELD_DEFAULT_ACTION);
            }else if (this.actionScripts.size() == 1) {
                return this.actionScripts.values().iterator().next();
            }
        }
        return null;
    }
    */

}
