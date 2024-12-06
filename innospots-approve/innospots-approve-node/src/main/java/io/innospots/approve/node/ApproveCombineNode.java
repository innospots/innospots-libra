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

package io.innospots.approve.node;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.enums.ActorType;
import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.enums.ApproveResult;
import io.innospots.approve.core.model.ApproveActor;
import io.innospots.approve.core.model.ApproveExecution;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/10/7
 */
@Slf4j
public class ApproveCombineNode extends ApproveBaseNode {

    public static final String FIELD_OUTPUT = "approve_mode";

    private ApproveMode approveMode;

    @Override
    protected void initialize() {
        super.initialize();
        approveMode = ApproveMode.valueOf(valueString(FIELD_OUTPUT));
    }

    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        ExecutionOutput nodeOutput = this.buildOutput(nodeExecution);

        List<ApproveExecution> executions = listPrevApproveExecution(flowExecution.getFlowExecutionId());

        if (CollectionUtils.isEmpty(executions) || executions.size() < this.prevNodeKeys().size()) {
            String message = "not all of the approve nodes have been executed completely.";
            nodeExecution.setMessage(message);
            nodeExecution.setNext(false);
            return;
        }

        boolean approved = false;
        if (approveMode == ApproveMode.ALL) {
            approved = executions.stream()
                    .allMatch(ae -> Objects.equals(ae.getApproveResult(), ApproveResult.APPROVED));
        } else {
            approved = executions.stream()
                    .anyMatch(ae -> Objects.equals(ae.getApproveResult(), ApproveResult.APPROVED));
        }

        nodeExecution.setNext(approved);

        ApproveResult approveResult = approved ? ApproveResult.APPROVED : ApproveResult.REJECTED;
        fillApproveActor(nodeExecution, ApproveAction.DONE, ActorType.FLOW, approveResult,true);

        Map<String,Object> result = new HashMap<>();
        result.put(ApproveConstant.APPROVE_RESULT, approveResult.name());
        result.put(ApproveConstant.APPROVE_INSTANCE_KEY, ApproveHolder.get().getApproveInstanceKey());
        nodeOutput.addResult(result);
    }

    public enum ApproveMode {
        ALL,
        EITHER;
    }
}
