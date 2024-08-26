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

import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.connector.schema.model.SchemaRegistryType;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.console.model.WorkflowQuery;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.innospots.workflow.core.flow.model.WorkflowBaseInfo;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.flow.model.WorkflowInfo;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.instance.model.WorkflowInstance;
import io.innospots.workflow.core.instance.operator.NodeInstanceOperator;
import io.innospots.workflow.core.instance.operator.WorkflowBodyOperator;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import io.innospots.workflow.core.node.executor.TriggerNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final NodeInstanceOperator nodeInstanceOperator;

    public WorkflowService(WorkflowInstanceOperator workflowInstanceOperator,
                           NodeInstanceOperator nodeInstanceOperator,
                           WorkflowBodyOperator workflowBodyOperator,
                           ScheduleFlowJobService scheduleFlowJobService,
                           WorkflowExecutionService executionService) {
        this.workflowInstanceOperator = workflowInstanceOperator;
        this.nodeInstanceOperator = nodeInstanceOperator;
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

    public PageBody<WorkflowInstance> getWorkflows(WorkflowQuery request) {
        return workflowInstanceOperator.pageWorkflows(request);
    }

    public SchemaRegistry getApiWorkflowSchemaRegistry(String registryId){
        WorkflowBody workflowBody = workflowBodyOperator.getWorkflowBody(Long.parseLong(registryId),true);
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        for (NodeInstance start : workflowBody.getStarts()) {
            if(start.getPrimitive() == NodePrimitive.trigger || start.getPrimitive() == NodePrimitive.apiTrigger){
                try{
                    BaseNodeExecutor nodeExecutor = NodeExecutorFactory.build(workflowBody.identifier(), start);
                    if(nodeExecutor instanceof TriggerNode){
                        schemaRegistry.setConfigs(((TriggerNode) nodeExecutor).triggerInfo());
                    }
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                }
                schemaRegistry.setRegistryId(registryId);
                schemaRegistry.setName(workflowBody.getName());
                schemaRegistry.setCode(workflowBody.getFlowKey());
                schemaRegistry.setRegistryType(SchemaRegistryType.WORKFLOW);
                schemaRegistry.setCategoryId(workflowBody.getCategoryId());
                List<SchemaField> schemaFields = new ArrayList<>();
                for (ParamField inputField : start.getInputFields()) {
                    SchemaField schemaField = SchemaField.convert(inputField);
                    schemaField.setRegistryId(registryId);
                    schemaFields.add(schemaField);
                }
                schemaRegistry.setSchemaFields(schemaFields);
            }
        }//end for
        return schemaRegistry;
    }

    public PageBody<SchemaRegistry> pageApiWorkflowRegistries(WorkflowQuery request) {
        PageBody<WorkflowInstance> pageBody = workflowInstanceOperator.pageWorkflows(request);
        PageBody<SchemaRegistry> registryPageBody = new PageBody<>();
        List<Long> workflowInstanceIds = new ArrayList<>();
        List<Integer> nodeDefinitionIds = new ArrayList<>();
        for (WorkflowInstance workflowInstance : pageBody.getList()) {
            workflowInstanceIds.add(workflowInstance.getWorkflowInstanceId());
            nodeDefinitionIds.add(workflowInstance.getTriggerNode().getNodeId());
        }
        List<NodeInstance> nodeInstances = nodeInstanceOperator.listNodeInstances(workflowInstanceIds, nodeDefinitionIds);
        Map<Long, NodeInstance> nodeInstanceMap = nodeInstances.stream().collect(Collectors.toMap(NodeInstance::getWorkflowInstanceId, nodeInstance -> nodeInstance));
        for (WorkflowInstance workflowInstance : pageBody.getList()) {
            SchemaRegistry registry = new SchemaRegistry();
            registry.setRegistryId(workflowInstance.getWorkflowInstanceId() + "");
            registry.setName(workflowInstance.getName());
            registry.setCode(workflowInstance.getFlowKey());
            registry.setCategoryId(workflowInstance.getCategoryId());
            registry.setUpdatedBy(workflowInstance.getUpdatedBy());
            registry.setUpdatedTime(workflowInstance.getUpdatedTime());
            registryPageBody.add(registry);
        }
        return registryPageBody;
    }

}