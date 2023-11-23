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

package io.innospots.workflow.core.flow.draft;

import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.enums.FlowVersion;
import io.innospots.workflow.core.flow.WorkflowBaseBody;
import io.innospots.workflow.core.flow.WorkflowBody;
import io.innospots.workflow.core.instance.converter.WorkflowInstanceConverter;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceCacheDao;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceDao;
import io.innospots.workflow.core.instance.dao.WorkflowRevisionDao;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceCacheEntity;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import io.innospots.workflow.core.instance.entity.WorkflowRevisionEntity;
import io.innospots.workflow.core.instance.model.Edge;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.instance.operator.EdgeOperator;
import io.innospots.workflow.core.instance.operator.NodeInstanceOperator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/12
 */
@Slf4j
@AllArgsConstructor
public class WorkflowDraftDbOperator implements IWorkflowDraftOperator{


    public static final String CACHE_NAME = "CACHE_FLOW_INSTANCE";


    private InnospotsWorkflowProperties innospotsWorkflowProperties;

    private WorkflowRevisionDao workflowRevisionDao;

    private WorkflowInstanceCacheDao workflowInstanceCacheDao;

    private WorkflowInstanceDao workflowInstanceDao;

    private NodeInstanceOperator nodeInstanceOperator;

    private EdgeOperator edgeOperator;




    /**
     * save instance to cache
     *
     * @param workflowBaseBody
     * @return
     */
    @Override
    public boolean saveFlowInstanceToCache(WorkflowBaseBody workflowBaseBody) {
        WorkflowInstanceCacheEntity cacheEntity = workflowInstanceCacheDao.selectById(workflowBaseBody.getWorkflowInstanceId());

        if (cacheEntity == null) {
            cacheEntity = new WorkflowInstanceCacheEntity();
        }
        cacheEntity.setFlowInstance(JSONUtils.toJsonString(workflowBaseBody));

        if (cacheEntity.getWorkflowInstanceId() == null) {
            cacheEntity.setWorkflowInstanceId(workflowBaseBody.getWorkflowInstanceId());
            workflowInstanceCacheDao.insert(cacheEntity);
        } else {
            workflowInstanceCacheDao.updateById(cacheEntity);
        }
        return true;
    }


