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

package io.innospots.workflow.core.execution.reader;

import io.innospots.base.data.body.PageBody;
import io.innospots.workflow.core.enums.FlowVersion;
import io.innospots.workflow.core.execution.model.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * the node execution display in the flow canvas
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/26
 */
public class NodeExecutionReader {

    private final INodeExecutionOperator nodeExecutionOperator;

    private final IFlowExecutionOperator flowExecutionOperator;

    private final IWorkflowLoader workflowLoader;

    public NodeExecutionReader(
            IWorkflowLoader workflowLoader,
            INodeExecutionOperator nodeExecutionOperator,
            IFlowExecutionOperator flowExecutionOperator) {
        this.nodeExecutionOperator = nodeExecutionOperator;
        this.workflowLoader = workflowLoader;
        this.flowExecutionOperator = flowExecutionOperator;
    }

    public Map<String, NodeExecutionDisplay> readExecutionByFlowExecutionId(String flowExecutionId) {
        return readExecutionByFlowExecutionId(flowExecutionId, Collections.emptyList(), false);
    }


    public Map<String, NodeExecutionDisplay> readExecutionByFlowExecutionId(String flowExecutionId, List<String> nodeKeys) {
        return readExecutionByFlowExecutionId(flowExecutionId, nodeKeys, false);
//        return readExecutionByFlowExecutionId(flowExecutionId, nodeKeys, true);
    }

    public Map<String, NodeExecutionDisplay> readExecutionByFlowExecutionId(String flowExecutionId, List<String> nodeKeys, boolean includeContext) {


        List<NodeExecution> nodeExecutions = nodeExecutionOperator.
                getNodeExecutionsByFlowExecutionId(flowExecutionId, nodeKeys, includeContext);

        if (CollectionUtils.isEmpty(nodeExecutions)) {
            return Collections.emptyMap();
        }

        Map<String, NodeExecutionDisplay> nodeDisplays = new HashMap<>(nodeExecutions.size());
        WorkflowBody workflowBody = null;
        if (!nodeExecutions.isEmpty()) {
            NodeExecution ne = nodeExecutions.get(0);
            workflowBody = workflowLoader.loadWorkflow(ne.getFlowInstanceId(), ne.getRevision());
        }

        for (NodeExecution nodeExecution : nodeExecutions) {
            NodeInstance nodeInstance = null;
            if (workflowBody != null) {
                nodeInstance = workflowBody.findNode(nodeExecution.getNodeKey());
            }
            if(nodeInstance!=null){
                nodeExecution.setNodeName(nodeInstance.getName());
            }
            nodeDisplays.put(nodeExecution.getNodeKey(), NodeExecutionDisplay.build(nodeExecution, nodeInstance));
        }

        return nodeDisplays;
    }

    public Map<String, NodeExecutionDisplay> readLatestNodeExecutionByFlowInstanceId(Long workflowInstanceId, Integer revision, List<String> nodeKeys) {

        PageBody<FlowExecutionBase> flowExecutions = flowExecutionOperator.pageLatestFlowExecutions(workflowInstanceId, revision, 0, 1);

        if (CollectionUtils.isEmpty(flowExecutions.getList())) {
            return Collections.emptyMap();
        }
        return readExecutionByFlowExecutionId(flowExecutions.getList().get(0).getFlowExecutionId(), nodeKeys);
    }

    public NodeExecutionDisplay findNodeExecution(String nodeExecutionId, int page, int size) {
        NodeExecution nodeExecution = nodeExecutionOperator.getNodeExecutionById(nodeExecutionId, true, page, size);
        NodeExecutionDisplay nodeExecutionDisplay = null;
        if (nodeExecution != null) {
            if (nodeExecution.getRevision() == null || nodeExecution.getRevision() == 0) {
                NodeInstance nodeInstance = workflowLoader.loadNodeInstance(nodeExecution.getFlowInstanceId(), FlowVersion.DRAFT.getVersion(), nodeExecution.getNodeKey());
                nodeExecutionDisplay = NodeExecutionDisplay.build(nodeExecution, nodeInstance, page, size);
                /*
                WorkflowBaseBody workflowBaseBody = workflowLoader.loadWorkflow(nodeExecution.getFlowInstanceId(), FlowVersion.DRAFT.getVersion());
                if (workflowBaseBody != null) {
                    NodeInstance nodeInstance = workflowBaseBody.getNodes().stream().filter(ni -> Objects.equals(ni.getNodeKey(), nodeExecution.getNodeKey())).findFirst().orElse(null);
                    nodeExecutionDisplay = NodeExecutionDisplay.build(nodeExecution, nodeInstance, page, size);
                }
                 */
            } else {
                NodeInstance nodeInstance = workflowLoader.loadNodeInstance(nodeExecution.getFlowInstanceId(), FlowVersion.DRAFT.getVersion(), nodeExecution.getNodeKey());
                nodeExecutionDisplay = NodeExecutionDisplay.build(nodeExecution, nodeInstance, page, size);
                /*
                WorkflowBody workflowBody =
                        workflowLoader.loadWorkflow(nodeExecution.getFlowInstanceId(), nodeExecution.getRevision());
                NodeInstance nodeInstance = workflowBody.findNode(nodeExecution.getNodeKey());
                nodeExecutionDisplay = NodeExecutionDisplay.build(nodeExecution, nodeInstance, page, size);

                 */
            }
        }
        return nodeExecutionDisplay;
    }


}
