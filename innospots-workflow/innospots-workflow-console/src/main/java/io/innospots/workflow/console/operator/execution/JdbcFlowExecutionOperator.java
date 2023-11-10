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
import io.innospots.base.data.operator.jdbc.SelectClause;
import io.innospots.base.data.operator.jdbc.UpdateItem;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.store.IDataStore;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.console.entity.execution.ExecutionContextEntity;
import io.innospots.workflow.console.entity.execution.FlowExecutionEntity;
import io.innospots.workflow.console.converter.execution.FlowExecutionConverter;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * flow execution crud
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/15
 */
public class JdbcFlowExecutionOperator implements IDataStore<FlowExecution>, IFlowExecutionOperator {

    private IDataOperator dataOperator;

    private IExecutionContextOperator executionContextOperator;

    public JdbcFlowExecutionOperator(IDataOperator dataOperator, IExecutionContextOperator executionContextOperator) {
        this.dataOperator = dataOperator;
        this.executionContextOperator = executionContextOperator;
    }

    @Override
    public FlowExecution getFlowExecutionById(String flowExecutionId, boolean includeContext) {
        String key = BeanUtils.getFieldName(FlowExecutionEntity::getFlowExecutionId, true);
        DataBody<Map<String, Object>> flowExecutionResp = dataOperator.selectForObject(FlowExecutionEntity.TABLE_NAME, key, flowExecutionId);
        FlowExecution flowExecution = null;
        if (flowExecutionResp != null && flowExecutionResp.getBody() instanceof Map) {
            flowExecution = fillFlowExecution(flowExecutionResp.getBody(), includeContext);
        }//end flow execution resp

        return flowExecution;
    }

    @Override
    public FlowExecution getLatestFlowExecution(Long flowInstanceId, Integer revision, boolean includeContext) {
        SelectClause selectClause = buildClause(flowInstanceId, revision, null, null, 0, 1, "start_time desc");
        FlowExecution flowExecution = null;
        PageBody<Map<String, Object>> response = dataOperator.selectForList(selectClause);
        if (response != null && response.getList() != null && response.getList().size() > 0) {
            flowExecution = fillFlowExecution(response.getList().get(0), includeContext);
        }
        return flowExecution;
    }

    @Override
    public PageBody<FlowExecutionBase> pageFlowExecutions(Long flowInstanceId, Integer revision, List<String> statuses,
                                                          String startTime, String endTime, Integer page, Integer size) {
        SelectClause selectClause = buildClause(flowInstanceId, revision, startTime, endTime, page, size, "start_time desc");
        return parse(dataOperator.selectForList(selectClause));
    }


    @Override
    public PageBody<FlowExecutionBase> pageLatestFlowExecutions(Long flowInstanceId, Integer revision, List<String> statuses, Integer page, Integer size) {
        SelectClause selectClause = buildClause(flowInstanceId, revision, null, null, page, size, "start_time desc");
        return parse(dataOperator.selectForList(selectClause));
    }

    @Override
    public boolean updateStatus(String executionId, ExecutionStatus status, String message) {
        UpdateItem updateItem = new UpdateItem();
        String flowExecutionId = BeanUtils.getFieldName(FlowExecutionEntity::getFlowExecutionId, true);
        updateItem.addCondition(flowExecutionId, Opt.EQUAL, executionId, FieldValueType.STRING);
        Map<String, Object> data = new HashMap<>();
        data.put(BeanUtils.getFieldName(FlowExecutionEntity::getStatus, true), status.name());
        if (message != null) {
            data.put(BeanUtils.getFieldName(FlowExecutionEntity::getMessage, true), message);
        }
        updateItem.setData(data);
        dataOperator.update(FlowExecutionEntity.TABLE_NAME, updateItem);
        return false;
    }

