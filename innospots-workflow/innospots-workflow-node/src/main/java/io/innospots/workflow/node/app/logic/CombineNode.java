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

package io.innospots.workflow.node.app.logic;

import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/10/7
 */
public class CombineNode extends BaseNodeExecutor {

    public static final String FIELD_OUTPUT = "output_setting";

    private OutputSetting outputSetting;

    @Override
    protected void initialize() {
        outputSetting = OutputSetting.valueOf(valueString(FIELD_OUTPUT));
    }

    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        ExecutionOutput nodeOutput = new ExecutionOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        if (outputSetting == OutputSetting.NONE) {
            return;
        }
        List<NodeExecution> nodeExecutions = new ArrayList<>();
        for (String prevNodeKey : this.prevNodeKeys()) {
            nodeExecutions.add(flowExecution.getNodeExecution(prevNodeKey));
        }
        nodeExecutions.sort(Comparator.comparing(NodeExecution::getEndTime));
        NodeExecution execution = null;
        if (outputSetting == OutputSetting.FIRST) {
            execution = nodeExecutions.get(0);
        } else {
            execution = nodeExecutions.get(nodeExecutions.size() - 1);
        }
        for (ExecutionOutput output : execution.getOutputs()) {
            nodeOutput.addResult(output.getResults());
        }
    }

    public enum OutputSetting {
        /**
         *
         */
        NONE,
        FIRST,
        LAST;
    }
}
