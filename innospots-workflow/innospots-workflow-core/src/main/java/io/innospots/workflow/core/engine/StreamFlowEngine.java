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

package io.innospots.workflow.core.engine;

import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.IFlowExecutionListener;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.manage.FlowManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Flow engine, directed acyclic graph, flow graph has no loop
 *
 * @author Raydian
 * @date 2020/12/20
 */
@Slf4j
public class StreamFlowEngine extends BaseFlowEngine {


    public StreamFlowEngine(List<IFlowExecutionListener> flowExecutionListeners, FlowManager flowManager) {
        super(flowExecutionListeners, flowManager);
    }

    @Override
    protected void execute(Flow flow, FlowExecution flowExecution) {
        List<BaseNodeExecutor> nodeExecutors = null;
        if (CollectionUtils.isEmpty(flowExecution.getCurrentNodeKeys())) {
            nodeExecutors = flow.startNodes();
        } else {
            nodeExecutors = flow.findNodes(flowExecution.getCurrentNodeKeys());
        }

        traverseExecuteNode(nodeExecutors, flow, flowExecution);
    }

    /**
     * breadth-first traversal
     *
     * @param nodeExecutors
     * @param flow
     * @param flowExecution
     */
    private void traverseExecuteNode(List<BaseNodeExecutor> nodeExecutors, Flow flow, FlowExecution flowExecution) {
        List<String> nextNodes = new ArrayList<>();

        //依次执行节点，宽度优先执行
        for (BaseNodeExecutor nodeExecutor : nodeExecutors) {
            if (!flowExecution.isNotExecute(nodeExecutor.nodeKey())) {
                continue;
            }
            try {
                NodeExecution nodeExecution = nodeExecutor.execute(flowExecution);
                if (nodeExecution.nextExecute()) {
                    nextNodes.addAll(nodeExecution.getNextNodeKeys());
                }
                /*
                if(nodeExecution.getStatus() == ExecutionStatus.PENDING){
                    flowExecution.setStatus(nodeExecution.getStatus());
                    return;
                }else if(nodeExecution.getStatus() == ExecutionStatus.FAILED){
                    flowExecution.setStatus(ExecutionStatus.FAILED);
                    flowExecution.setMessage(nodeExecution.getMessage());
                }
                flowExecution.addNodeExecution(nodeExecution);
                 */

            } catch (Exception e) {
                log.error("execute node error:{}", nodeExecutor, e);
                throw InnospotException.buildException(this.getClass(), ResponseCode.EXECUTE_ERROR, e, "node executor error:" + nodeExecutor.nodeKey(), e.getMessage());
            }
            if (flowExecution.getEndNodeKey() != null && flowExecution.getEndNodeKey().equals(nodeExecutor.nodeKey())) {
                //set target nodeKey that will execute to this node
                return;
            }
        }//end for

        if (CollectionUtils.isEmpty(nextNodes)) {
            return;
        }


        //all next should executable nodes
        for (String nextNode : nextNodes) {
            if (flowExecution.isDone(nextNode)) {
                //log.error("The flow is a directed acyclic graph, which has the loop node, please check the flow node config, the loop node key:{}",nextNode);
                log.warn("next nodes has bean executed, node key:{}", nextNode);
                continue;
            }
            nextNodeExecute(nextNode, flow, flowExecution);
        }
        /*
        List<BaseAppNode> nextExecutors = new ArrayList<>();
        for (String nextNode : nextNodes) {
            if(flowExecution.isDone(nextNode)){
                log.error("The flow is a directed acyclic graph, which has the loop node, please check the flow node config, the loop node key:{}",nextNode);
                continue;
            }

            //判断来源节点是否都已经执行完成，如果全部执行完成则，可执行后续节点
            Set<String> sourceKeys = flow.sourceKey(nextNode);
            boolean isDone = sourceKeys.stream().allMatch(flowExecution::isDone);
            if(isDone){
                BaseAppNode baseAppNode = flow.findNode(nextNode);
                // end node
                if (baseAppNode!=null && !"END".equals(baseAppNode.getCode())){
                    nextExecutors.add(baseAppNode);
                }

            }
        }//end nextNodes for

        traverseExecuteNode(nextExecutors,flow,flowExecution);

         */
    }


    private void nextNodeExecute(String shouldExecuteNode, Flow flow, FlowExecution flowExecution) {
        //whether all of the source node is complete
        Set<String> sourceKeys = flow.sourceKey(shouldExecuteNode);
        //not execute node list
        List<String> unDoneList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(sourceKeys)) {
            for (String source : sourceKeys) {
                if (!flowExecution.isDone(source)) {
                    unDoneList.add(source);
                }
            }
        }


        //have not execute source nodes in the current node
        if (!unDoneList.isEmpty()) {
            for (String unDoneNode : unDoneList) {
                //recursively invoke the node that needs to be executed in the unDoneList
                if (flowExecution.isNotExecute(unDoneNode)) {
                    nextNodeExecute(unDoneNode, flow, flowExecution);
                }
            }

            for (String unDoneNode : unDoneList) {
                while (flowExecution.isExecuting(unDoneNode)) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }//wait until the node is done, in the asynchronous execution of nodes
            }
            //all unDoneList source nodes are completed
        }//end unDoneList

        if (flowExecution.isNotExecute(shouldExecuteNode)) {
            BaseNodeExecutor baseAppNode = flow.findNode(shouldExecuteNode);
            //add to executable node list
            List<BaseNodeExecutor> nodeExecutors = new ArrayList<>();
            nodeExecutors.add(baseAppNode);
            traverseExecuteNode(nodeExecutors, flow, flowExecution);
        }

    }

    @Override
    public FlowExecution run(Long workflowInstanceId, Integer revisionId, String targetNodeKey, List<Map<String, Object>> payloads) {
        return null;
    }
}
