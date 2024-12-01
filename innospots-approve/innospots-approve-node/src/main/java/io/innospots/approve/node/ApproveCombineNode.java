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
import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.enums.ApproveResult;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveActor;
import io.innospots.approve.core.model.ApproveExecution;
import io.innospots.approve.core.operator.ApproveActorOperator;
import io.innospots.approve.core.operator.ApproveExecutionOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mapdb.CC;

import java.util.ArrayList;
import java.util.List;
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

        List<ApproveExecution> executions = approveExecutionOperator.listApproveExecutions(flowExecution.getFlowExecutionId(), this.prevNodeKeys());

        if(CollectionUtils.isEmpty(executions) || executions.size() < this.prevNodeKeys().size()){
            String message = "not all of the approve nodes have been executed completely.";
            nodeExecution.setMessage(message);
            nodeExecution.setNext(false);
            return;
        }

        boolean approved = false;
        if (approveMode == ApproveMode.ALL) {
            approved = executions.stream()
                    .allMatch(ae -> Objects.equals(ae.getApproveStatus(), ApproveStatus.APPROVED));
        } else {
            approved = executions.stream()
                    .anyMatch(ae -> Objects.equals(ae.getApproveStatus(), ApproveStatus.APPROVED));
        }

        nodeExecution.setNext(approved);

        for (ApproveExecution approveExecution : executions) {
            nodeOutput.addResult(approveExecution.toInfo());
        }
        ApproveActor actor = ApproveActor.builder()
                .actorType(ApproveConstant.ACTOR_TYPE_SYS)
                .approveAction(ApproveAction.DONE)
                .approveInstanceKey(ApproveHolder.get().getApproveInstanceKey())
                .nodeKey(this.nodeKey())
                .userId(CCH.userId())
                .userName(CCH.authUser())
                .result(approved ? ApproveResult.APPROVED.name():ApproveResult.REJECTED.name())
                .build();
        actor = approveActorOperator.saveApproveActor(actor);
        ApproveHolder.setActor(actor);
    }

    public enum ApproveMode {
        ALL,
        EITHER;
    }
}