    /**
     * 从缓存中获取flowInstance的值，或者获取草稿中的值
     *
     * @param flowInstanceId
     * @return
     */
    @Override
    public WorkflowBaseBody getFlowInstanceDraftOrCache(Long flowInstanceId) {
        WorkflowInstanceCacheEntity cacheEntity = workflowInstanceCacheDao.selectById(flowInstanceId);
        WorkflowBaseBody flow;
        if (cacheEntity == null || cacheEntity.getFlowInstance() == null) {
            flow = getWorkflowBody(flowInstanceId, FlowVersion.DRAFT.getVersion(), true);
        } else {
            flow = JSONUtils.parseObject(cacheEntity.getFlowInstance(), WorkflowBaseBody.class);
        }
        return flow;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public WorkflowBaseBody saveCacheToDraft(Long flowInstanceId) {
        WorkflowInstanceCacheEntity cacheEntity = workflowInstanceCacheDao.selectById(flowInstanceId);
        if (cacheEntity == null) {
            return null;
        }
        WorkflowBaseBody flow = JSONUtils.parseObject(cacheEntity.getFlowInstance(), WorkflowBaseBody.class);
        if (flow != null) {
            flow = this.saveDraft(flow);
        }
        return flow;
    }

    @Override
    public WorkflowBaseBody getWorkflowBodyByKey(String flowKey, boolean includeNode) {
        return getWorkflowBodyByKey(flowKey,0,includeNode);
    }

    public List<Map<String, Object>> selectNodeInputFields(Long workflowInstanceId, String nodeKey, Set<String> sourceNodeKeys) {
        WorkflowBaseBody workflowBaseBody = getFlowInstanceDraftOrCache(workflowInstanceId);
        if (workflowBaseBody == null) {
            log.warn("select input field is null:{}, nodeKey:{}",workflowInstanceId, nodeKey);
            return Collections.emptyList();
        }

        if (CollectionUtils.isEmpty(sourceNodeKeys)) {
            sourceNodeKeys = workflowBaseBody.getEdges().stream().
                    filter(edge -> nodeKey.equals(edge.getTarget())).
                    map(Edge::getSource).collect(Collectors.toSet());
//            sourceNodeKeys = this.edgeOperator.selectSourceNodeKey(workflowInstanceId,0,nodeKey);
        }
        if (CollectionUtils.isEmpty(sourceNodeKeys)) {
            log.warn("sourceNode key, select input field is null:{}, nodeKey:{}",workflowInstanceId, nodeKey);
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        final Set<String> finalSourceNodeKeys = sourceNodeKeys;
        List<NodeInstance> nodeInstances = workflowBaseBody.getNodes().stream()
                .filter(nodeInstance -> finalSourceNodeKeys.contains(nodeInstance.getNodeKey()))
                .collect(Collectors.toList());
//        List<NodeInstance> nodeInstances = this.nodeInstanceOperator.listNodeInstancesByNodeKeys(workflowInstanceId,0,sourceNodeKeys);
        for (NodeInstance nodeInstance : nodeInstances) {
            List<ParamField> outputFieldList = nodeInstance.getOutputFields();
            if (CollectionUtils.isNotEmpty(outputFieldList)) {
                Map<String, Object> nodeMap = new HashMap<>();
                nodeMap.put("nodeKey", nodeInstance.getNodeKey());
                nodeMap.put("nodeName", nodeInstance.getDisplayName());
                List<Map<String, Object>> fieldList = toInputFields(outputFieldList);
                nodeMap.put("fields", fieldList);
                result.add(nodeMap);
            }
        }//end for
        if(result.isEmpty()){
            log.warn("select input field is null:{}, nodeKey:{}",workflowInstanceId, nodeKey);
        }
        return result;
    }

    private List<Map<String, Object>> toInputFields(List<ParamField> outputFieldList) {
        List<Map<String, Object>> fieldList = new ArrayList<>();
        outputFieldList.forEach(paramField -> {
            Map<String, Object> field = new HashMap<>();
            field.put("value", paramField.getCode());
            field.put("label", paramField.getName());
            field.put("type", paramField.getValueType() != null ? paramField.getValueType().name() : "");
            fieldList.add(field);
            if (CollectionUtils.isNotEmpty(paramField.getSubFields())) {
                List<Map<String, Object>> subFields = toInputFields(paramField.getSubFields());
                field.put("subFields", subFields);
            }
        });
        return fieldList;
    }


    /**
     * 获取工作流实例节点的输出字段信息，默认从缓存获取，缓存没有获取草稿实例
     *
     * @param workflowInstanceId
     * @param nodeKey            nodeKey
     * @return
     */
    public List<Map<String, Object>> getNodeOutputFieldOfInstance(Long workflowInstanceId, String nodeKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        WorkflowBaseBody workflowBaseBody = getFlowInstanceDraftOrCache(workflowInstanceId);

        if (workflowBaseBody != null && CollectionUtils.isNotEmpty(workflowBaseBody.getNodes())) {
            workflowBaseBody.getNodes().forEach(nodeInstance -> {

                List<ParamField> outputFieldList = nodeInstance.getOutputFields();
                if (CollectionUtils.isNotEmpty(outputFieldList)) {
                    Map<String, Object> nodeMap = new HashMap<>();
                    nodeMap.put("nodeKey", nodeInstance.getNodeKey());
                    nodeMap.put("nodeName", nodeInstance.getDisplayName());
                    List<Map<String, String>> fieldList = new ArrayList<>();
                    outputFieldList.forEach(paramField -> {
                        Map<String, String> field = new HashMap<>();
                        field.put("value", paramField.getCode());
                        field.put("label", paramField.getName());
                        field.put("type", paramField.getValueType() != null ? paramField.getValueType().name() : "");
                        fieldList.add(field);
                    });
                    nodeMap.put("fields", fieldList);
                    result.add(nodeMap);
                }
            });
        }

        return result;
    }


    /**
     * get flow instance by instance id
     *
     * @param workflowInstanceId
     * @param includeNodes
     * @return
     */
//    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#workflowInstanceId + '-' + #revision", condition = "!#includeNodes")
    public WorkflowBody getWorkflowBody(Long workflowInstanceId, Integer revision, Boolean includeNodes) {
        WorkflowInstanceEntity entity = workflowInstanceDao.selectById(workflowInstanceId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), workflowInstanceId, revision);
        }

        return getFlowInstance(entity, revision, includeNodes);
    }

