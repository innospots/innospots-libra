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
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/8/31
 */
public class FlowExecutionReader {

    private IFlowExecutionOperator IFlowExecutionOperator;
    private INodeExecutionOperator INodeExecutionOperator;

    public FlowExecutionReader(IFlowExecutionOperator IFlowExecutionOperator,
                               INodeExecutionOperator INodeExecutionOperator) {
        this.IFlowExecutionOperator = IFlowExecutionOperator;
        this.INodeExecutionOperator = INodeExecutionOperator;
    }

    public PageBody<FlowExecutionBase> pageFlowExecutions(Long workflowInstanceId, Integer revision, List<String> status,
                                                          String startTime, String endTime, Integer page, Integer size) {
        PageBody<FlowExecutionBase> body = IFlowExecutionOperator.pageFlowExecutions(workflowInstanceId, revision, status, startTime, endTime, page, size);
        List<FlowExecutionBase> bases = body.getList();
        if (CollectionUtils.isNotEmpty(bases)) {
            for (FlowExecutionBase base : bases) {
                FlowExecution execution = (FlowExecution) base;
                FlowExecution flowExecution = IFlowExecutionOperator.getFlowExecutionById(execution.getFlowExecutionId(), true);
                if (flowExecution != null) {
                    execution.setInput(flowExecution.getInput());
                    execution.setOutput(flowExecution.getOutput());
                }
            }
        }
        return body;
    }

    public FlowExecutionBase findLatestFlowExecution(Long workflowInstanceId, Integer revision) {
        PageBody<FlowExecutionBase> flowExecutionPageBody = IFlowExecutionOperator.pageLatestFlowExecutions(workflowInstanceId, revision, 0, 1);
        if (CollectionUtils.isEmpty(flowExecutionPageBody.getList())) {
            return null;
        }
        FlowExecutionBase flowExecutionBase = new FlowExecutionBase();
        BeanUtils.copyProperties(flowExecutionPageBody.getList().get(0), flowExecutionBase);

        List<NodeExecution> nodeExecutionList = INodeExecutionOperator.getNodeExecutionsByFlowExecutionId(flowExecutionBase.getFlowExecutionId(), false);
        if (nodeExecutionList != null) {
            flowExecutionBase.addNodeExecutions(nodeExecutionList);
        }

        return flowExecutionBase;
    }

    public FlowExecutionBase getFlowExecutionById(String flowExecutionId) {
        FlowExecutionBase flowExecutionBase = IFlowExecutionOperator.getFlowExecutionById(flowExecutionId, false);

        List<NodeExecution> nodeExecutionList = INodeExecutionOperator.getNodeExecutionsByFlowExecutionId(flowExecutionBase.getFlowExecutionId(), false);
        if (nodeExecutionList != null) {
            flowExecutionBase.addNodeExecutions(nodeExecutionList);
        }
        return flowExecutionBase;
    }
}
