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

package io.innospots.workflow.runtime.execution;

import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

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
 * @version 2.0.0
 * @date 2024/10/3
 */
@Slf4j
public class EcUnit implements Comparable<EcUnit>{

    final BaseNodeExecutor nodeExecutor;

    CompletableFuture<NodeExecution> future;

    NodeExecution nodeExecution;

    ExecutionStatus unitStatus;

    private String threadName;

    private final ExecutionCarrier carrier;

    private EcUnit(BaseNodeExecutor nodeExecutor, ExecutionCarrier executionCarrier) {
        this.nodeExecutor = nodeExecutor;
        unitStatus = ExecutionStatus.READY;
        this.carrier = executionCarrier;
        carrier.addUnit(this);
    }

    static EcUnit build(BaseNodeExecutor nodeExecutor, ExecutionCarrier executionCarrier) {
        return new EcUnit(nodeExecutor, executionCarrier);
    }

//    public void run(boolean async) {
//        carrier.runFlowNode(this,async);
//    }

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
            this.nodeExecution = carrier.getNodeExecution(this.nodeKey());
        }
        boolean done = false;
        if (nodeExecution != null) {
            done = nodeExecution.getStatus().isDone();
        }
        if (log.isDebugEnabled()) {
            if (!done) {
                log.debug("future:{}, out key: {}, status:{}, {}",future, this.nodeKey(), unitStatus, nodeExecution);
            }
        }
        return done;
    }

    public String nodeKey() {
        return nodeExecutor.nodeKey();
    }

    NodeExecution execute() {
        this.threadName = Thread.currentThread().getName();
//        unitStatus = ExecutionStatus.STARTING;
        try {
            //Check whether the previous nodes have been executed completely
//            boolean b = executePreviousNodes();
//            if (!b) {
//                unitStatus = ExecutionStatus.NOT_PREPARED;
//                log.warn("execution unit quit, the pre-execution dependency condition is not met, node:{}", nodeKey());
                //this.executionUnits.remove(nodeKey());
//                return null;
//            }
            unitStatus = ExecutionStatus.RUNNING;
            if (carrier.hasExecuted(this.nodeKey())) {
                nodeExecution = carrier.getNodeExecution(nodeKey());
                return nodeExecution;
            }
            nodeExecution = nodeExecutor.execute(carrier.flowExecution);
            unitStatus = nodeExecution.getStatus();

            if (log.isDebugEnabled()) {
                log.debug("execute done unit node: {}, sequence:{}", this.nodeExecutor.simpleInfo(), nodeExecution.getSequenceNumber());
            }

            if (carrier.isStoppedNode(this.nodeKey())) {
                return nodeExecution;
            }
//            executeNextNode();

        } catch (Exception e) {
            log.error("execute node error:{}", nodeExecutor, e);
            unitStatus = ExecutionStatus.FAILED;
            throw InnospotException.buildException(this.getClass(), ResponseCode.EXECUTE_ERROR, e, "node executor error:" + nodeExecutor.nodeKey(), e.getMessage());
        }
        return nodeExecution;
    }

    String consume(){
        return nodeExecution.getConsume();
    }

    int sequence(){
        return nodeExecution.getSequenceNumber();
    }


    /**
     * next node execute
     */
    /*
    private void executeNextNode() {

        List<String> nextNodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(nodeExecution.getNextNodeKeys())) {
            nextNodes.addAll(nodeExecution.getNextNodeKeys());
            nextNodes = nextNodes.stream().filter(carrier::shouldExecute).collect(Collectors.toList());
        }

        if (nodeExecution.getStatus() == ExecutionStatus.PENDING) {
            if (log.isDebugEnabled()) {
                log.debug("node execution status is pending, nodeKey:{}", nodeExecution.getNodeKey());
            }
            return;
        }

        if (nextNodes.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("the node is the leaf node, node key:{}", this.nodeKey());
            }
            return;
        }

        //only one next node
        if (nextNodes.size() == 1) {
            carrier.runNodeExecutor(nextNodes.get(0), false);
        } else {
            //parallel execute
            for (String nextNode : nextNodes) {
                if (carrier.hasDone(nextNode)) {
                    //log.error("The flow is a directed acyclic graph, which has the loop node, please check the flow node config, the loop node key:{}",nextNode);
                    log.warn("next nodes has bean executed, node key:{}", nextNode);
                    continue;
                }
                carrier.runNodeExecutor(nextNode, true);
            }
        }

    }


     */

    /**
     * execute previous node, if has not execute dependency node
     *
     * @return should execute
     */
    /*
    private boolean executePreviousNodes() {
        //not execute node list
        List<String> unDoneList = carrier.previousNotExecuteNodes(this.nodeExecutor);

        if (unDoneList.isEmpty()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("node: {}, undone nodes: {}, previous nodes: {}", this.nodeExecutor.simpleInfo(), unDoneList, carrier.sourceKey(this.nodeKey()));
        }
        boolean allDone = true;

        //have not execute source nodes in the current node

        if (unDoneList.size() == 1) {
            carrier.runNodeExecutor(unDoneList.get(0), false);
        } else {
            for (String unDoneNode : unDoneList) {
                //recursively invoke the node that needs to be executed in the unDoneList
                carrier.runNodeExecutor(unDoneNode, true);
            }
        }

        for (String unDoneNode : unDoneList) {
            EcUnit unit = carrier.getEcUnit(unDoneNode);
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

        return allDone;
    }

     */

    @Override
    public String toString() {
        return this.sequence() + ": " +
                this.threadName() + ", " +
                this.nodeKey() + ", " +
                this.nodeExecutor.nodeCode() + ", " +
                this.unitStatus() + ", " +
                this.consume() + ", " +
                this.isTaskDone();
    }



    @Override
    public int compareTo(@NotNull EcUnit o) {
        return Integer.compare(this.sequence(), o.sequence());
    }
}
