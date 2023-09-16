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

import io.innospots.base.condition.Opt;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.SelectClause;
import io.innospots.base.data.operator.UpdateItem;
import io.innospots.base.model.DataBody;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.console.entity.execution.NodeExecutionEntity;
import io.innospots.workflow.console.mapper.execution.NodeExecutionMapper;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * node execution crud
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/15
 */
@Slf4j
public class JdbcNodeExecutionOperator implements INodeExecutionOperator {

    private IDataOperator dataOperator;

    private IExecutionContextOperator executionContextOperator;

    public JdbcNodeExecutionOperator(IDataOperator dataOperator, IExecutionContextOperator executionContextOperator) {
        this.dataOperator = dataOperator;
        this.executionContextOperator = executionContextOperator;
    }

    @Override
    public NodeExecution getNodeExecutionById(String nodeExecutionId, boolean includeContext, int page, int size) {
        SelectClause selectClause = buildClause(null, nodeExecutionId, null);
        DataBody<Map<String, Object>> response = dataOperator.selectForObject(selectClause);
        NodeExecution nodeExecution = null;
        if (response != null && response.getBody() instanceof Map) {
            nodeExecution = fillNodeExecution(response.getBody(), includeContext, page, size);
        }

        return nodeExecution;
    }

    @Override
    public boolean updateStatus(String nodeExecutionId, ExecutionStatus status, String message) {
        UpdateItem updateItem = new UpdateItem();
        Map<String, Object> data = new HashMap<>();
        data.put(BeanUtils.getFieldName(NodeExecutionEntity::getStatus, true), status.name());
        if (message != null) {
            data.put(BeanUtils.getFieldName(NodeExecutionEntity::getMessage, true), message);
        }
        updateItem.addCondition(nodeExecutionId, Opt.EQUAL, nodeExecutionId, FieldValueType.STRING);
        updateItem.setData(data);
        dataOperator.update(NodeExecutionEntity.TABLE_NAME, updateItem);
        return false;
    }

    @Override
    public List<NodeExecution> getNodeExecutionsByFlowExecutionId(String flowExecutionId, boolean includeContext) {
        return getNodeExecutionsByFlowExecutionId(flowExecutionId, Collections.emptyList(), includeContext);
    }

    @Override
    public List<NodeExecution> getNodeExecutionsByFlowExecutionId(String flowExecutionId, List<String> nodeKeys, boolean includeContext) {
        //WorkflowInstance workflowInstance = workflowInstanceOperator.getFlowInstance(workflowInstanceId,revision,false,false);
//        String datasourceCode = FlowExecution.executionDatasourceCode(flowExecutionId);
        SelectClause selectClause = buildClause(flowExecutionId, null, nodeKeys);

        return parse(dataOperator.selectForList(selectClause), includeContext);
    }

    private List<NodeExecution> parse(PageBody<Map<String, Object>> innospotResponse, boolean includeContext) {
        List<NodeExecution> nodeExecutions = new ArrayList<>();
        if (innospotResponse != null && CollectionUtils.isNotEmpty(innospotResponse.getList())) {
            for (Map<String, Object> body : innospotResponse.getList()) {
                NodeExecution nodeExecution = fillNodeExecution(body, includeContext);
                nodeExecutions.add(nodeExecution);
            }//end for
        }//end response
        return nodeExecutions;
    }

    private NodeExecution fillNodeExecution(Map<String, Object> data, boolean includeContext) {
        return fillNodeExecution(data, includeContext, 0, Integer.MAX_VALUE);
    }

    private NodeExecution fillNodeExecution(Map<String, Object> data, boolean includeContext, int page, int size) {
        NodeExecution nodeExecution = NodeExecutionMapper.INSTANCE.mapToModel(data, true);
        if (includeContext) {
            executionContextOperator.fillNodeExecution(nodeExecution, true, page, size);
        }//end context

        return nodeExecution;
    }

    private SelectClause buildClause(String flowExecutionId,
                                     String nodeExecutionId,
                                     List<String> nodeKeys) {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(NodeExecutionEntity.TABLE_NAME);
        if (nodeExecutionId != null) {
            selectClause.addWhere(BeanUtils.getFieldName(NodeExecutionEntity::getNodeExecutionId, true), nodeExecutionId, Opt.EQUAL);
        }
        if (flowExecutionId != null) {
            selectClause.addWhere(BeanUtils.getFieldName(NodeExecutionEntity::getFlowExecutionId, true), flowExecutionId, Opt.EQUAL);
        }
        if (CollectionUtils.isNotEmpty(nodeKeys)) {
            selectClause.addWhereInclude(BeanUtils.getFieldName(NodeExecutionEntity::getNodeKey, true), nodeKeys, FieldValueType.STRING);
        }
        return selectClause;
    }

    @Override
    public boolean insert(NodeExecution execution) {
        dataOperator.insert(NodeExecutionEntity.TABLE_NAME, NodeExecutionMapper.INSTANCE.modelToMap(execution, true));
        if(log.isDebugEnabled()){
            log.debug("node execution saved: {}",execution.getNodeExecutionId());
        }
        executionContextOperator.saveExecutionContext(execution);
        execution.clearInput();
        if (!execution.isMemoryMode()) {
            execution.clearOutput();
        }
        return true;
    }

    @Override
    public boolean insert(List<NodeExecution> executions) {
        for (NodeExecution execution : executions) {
            insert(execution);
        }
        return true;
    }

    @Override
    public boolean update(NodeExecution execution) {
        UpdateItem updateItem = new UpdateItem();
        Map<String, Object> data = NodeExecutionMapper.INSTANCE.modelToMap(execution, true);
        String nodeExecutionId = BeanUtils.getFieldName(NodeExecutionEntity::getNodeExecutionId, true);
        updateItem.addCondition(nodeExecutionId, Opt.EQUAL, data.get(nodeExecutionId), FieldValueType.STRING);
        data.remove(nodeExecutionId);
        data.remove(BeanUtils.getFieldName(NodeExecutionEntity::getCreatedTime, true));
        updateItem.setData(data);
        dataOperator.update(NodeExecutionEntity.TABLE_NAME, updateItem);
        return true;
    }

    @Override
    public boolean update(List<NodeExecution> executions) {
        for (NodeExecution execution : executions) {
            update(execution);
        }
        return true;
    }


    @Override
    public void close() {

    }
}
