/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.workflow.core.node.executor;

import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.instance.model.NodeInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * triggerNode是否相同判断eventBody内容是否一致，一致则认为TriggerNode相同
 *
 * @author Smars
 * @date 2021/5/8
 */
public class TriggerNode extends BaseAppNode {

    protected Map<String, Object> eventBody = new HashMap<>();

    public static final String FIELD_NEXT_NODES = "next_nodes";

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        eventBody.put(FIELD_NEXT_NODES, ni.getNextNodeKeys());
        eventBody.put("node_key", this.nodeKey());
        eventBody.put("node_instance_id", ni.getNodeInstanceId());
        eventBody.put("node_code", ni.getCode());
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        //output the data that input
        List<ExecutionInput> inputs = nodeExecution.getInputs();
        NodeOutput nodeOutput = new NodeOutput();
        for (ExecutionInput input : inputs) {
            for (Map<String, Object> item : input.getData()) {
                nodeOutput.addResult(item);
            }
        }
        nodeOutput.addNextKey(ni.getNextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
    }

    public Map<String, Object> getEventBody() {
        return eventBody;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TriggerNode)) {
            return false;
        }
        TriggerNode that = (TriggerNode) o;
        return Objects.equals(eventBody, that.eventBody) &&
                Objects.equals(ni.getNextNodeKeys(), that.ni.getNextNodeKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventBody, ni.getNextNodeKeys());
    }
}