    public WorkflowBody getWorkflowBody(String flowKey, Boolean includeNodes){
        WorkflowInstanceEntity entity = workflowInstanceDao.getInstanceByFlowKey(flowKey);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), flowKey);
        }
        return getFlowInstance(entity,entity.getRevision(),includeNodes);
    }

    private WorkflowBody getFlowInstance(WorkflowInstanceEntity entity, Integer revision, Boolean includeNodes) {
        WorkflowBody flowInstance = WorkflowInstanceConverter.INSTANCE.entityToFlowBody(entity);
        if (includeNodes) {
            //use current revision, if revision is null
            if (revision == null) {
                revision = entity.getRevision();
            } else if (revision != 0) {
                // check revision exist
                WorkflowRevisionEntity workflowRevisionEntity = workflowRevisionDao.getByWorkflowInstanceIdAndRevision(entity.getWorkflowInstanceId(), revision);
                if (workflowRevisionEntity == null) {
                    log.error("flow instance revision not exits workflowInstanceId:{} revision:{}", entity.getWorkflowInstanceId(), revision);
                    throw ResourceException.buildAbandonException(this.getClass(), entity.getWorkflowInstanceId());
                }
            }

            flowInstance.setRevision(revision);

            flowInstance.setNodes(nodeInstanceOperator.getNodeInstanceByFlowInstanceId(entity.getWorkflowInstanceId(), revision));
            flowInstance.setEdges(edgeOperator.getEdgeByFlowInstanceId(entity.getWorkflowInstanceId(), revision));

            //初始化
            flowInstance.initialize();
        }
        return flowInstance;
    }


    /**
     * get flow instance by flowKey and revision
     *
     * @param flowKey
     * @param revision
     * @param includeNodes
     * @return
     */
//    @Override
    public WorkflowBaseBody getWorkflowBodyByKey(String flowKey, Integer revision, Boolean includeNodes) {
        WorkflowInstanceEntity entity = workflowInstanceDao.getInstanceByFlowKey(flowKey);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), flowKey);
        }
        return getFlowInstance(entity, revision, includeNodes);
    }



    /**
     * modify flow instance
     *
     * @param workflowBaseBody
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public WorkflowBaseBody saveDraft(WorkflowBaseBody workflowBaseBody) {
        //验证节点无循环依赖
        if (workflowBaseBody.checkNodeCircle()) {
            throw ValidatorException.buildInvalidException(this.getClass(), "flow instance nodes circular dependency");
        }
        WorkflowInstanceEntity entity = workflowInstanceDao.selectById(workflowBaseBody.getWorkflowInstanceId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "workflow not exist , " + workflowBaseBody.getWorkflowInstanceId());
        }
        boolean change = false;

        if (StringUtils.isNotEmpty(workflowBaseBody.getName()) && !workflowBaseBody.getName().equals(entity.getName())) {
            entity.setName(workflowBaseBody.getName());
            change = true;
        }
        if (change) {
            workflowInstanceDao.updateById(entity);
        }

        //如果节点和边为空，会情况当前实例的所有节点和边
        nodeInstanceOperator.saveDraftNodeInstances(workflowBaseBody.getWorkflowInstanceId(),
                workflowBaseBody.getNodes());

        edgeOperator.saveDraftEdgeInstances(workflowBaseBody.getWorkflowInstanceId(),
                workflowBaseBody.getEdges());

        workflowBaseBody = getWorkflowBody(workflowBaseBody.getWorkflowInstanceId(), FlowVersion.DRAFT.getVersion(), true);
        saveFlowInstanceToCache(workflowBaseBody);

        //EventBusCenter.postSync(new InstanceUpdateEvent(workflowBaseBody.getWorkflowInstanceId(), NodeReferenceListener.APP_USE_ADD));
        return workflowBaseBody;
    }

}
