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

package io.innospots.workflow.core.execution.operator.jdbc;

import io.innospots.base.condition.Factor;
import io.innospots.base.condition.Opt;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.jdbc.SelectClause;
import io.innospots.base.data.operator.jdbc.UpdateItem;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.store.IDataStore;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.entity.ScheduledNodeExecutionEntity;
import io.innospots.workflow.core.execution.converter.ScheduledNodeExecutionConverter;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.model.scheduled.ScheduledNodeExecution;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.innospots.base.utils.time.DateTimeUtils.DEFAULT_DATETIME_PATTERN;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/5
 */
public class ScheduledNodeExecutionOperator implements IDataStore<ScheduledNodeExecution>, IScheduledNodeExecutionOperator {

    private DataConnectionMinderManager dataConnectionMinderManager;

    private IDataOperator dataOperator;

    public ScheduledNodeExecutionOperator(DataConnectionMinderManager dataConnectionMinderManager,
                                          IDataOperator defaultDataOperator) {
        this.dataConnectionMinderManager = dataConnectionMinderManager;
        this.dataOperator = defaultDataOperator;
    }

    @Override
    public List<ScheduledNodeExecution> scanArrivalTimeExecutions(String datasourceCode, String serverKey, Integer[] shardingKeys) {
        if (ArrayUtils.isEmpty(shardingKeys)) {
            return Collections.emptyList();
        }
        SelectClause selectClause = buildClause(null, shardingKeys, ExecutionStatus.PENDING, LocalDateTime.now(), false);
        PageBody<Map<String, Object>> response = dataOperator.selectForList(selectClause);
        if (response != null && response.getList() != null) {
            List<String> nodeExecutionIds = new ArrayList<>();
            String executionField = BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getNodeExecutionId, true);
            for (Map<String, Object> item : response.getList()) {
                nodeExecutionIds.add(item.get(executionField).toString());
            }
            updateExecution(datasourceCode, serverKey, nodeExecutionIds, ExecutionStatus.PENDING, ExecutionStatus.READY);
        }


        SelectClause serverClause = buildClause(serverKey, null, ExecutionStatus.READY, null, true);
        PageBody<Map<String, Object>> serverResp = dataOperator.selectForList(serverClause);
        List<ScheduledNodeExecution> scheduledNodeExecutions = new ArrayList<>();
        if (serverResp == null || CollectionUtils.isEmpty(serverResp.getList())) {
            return Collections.emptyList();
        }
        for (Map<String, Object> item : serverResp.getList()) {
            scheduledNodeExecutions.add(ScheduledNodeExecutionConverter.INSTANCE.mapToModel(item, true));
        }
        return scheduledNodeExecutions;
    }

    @Override
    public void updateExecution(String datasourceCode, String serverKey, List<String> nodeExecutionIds, ExecutionStatus oldStatus, ExecutionStatus newStatus) {
        List<UpdateItem> updateItems = new ArrayList<>();
        for (String nodeExecutionId : nodeExecutionIds) {
            UpdateItem updateItem = new UpdateItem();
            Map<String, Object> data = new HashMap<>();
            data.put(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getStatus, true), newStatus);
            data.put(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getServerKey, true), serverKey);
            updateItem.setData(data);
            List<Factor> factors = new ArrayList<>();
            Factor factor = new Factor();
            factor.setValue(nodeExecutionId);
            factor.setOpt(Opt.EQUAL);
            factor.setValueType(FieldValueType.STRING);
            factor.setCode(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getNodeExecutionId, true));
            factors.add(factor);
            factor = new Factor();
            factor.setCode(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getStatus, true));
            factor.setOpt(Opt.EQUAL);
            factor.setValueType(FieldValueType.STRING);
            factor.setValue(oldStatus);
            updateItem.setConditions(factors);
            updateItems.add(updateItem);
        }
        dataOperator.updateForBatch(ScheduledNodeExecutionEntity.TABLE_NAME, updateItems);
    }


    private SelectClause buildClause(String serverKey,
                                     Integer[] shardingKey, ExecutionStatus status, LocalDateTime localDateTime, boolean allColumn) {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(ScheduledNodeExecutionEntity.TABLE_NAME);
        if (!allColumn) {
            List<String> columns = new ArrayList<>();
            columns.add(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getNodeExecutionId, true));
            selectClause.setColumns(columns);
        }
        if (serverKey != null) {
            selectClause.addWhere(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getServerKey, true), serverKey, Opt.EQUAL);
        }
        if (shardingKey != null) {
            selectClause.addWhereInclude(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getShardingKey, true), Arrays.asList(shardingKey), FieldValueType.INTEGER);
        }

        if (status != null) {
            selectClause.addWhere(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getStatus, true), status.name(), Opt.EQUAL);
        }
        if (localDateTime != null) {
            String nowDateTime = localDateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN));
            selectClause.addWhere(BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getScheduledTime, true), nowDateTime, Opt.LESS);
        }

        return selectClause;
    }

    @Override
    public boolean insert(ScheduledNodeExecution execution) {
        dataOperator.insert(ScheduledNodeExecutionEntity.TABLE_NAME, ScheduledNodeExecutionConverter.INSTANCE.modelToMap(execution, true));
        return false;
    }

    @Override
    public boolean insert(List<ScheduledNodeExecution> executions) {
        for (ScheduledNodeExecution execution : executions) {
            insert(execution);
        }
        return true;
    }

    @Override
    public boolean update(ScheduledNodeExecution execution) {
        UpdateItem updateItem = new UpdateItem();
        Map<String, Object> data = ScheduledNodeExecutionConverter.INSTANCE.modelToMap(execution, true);
        String nodeExecutionId = BeanUtils.getFieldName(ScheduledNodeExecutionEntity::getNodeExecutionId, true);
        updateItem.addCondition(nodeExecutionId, Opt.EQUAL, data.get(nodeExecutionId), FieldValueType.STRING);
        data.remove(nodeExecutionId);
        updateItem.setData(data);
        dataOperator.update(ScheduledNodeExecutionEntity.TABLE_NAME, updateItem);
        return true;
    }

    @Override
    public boolean update(List<ScheduledNodeExecution> executions) {
        for (ScheduledNodeExecution execution : executions) {
            insert(execution);
        }
        return true;
    }

    @Override
    public void close() {

    }

}
