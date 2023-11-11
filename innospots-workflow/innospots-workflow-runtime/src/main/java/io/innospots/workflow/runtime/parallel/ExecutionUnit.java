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

package io.innospots.workflow.runtime.parallel;

import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseAppNode;
import io.innospots.workflow.runtime.flow.Flow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/10/3
 */
@Slf4j
public class ExecutionUnit {

    private final BaseAppNode nodeExecutor;

    private final FlowExecution flowExecution;

    private final Executor threadExecutor;
//    private final CompletionService<NodeExecution> completionService;

    private final Flow flow;

    private final Map<String, ExecutionUnit> executionUnits;

    private CompletableFuture<NodeExecution> future;
//    private Future<NodeExecution> future;

    private NodeExecution nodeExecution;

    private ExecutionStatus unitStatus;

    private String threadName;

    private ExecutionUnit(BaseAppNode nodeExecutor, Map<String, ExecutionUnit> executionUnits, FlowExecution flowExecution, Flow flow, Executor executor) {
        this.nodeExecutor = nodeExecutor;
        this.flowExecution = flowExecution;
        this.threadExecutor = executor;
        this.flow = flow;
        this.executionUnits = executionUnits;
        unitStatus = ExecutionStatus.READY;
        this.executionUnits.put(this.nodeKey(), this);
    }

    public static ExecutionUnit build(BaseAppNode nodeExecutor, Map<String, ExecutionUnit> executionUnits, FlowExecution flowExecution, Flow flow, Executor threadExecutor) {
        return new ExecutionUnit(nodeExecutor, executionUnits, flowExecution, flow, threadExecutor);
    }

    private ExecutionUnit build(BaseAppNode nodeExecutor) {
        return new ExecutionUnit(nodeExecutor, executionUnits, flowExecution, flow, threadExecutor);
    }

    public void run(boolean async) {
        if (flowExecution.shouldStopped()) {
            return;
        }
        if (async) {
            future = CompletableFuture.supplyAsync(this::execute, threadExecutor);
        } else {
            future = CompletableFuture.completedFuture(this.execute());
//            nodeExecution = this.execute();
        }
        if (log.isDebugEnabled()) {
            log.debug("run node:{}, async:{}, isDone:{}, hashcode:{}", this.nodeKey(), async, future.isDone(), future.hashCode());
        }
    }

    public boolean isTimeout() {

        return false;
    }

    public ExecutionStatus unitStatus() {
        return this.unitStatus;
    }

    public void cancel() {
        if (!future.isDone() && !future.isCancelled()) {
            future.cancel(true);
        }
    }

    public String threadName() {
        return threadName;
    }

    public boolean isTaskDone() {
        return this.future != null && (this.future.isDone() || future.isCancelled());
    }

    public boolean isDone() {
        if (future != null) {
            //block until the node execute complete.
            try {
                nodeExecution = future.get();
                if (nodeExecution == null) {
                    future = null;
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage(), e);
            }

        }
        if (nodeExecution == null) {
            this.nodeExecution = flowExecution.getNodeExecution(this.nodeKey());
        }
        boolean done = false;
        if (nodeExecution != null) {
            done = nodeExecution.getStatus().isDone();
        }
        if (log.isDebugEnabled()) {
            if(!done){
                log.debug("future out key: {}, status:{}, {}", this.nodeKey(), unitStatus, nodeExecution);
            }
        }
        return done;
    }

    public String nodeKey() {
        return nodeExecutor.nodeKey();
    }

    private NodeExecution execute() {
        unitStatus = ExecutionStatus.STARTING;
        try {
            //Check whether the previous nodes has bean executed completely
            boolean b = executePreviousNodes();
            if (!b) {
                unitStatus = ExecutionStatus.NOT_PREPARED;
                log.warn("execution unit quit, the pre-execution dependency condition is not met, node:{}", nodeKey());
                //this.executionUnits.remove(nodeKey());
                return null;
            }
            unitStatus = ExecutionStatus.RUNNING;
            if (flowExecution.isExecuted(nodeKey())) {
                nodeExecution = flowExecution.getNodeExecution(nodeKey());
                return nodeExecution;
            }
            nodeExecution = nodeExecutor.execute(flowExecution);
            unitStatus = nodeExecution.getStatus();
            this.threadName = Thread.currentThread().getName();

            if (log.isDebugEnabled()) {
                log.debug("execute done unit node: {}, sequence:{}", this.nodeExecutor.simpleInfo(), nodeExecution.getSequenceNumber());
            }
            if (flowExecution.getEndNodeKey() != null && flowExecution.getEndNodeKey().equals(nodeExecutor.nodeKey())) {
                //unitStatus = nodeExecution.getStatus();
                //set target nodeKey that will execute to this node
                return nodeExecution;
            }
            List<String> nextNodes = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(nodeExecution.getNextNodeKeys())) {
                nextNodes.addAll(nodeExecution.getNextNodeKeys());
                nextNodes = nextNodes.stream().filter(flowExecution::shouldExecute).collect(Collectors.toList());
            }

            if (nodeExecution.getStatus() == ExecutionStatus.PENDING) {
                if (log.isDebugEnabled()) {
                    log.debug("node execution status is pending, nodeKey:{}", nodeExecution.getNodeKey());
                }
                //unitStatus = nodeExecution.getStatus();
                return nodeExecution;
            }

            if (nextNodes.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("the node is the leaf node, node key:{}", this.nodeKey());
                }
                //unitStatus = nodeExecution.getStatus();
                return nodeExecution;
            }

