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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.workflow.core.execution.dao.ExecutionContextDao;
import io.innospots.workflow.core.execution.dao.FlowExecutionDao;
import io.innospots.workflow.core.execution.dao.NodeExecutionDao;
import io.innospots.workflow.core.execution.dao.ScheduledNodeExecutionDao;
import io.innospots.workflow.core.execution.entity.FlowExecutionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExecutionManagerOperator
 *
 * @author Wren
 * @date 2022/12/4-19:27
 */
public class ExecutionManagerOperator {

    private ExecutionContextDao executionContextDao;

    private FlowExecutionDao flowExecutionDao;

    private NodeExecutionDao nodeExecutionDao;

    private ScheduledNodeExecutionDao scheduledNodeExecutionDao;

    public ExecutionManagerOperator(ExecutionContextDao executionContextDao, FlowExecutionDao flowExecutionDao, NodeExecutionDao nodeExecutionDao, ScheduledNodeExecutionDao scheduledNodeExecutionDao) {
        this.executionContextDao = executionContextDao;
        this.flowExecutionDao = flowExecutionDao;
        this.nodeExecutionDao = nodeExecutionDao;
        this.scheduledNodeExecutionDao = scheduledNodeExecutionDao;
    }


    public Long getFlowExecutionCount(Long flowInstanceId) {
        QueryWrapper<FlowExecutionEntity> query = new QueryWrapper<>();
        query.lambda().eq(FlowExecutionEntity::getFlowInstanceId, flowInstanceId);
        return flowExecutionDao.selectCount(query);
    }

    public int deleteExecutionLogHis(Long flowInstanceId, LocalDateTime operateTime) {
        QueryWrapper<FlowExecutionEntity> wrapper = new QueryWrapper<>();
        wrapper.select("flow_execution_id");
        wrapper.lambda().eq(FlowExecutionEntity::getFlowInstanceId, flowInstanceId)
                .le(FlowExecutionEntity::getCreatedTime, operateTime);
        List<FlowExecutionEntity> list = this.flowExecutionDao.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        List<String> executionIdList = list.stream().map(FlowExecutionEntity::getFlowExecutionId).collect(Collectors.toList());
        this.flowExecutionDao.deleteBatchIds(executionIdList);
        this.nodeExecutionDao.deleteBatchIds(executionIdList);
        this.executionContextDao.deleteBatchIds(executionIdList);
        this.scheduledNodeExecutionDao.deleteBatchIds(executionIdList);
        return executionIdList.size();
    }


    public int deleteExecutionLog(Long flowInstanceId) {
        QueryWrapper<FlowExecutionEntity> wrapper = new QueryWrapper<>();
        wrapper.select("flow_execution_id");
        wrapper.lambda().eq(FlowExecutionEntity::getFlowInstanceId, flowInstanceId);
        List<FlowExecutionEntity> list = this.flowExecutionDao.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        List<String> executionIdList = list.stream().map(FlowExecutionEntity::getFlowExecutionId).collect(Collectors.toList());
        this.flowExecutionDao.deleteBatchIds(executionIdList);
        this.nodeExecutionDao.deleteBatchIds(executionIdList);
        this.executionContextDao.deleteBatchIds(executionIdList);
        this.scheduledNodeExecutionDao.deleteBatchIds(executionIdList);
        return executionIdList.size();
    }

}
