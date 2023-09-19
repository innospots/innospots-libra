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

import io.innospots.workflow.core.engine.FlowEngineManager;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Raydian
 * @date 2020/12/20
 */
public class WebhookSubNode extends BaseAppNode {


    public static final String FLOW_INSTANCE_ID = "flow_instance_id";
    //    public static final String FLOW_INSTANCE_REVISION = "FLW_INS_REV";
    public static final String FLOW_ENGINE_TYPE = "FLW_EG_TYPE";

    private Long flowInstanceId;
//    private Integer revision;

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        flowInstanceId = nodeInstance.valueLong(FLOW_INSTANCE_ID);
        //TODO use latest publish version
//        revision = nodeInstance.valueInteger(FLOW_INSTANCE_REVISION);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        List<Map<String, Object>> payloads = new ArrayList<>();
        for (ExecutionInput input : nodeExecution.getInputs()) {
            payloads.addAll(input.getData());
        }
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(
                flowInstanceId, nodeExecution.getRevision(), payloads);
        FlowEngineManager.eventFlowEngine().execute(flowExecution);
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeOutput.addResult(flowExecution.getOutput());

    }


}