            //only one next node
            if (nextNodes.size() == 1) {
                executeUnit(nextNodes.get(0), false);
            } else {
                for (String nextNode : nextNodes) {
                    if (flowExecution.isDone(nextNode)) {
                        //log.error("The flow is a directed acyclic graph, which has the loop node, please check the flow node config, the loop node key:{}",nextNode);
                        log.warn("next nodes has bean executed, node key:{}", nextNode);
                        continue;
                    }
                    executeUnit(nextNode, true);
                }
            }
            //unitStatus = nodeExecution.getStatus();
        } catch (Exception e) {
            log.error("execute node error:{}", nodeExecutor, e);
            unitStatus = ExecutionStatus.FAILED;
            throw InnospotException.buildException(this.getClass(), ResponseCode.EXECUTE_ERROR, e, "node executor error:" + nodeExecutor.nodeKey(), e.getMessage());
        }
        return nodeExecution;
    }

    private void executeUnit(String nodeKey, boolean async) {
        if (flowExecution.isExecuted(nodeKey)) {
            log.warn("node has executed, nodeKey:{}",nodeKey);
            return;
        }
        BaseAppNode baseAppNode = flow.findNode(nodeKey);
        ExecutionUnit nodeUnit = executionUnits.get(nodeKey);
        if (nodeUnit == null) {
            nodeUnit = build(baseAppNode);
            nodeUnit.run(async);
        } else if (nodeUnit.nodeExecution == null && !nodeUnit.unitStatus.isDone()) {
            nodeUnit.run(async);
        } else {
            log.warn("node has bean executed, node key:{}", nodeKey);
        }
    }

    private boolean executePreviousNodes() {
        //whether all of the source node is complete
        Set<String> previousNodeKeys = flow.sourceKey(this.nodeKey());
        if (log.isDebugEnabled()) {
            log.debug("current node: {} ,previous nodes: {}", this.nodeExecutor.simpleInfo(), previousNodeKeys);
        }
        if (CollectionUtils.isEmpty(previousNodeKeys)) {
            return true;
        }
        //not execute node list
        List<String> unDoneList = new ArrayList<>();

        for (String source : previousNodeKeys) {
            if (!flowExecution.isDone(source) && flowExecution.shouldExecute(source)) {
                unDoneList.add(source);
            }
        }//end for

        if (log.isDebugEnabled()) {
            log.debug("node: {}, undone nodes: {}, previous nodes: {}", this.nodeExecutor.simpleInfo(), unDoneList, previousNodeKeys);
        }
        boolean allDone = true;

        //have not execute source nodes in the current node
        if (!unDoneList.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("execute previous nodes:{}", unDoneList);
            }
            if (unDoneList.size() == 1) {
                executeUnit(unDoneList.get(0), false);
            } else {
                for (String unDoneNode : unDoneList) {
                    //recursively invoke the node that needs to be executed in the unDoneList
                    executeUnit(unDoneNode, true);
                }
            }
            for (String unDoneNode : unDoneList) {
                ExecutionUnit unit = this.executionUnits.get(unDoneNode);
                if (unit == null) {
                    continue;
                }
                boolean d = unit.isDone();
                allDone = d && allDone;
                if (!d) {
                    log.warn("previous,node not execute completed, undone node: {}, nodeKey:{}", unDoneNode, nodeKey());
                }
            }
            if (!allDone) {
                log.warn("not all of previous nodes execute completed, nodeKey:{}", nodeKey());
            }
            //all unDoneList source nodes are completed
        }//end unDoneList

        return allDone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("node=").append(this.nodeExecutor.simpleInfo());
        sb.append(", status=").append(unitStatus);
        sb.append(", tName='").append(threadName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