    private SelectClause buildClause(Long flowInstanceId, Integer revision, List<String> statuses,
                                     String startTime, String endTime, String input, String output,
                                     Integer page, Integer size, String orderBy) {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName("flow_execution fe left join flow_execution_context fec on fe.flow_execution_id = fec.execution_id");
        if (page != null && size != null) {
            selectClause.setPage(page);
            selectClause.setSize(size);
        }
        selectClause.addOrderBy(orderBy);
        selectClause.addWhere(BeanUtils.getFieldName(FlowExecutionEntity::getFlowInstanceId, true), flowInstanceId, Opt.EQUAL);
        if (CollectionUtils.isNotEmpty(statuses)) {
            selectClause.addWhere(BeanUtils.getFieldName(FlowExecutionEntity::getStatus, true), statuses, Opt.IN);
        }
        if (revision != null) {
            selectClause.addWhere(BeanUtils.getFieldName(FlowExecutionEntity::getRevision, true), revision, Opt.EQUAL);
        }
        if (StringUtils.isNotBlank(input)) {
            selectClause.addWhere(BeanUtils.getFieldName(ExecutionContextEntity::getInputs, true), "%" + input + "%", Opt.LIKE);
        }
        if (StringUtils.isNotBlank(output)) {
            selectClause.addWhere(BeanUtils.getFieldName(ExecutionContextEntity::getOutputs, true), "%" + output + "%", Opt.LIKE);
        }
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            selectClause.addWhereRange(BeanUtils.getFieldName(FlowExecutionEntity::getStartTime, true), startTime, endTime);
        }
        return selectClause;
    }

    private SelectClause buildClause(Long flowInstanceId, Integer revision,
                                     String startTime, String endTime,
                                     Integer page, Integer size, String orderBy) {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(FlowExecutionEntity.TABLE_NAME);
        if (page != null && size != null) {
            selectClause.setPage(page);
            selectClause.setSize(size);
        }
        selectClause.addOrderBy(orderBy);
        selectClause.addWhere(BeanUtils.getFieldName(FlowExecutionEntity::getFlowInstanceId, true), flowInstanceId, Opt.EQUAL);
        if (revision != null) {
            selectClause.addWhere(BeanUtils.getFieldName(FlowExecutionEntity::getRevision, true), revision, Opt.EQUAL);
        }
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            selectClause.addWhereRange(BeanUtils.getFieldName(FlowExecutionEntity::getStartTime, true), startTime, endTime);
        }
        return selectClause;
    }

    private PageBody<FlowExecutionBase> parse(PageBody<Map<String, Object>> innospotResponse) {
        PageBody<FlowExecutionBase> flowPageBody = new PageBody<>();
        List<FlowExecutionBase> flowExecutions = new ArrayList<>();
        if (innospotResponse != null && CollectionUtils.isNotEmpty(innospotResponse.getList())) {
            for (Map<String, Object> body : innospotResponse.getList()) {
                if (body instanceof Map) {
                    FlowExecution flowExecution = fillFlowExecution((Map<String, Object>) body, false);
                    flowExecutions.add(flowExecution);
                }
            }//end for
            flowPageBody.setTotalPage(innospotResponse.getPagination().getTotalPage());
            flowPageBody.setPageSize(innospotResponse.getPagination().getPageSize());
            flowPageBody.setTotal(innospotResponse.getPagination().getTotal());
            flowPageBody.setCurrent(innospotResponse.getPagination().getCurrent());
        }//end response
        flowPageBody.setList(flowExecutions);

        return flowPageBody;
    }

    private FlowExecution fillFlowExecution(Map<String, Object> data, boolean includeContext) {
        FlowExecution flowExecution = FlowExecutionConverter.INSTANCE.mapToModel(data, true);
        flowExecution.initialize();
        if (includeContext) {
            executionContextOperator.fillFlowExecution(flowExecution, true);
        }//end context

        return flowExecution;
    }


    @Override
    public boolean insert(FlowExecution execution) {
        dataOperator.insert(FlowExecutionEntity.TABLE_NAME, FlowExecutionConverter.INSTANCE.modelToMap(execution, true));
        executionContextOperator.saveExecutionContext(execution);
//        ExecutionContextEntity executionContextEntity = execution.toExecutionContextEntity();
//        dataOperator.insert(ExecutionContextEntity.TABLE_NAME, execution.toExecutionContextMap(true));

        return true;
    }

    @Override
    public boolean insert(List<FlowExecution> executions) {
        //TODO store executions group by workflow instance and datasource id
        for (FlowExecution execution : executions) {
            insert(execution);
        }
        return true;
    }

    @Override
    public boolean update(FlowExecution execution) {
        UpdateItem updateItem = new UpdateItem();
        Map<String, Object> data = FlowExecutionConverter.INSTANCE.modelToMap(execution, true);
        String flowExecutionId = BeanUtils.getFieldName(FlowExecutionEntity::getFlowExecutionId, true);
        updateItem.addCondition(flowExecutionId, Opt.EQUAL, data.get(flowExecutionId), FieldValueType.STRING);
        data.remove(flowExecutionId);
        updateItem.setData(data);
        dataOperator.update(FlowExecutionEntity.TABLE_NAME, updateItem);
        return true;
    }

    @Override
    public boolean update(List<FlowExecution> executions) {
        for (FlowExecution execution : executions) {
            update(execution);
        }
        return true;
    }

    @Override
    public void close() {

    }
}
