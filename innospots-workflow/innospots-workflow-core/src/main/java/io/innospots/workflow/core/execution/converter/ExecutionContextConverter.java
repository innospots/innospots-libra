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

package io.innospots.workflow.core.execution.converter;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.entity.ExecutionContextEntity;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/1/27
 */
public interface ExecutionContextConverter {


    static Map<String, Object> flowExecutionContextToMap(FlowExecution flowExecution, boolean underscore) {
        return BeanUtils.toMap(toExecutionContextEntity(flowExecution), underscore, true);
    }

    static ExecutionContextEntity toExecutionContextEntity(FlowExecution flowExecution) {
        ExecutionContextEntity context = new ExecutionContextEntity();
        context.setFlowInstanceId(flowExecution.getFlowInstanceId());
        context.setExecutionId(flowExecution.getFlowExecutionId());
        context.setInputs(JSONUtils.toJsonString(flowExecution.getInput().copy()));
        context.setInputSize(flowExecution.getInput().getData().size());
        context.setOutputs(JSONUtils.toJsonString(flowExecution.getOutput()));
        context.setOutputSize(flowExecution.getOutput().size());
        context.setNodePaths(JSONUtils.toJsonString(flowExecution.getNodeExecutions().keySet()));
        context.setContextType(ExecutionContextEntity.ContextType.FLOW.name());
        context.setCreatedTime(LocalDateTime.now());
        return context;
    }


    static ExecutionContextEntity toExecutionContextEntity(NodeExecution nodeExecution) {
        ExecutionContextEntity contextEntity = new ExecutionContextEntity();
        contextEntity.setFlowInstanceId(nodeExecution.getFlowInstanceId());
        contextEntity.setContextType(ExecutionContextEntity.ContextType.NODE.name());
        contextEntity.setExecutionId(nodeExecution.getNodeExecutionId());
        contextEntity.setNodePaths(nodeExecution.getNodeKey());
        contextEntity.setCreatedTime(LocalDateTime.now());

        int count = 0;
        List<ExecutionInput> executionInputs = new ArrayList<>();
        for (ExecutionInput input : nodeExecution.getInputs()) {
            count += input.getData().size();
            executionInputs.add(input.copy());
        }
        String inputJson = JSONUtils.toJsonString(executionInputs).replaceAll("\\n", " ");
        contextEntity.setInputs(inputJson);
        contextEntity.setInputSize(count);


        List<NodeOutput> nodeOutputs = new ArrayList<>();
        count = 0;
        for (NodeOutput output : nodeExecution.getOutputs()) {
            count += output.getResults().size();
            nodeOutputs.add(output.copy());
        }
        String outJson = JSONUtils.toJsonString(nodeOutputs).replaceAll("\\n", " ");
        contextEntity.setOutputs(outJson);
        contextEntity.setOutputSize(count);

        contextEntity.setCreatedTime(LocalDateTime.now());
        return contextEntity;
    }
}
