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

package io.innospots.workflow.console.service;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.base.enums.DataStatus;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.workflow.core.flow.model.WorkflowBaseInfo;
import io.innospots.workflow.core.flow.model.WorkflowInfo;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import io.innospots.workflow.core.instance.model.WorkflowInstance;
import io.innospots.workflow.core.instance.operator.WorkflowBodyOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author Jegy
 * @version 1.0.0
 * @date 2022/3/27
 */
@Slf4j
@Service
public class WorkflowService {


    private final WorkflowInstanceOperator workflowInstanceOperator;

    private final WorkflowBodyOperator workflowBodyOperator;

    private final ScheduleFlowJobService scheduleFlowJobService;

    private final WorkflowExecutionService executionService;

    public WorkflowService(WorkflowInstanceOperator workflowInstanceOperator,
                           WorkflowBodyOperator workflowBodyOperator, ScheduleFlowJobService scheduleFlowJobService,
                           WorkflowExecutionService executionService) {
        this.workflowInstanceOperator = workflowInstanceOperator;
        this.workflowBodyOperator = workflowBodyOperator;
        this.scheduleFlowJobService = scheduleFlowJobService;
        this.executionService = executionService;
    }

    public WorkflowInstance createWorkflow(WorkflowInfo workflow) {
        return workflowInstanceOperator.createWorkflow(workflow);
    }

    public boolean updateWorkflow(WorkflowInstance workflow){
        return workflowInstanceOperator.updateWorkflow(workflow);
    }

    public boolean removeWorkflowToRecycle(Long workflowInstanceId){
        return workflowInstanceOperator.removeWorkflowToRecycle(workflowInstanceId);
    }

    public boolean deleteByWorkflowBody(Long workflowInstanceId){
        Boolean delete = workflowBodyOperator.deleteByWorkflowBody(workflowInstanceId);
        if (delete) {
            executionService.deleteExecutionByFlowInstanceId(workflowInstanceId);
            WorkflowInstanceEntity instanceEntity = workflowInstanceOperator.getWorkflowInstanceEntity(workflowInstanceId);
            scheduleFlowJobService.deleteSchedule(instanceEntity);
        }
        return delete;
    }

    public boolean updateWorkflowStatus(long workflowInstanceId, DataStatus dataStatus){
        boolean up = workflowInstanceOperator.updateWorkflowStatus(workflowInstanceId, dataStatus);
        if (up) {
            WorkflowInstanceEntity instanceEntity = workflowInstanceOperator.getWorkflowInstanceEntity(workflowInstanceId);
            instanceEntity.setStatus(String.valueOf(dataStatus));
            scheduleFlowJobService.updateScheduleStatus(instanceEntity);
        }
        return up;
    }

    public WorkflowInstance getWorkflowInstance(long workflowInstanceId) {
        return workflowInstanceOperator.getWorkflowInstance(workflowInstanceId);
    }

    public List<WorkflowBaseInfo> listWorkflows(@PathVariable String triggerCode){
        return workflowInstanceOperator.listWorkflows(triggerCode);
    }

    public PageBody<WorkflowInstance> getWorkflows(FormQuery request) {
        return workflowInstanceOperator.pageWorkflows(request);
    }

}