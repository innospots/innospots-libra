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

package io.innospots.workflow.runtime.scheduled;

import io.innospots.base.events.IEventListener;
import io.innospots.workflow.core.engine.FlowEngineManager;
import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.enums.FlowStatus;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.model.scheduled.ScheduledNodeExecution;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/9
 */
@Slf4j
public class NodeExecutionEventListener implements IEventListener<NodeExecutionEventBody> {

    private IFlowExecutionOperator flowExecutionOperator;
    private INodeExecutionOperator nodeExecutionOperator;
    private IScheduledNodeExecutionOperator scheduledNodeExecutionOperator;

    public NodeExecutionEventListener(IFlowExecutionOperator flowExecutionOperator,
                                      INodeExecutionOperator nodeExecutionOperator,
                                      IScheduledNodeExecutionOperator scheduledNodeExecutionOperator) {
        this.flowExecutionOperator = flowExecutionOperator;
        this.nodeExecutionOperator = nodeExecutionOperator;
        this.scheduledNodeExecutionOperator = scheduledNodeExecutionOperator;
    }

    @Override
    public Object listen(NodeExecutionEventBody event) {
        ScheduledNodeExecution scheduledNodeExecution = (ScheduledNodeExecution) event.getBody();
        if (log.isDebugEnabled()) {
            log.debug("scheduled node execution:{}, scheduleTime:{}", scheduledNodeExecution.getNodeExecutionId(), scheduledNodeExecution.getScheduledTime());
            log.debug(event.getEventType());
        }
        try {
            FlowExecution flowExecution = flowExecutionOperator.getFlowExecutionById(scheduledNodeExecution.getFlowExecutionId(), true);
            List<NodeExecution> nodeExecutions = nodeExecutionOperator.getNodeExecutionsByFlowExecutionId(flowExecution.getFlowExecutionId(), null, true);
            flowExecution.resetSequenceNumber(nodeExecutions.size());
            flowExecution.addNodeExecutions(nodeExecutions);
            flowExecution.setStatus(ExecutionStatus.CONTINUE_RUNNING);
            flowExecution.resetCurrentNodeKey(scheduledNodeExecution.getNodeKey());
            IFlowEngine flowEngine = FlowEngineManager.eventFlowEngine();
            BuildProcessInfo processInfo = flowEngine.prepare(flowExecution.getFlowInstanceId(), flowExecution.getRevision(), false);

            if (processInfo != null && processInfo.getStatus() == FlowStatus.LOADED) {
                NodeExecution nodeExecution = flowExecution.getNodeExecution(scheduledNodeExecution.getNodeKey());
                if (nodeExecution != null) {
                    nodeExecution.end("scheduled execution", ExecutionStatus.COMPLETE);
                    nodeExecution.setNext(true);
                    nodeExecutionOperator.update(nodeExecution);
                }
                boolean b = flowEngine.continueExecute(flowExecution);
                if (b) {
                    scheduledNodeExecution.setStatus(ExecutionStatus.COMPLETE);
                    scheduledNodeExecutionOperator.update(scheduledNodeExecution);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            scheduledNodeExecution.setStatus(ExecutionStatus.FAILED);
            scheduledNodeExecution.setMessage(e.getMessage() + "_" + e.getClass());
            scheduledNodeExecutionOperator.update(scheduledNodeExecution);
        }


        return event;
    }

    @Override
    public Class<NodeExecutionEventBody> eventBodyClass() {
        return NodeExecutionEventBody.class;
    }
}
