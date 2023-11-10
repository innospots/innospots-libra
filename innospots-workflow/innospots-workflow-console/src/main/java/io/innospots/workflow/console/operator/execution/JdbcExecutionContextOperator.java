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

package io.innospots.workflow.console.operator.execution;

import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.console.entity.execution.ExecutionContextEntity;
import io.innospots.workflow.console.converter.execution.ExecutionContextConverter;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/1/19
 */
public class JdbcExecutionContextOperator implements IExecutionContextOperator {

    private IDataOperator dataOperator;

    private String storePath;

    public JdbcExecutionContextOperator(IDataOperator dataOperator, String storePath) {
        this.dataOperator = dataOperator;
        this.storePath = storePath;
    }

    @Override
    public List<NodeOutput> readNodeOutputs(String flowExecutionId, String nodeExecutionId) {
        return readNodeOutputs(flowExecutionId, nodeExecutionId, null);
    }

    @Override
    public List<NodeOutput> readNodeOutputs(String flowExecutionId, String nodeExecutionId, String targetNodeKey) {
        String contextId = BeanUtils.getFieldName(ExecutionContextEntity::getExecutionId, true);
        NodeExecution nodeExecution = new NodeExecution();
        nodeExecution.setNodeExecutionId(nodeExecutionId);
        nodeExecution.setFlowExecutionId(flowExecutionId);
        DataBody<Map<String, Object>> contextResp = dataOperator.selectForObject(ExecutionContextEntity.TABLE_NAME, contextId, nodeExecutionId);
        if (contextResp.getBody() != null) {
            nodeExecution.fillExecutionContext(contextResp.getBody());
        }
        this.fillNodeExecutionOutput(nodeExecution, null);
        return nodeExecution.getOutputs();
    }

    @Override
    public Map<String, Object> readFlowExecutionContext(String flowExecutionId) {
        String contextId = BeanUtils.getFieldName(ExecutionContextEntity::getExecutionId, true);
        DataBody<Map<String, Object>> contextResp = dataOperator.selectForObject(ExecutionContextEntity.TABLE_NAME, contextId, flowExecutionId);
        if (contextResp.getBody() instanceof Map) {
            return contextResp.getBody();
        }
        return Collections.emptyMap();
    }

    @Override
    public PageBody<NodeOutput> pageNodeOutputs(String executionId, int page, int size) {
        String contextId = BeanUtils.getFieldName(ExecutionContextEntity::getExecutionId, true);
        NodeExecution nodeExecution = new NodeExecution();
        nodeExecution.setNodeExecutionId(executionId);
        DataBody<Map<String, Object>> contextResp = dataOperator.selectForObject(ExecutionContextEntity.TABLE_NAME, contextId, executionId);
        if (contextResp != null) {
            nodeExecution.fillExecutionContext(contextResp.getBody());
        }
        IExecutionContextOperator.fillExecutionDataPath(nodeExecution, storePath);
        this.fillNodeExecutionOutput(nodeExecution, page, size, null);
        List<NodeOutput> nodeOutputs = nodeExecution.getOutputs();

        PageBody<NodeOutput> pageBody = new PageBody<>();
        pageBody.setCurrent((long) page);
        pageBody.setPageSize((long) size);
        pageBody.setList(nodeOutputs);
        return pageBody;
    }

    @Override
    public void saveExecutionContext(NodeExecution nodeExecution) {
        ExecutionContextEntity executionContextEntity = ExecutionContextConverter.toExecutionContextEntity(nodeExecution);
        dataOperator.insert(ExecutionContextEntity.TABLE_NAME, BeanUtils.toMap(executionContextEntity, true, true));
        if(logger.isDebugEnabled()){
            logger.debug("save node execution context,{}",nodeExecution.getNodeExecutionId());
        }
        //this.saveInputItems(nodeExecution);
        this.saveOutputItems(nodeExecution);
    }

    @Override
    public void saveExecutionContext(FlowExecution flowExecution) {
        dataOperator.insert(ExecutionContextEntity.TABLE_NAME, ExecutionContextConverter.flowExecutionContextToMap(flowExecution, true));
    }

    @Override
    public void cleanExecutionContext(Long flowInstanceId) {
    }

    @Override
    public void fillNodeExecution(NodeExecution nodeExecution, boolean fillOutput, int page, int size) {
        String contextId = BeanUtils.getFieldName(ExecutionContextEntity::getExecutionId, true);
        DataBody<Map<String, Object>> contextResp = dataOperator.selectForObject(ExecutionContextEntity.TABLE_NAME, contextId, nodeExecution.getNodeExecutionId());
        if (contextResp != null) {
            nodeExecution.fillExecutionContext(contextResp.getBody());
        }
        if (fillOutput) {
            IExecutionContextOperator.fillExecutionDataPath(nodeExecution, storePath);
            this.fillNodeExecutionOutput(nodeExecution, page, size, null);
        }
    }

    @Override
    public void fillFlowExecution(FlowExecution flowExecution, boolean fillOutput) {
        String contextId = BeanUtils.getFieldName(ExecutionContextEntity::getExecutionId, true);
        DataBody<Map<String, Object>> contextResp = dataOperator.selectForObject(ExecutionContextEntity.TABLE_NAME, contextId, flowExecution.getFlowExecutionId());
        if (contextResp.getBody() instanceof Map) {
            flowExecution.fillExecutionContext((Map<String, Object>) contextResp.getBody());
        }
    }
}
