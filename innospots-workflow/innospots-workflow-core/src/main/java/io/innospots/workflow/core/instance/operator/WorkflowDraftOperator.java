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

package io.innospots.workflow.core.instance.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.engine.FlowEngineManager;
import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.enums.FlowVersion;
import io.innospots.workflow.core.exception.WorkflowPublishException;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.instance.converter.WorkflowInstanceConverter;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceCacheDao;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceDao;
import io.innospots.workflow.core.instance.dao.WorkflowRevisionDao;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceCacheEntity;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import io.innospots.workflow.core.instance.entity.WorkflowRevisionEntity;
import io.innospots.workflow.core.instance.model.Edge;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.innospots.workflow.core.instance.operator.WorkflowBodyOperator.CACHE_NAME;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/12
 */
@Slf4j
@AllArgsConstructor
public class WorkflowDraftOperator {

    private WorkflowInstanceCacheDao workflowInstanceCacheDao;

    private WorkflowInstanceDao workflowInstanceDao;

    private WorkflowRevisionDao workflowRevisionDao;

    private NodeInstanceOperator nodeInstanceOperator;

    private EdgeOperator edgeOperator;

    /**
     * save instance to cache
     *
     * @param workflowBaseBody workflow instance
     * @return boolean
     */
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
     * get draft workflow by id
     *
     * @param flowInstanceId primary id
     * @return workflow instance body
     */
    public WorkflowBody getDraftWorkflow(Long flowInstanceId) {
        WorkflowInstanceCacheEntity cacheEntity = workflowInstanceCacheDao.selectById(flowInstanceId);
        WorkflowBody flow;
        if (cacheEntity == null || cacheEntity.getFlowInstance() == null) {
            flow = getDraftWorkflowById(flowInstanceId);
        } else {
            flow = JSONUtils.parseObject(cacheEntity.getFlowInstance(), WorkflowBody.class);
        }
        return flow;
    }

    public WorkflowBody getDraftWorkflow(String flowKey) {
        WorkflowInstanceEntity entity = workflowInstanceDao.getInstanceByFlowKey(flowKey);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), flowKey);
        }
        return fillNodeAndEdge(entity);
    }



    public List<Map<String, Object>> selectNodeInputFields(Long workflowInstanceId, String nodeKey, Set<String> sourceNodeKeys) {
        WorkflowBaseBody workflowBaseBody = getDraftWorkflow(workflowInstanceId);
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
     *  Gets the output field of the workflow instance node.
     *  By default, it is obtained from the cache. The cache does not get the draft instance
     * @param workflowInstanceId
     * @param nodeKey  nodeKey
     * @return
     */
    public List<Map<String, Object>> getNodeOutputFieldOfInstance(Long workflowInstanceId, String nodeKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        WorkflowBaseBody workflowBaseBody = getDraftWorkflow(workflowInstanceId);

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


    /**
     * modify flow instance
     * @param workflowBaseBody workflow instance
     * @return workflow instance
     */
    @Transactional(rollbackFor = {Exception.class})
    public WorkflowBaseBody saveDraft(WorkflowBaseBody workflowBaseBody) {
        //valid node not circle
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

        nodeInstanceOperator.saveDraftNodeInstances(workflowBaseBody.getWorkflowInstanceId(),
                workflowBaseBody.getNodes());

        edgeOperator.saveDraftEdgeInstances(workflowBaseBody.getWorkflowInstanceId(),
                workflowBaseBody.getEdges());

        workflowBaseBody = fillNodeAndEdge(entity);

        saveFlowInstanceToCache(workflowBaseBody);

        //EventBusCenter.postSync(new InstanceUpdateEvent(workflowBaseBody.getWorkflowInstanceId(), NodeReferenceListener.APP_USE_ADD));
        return workflowBaseBody;
    }


    @Transactional(rollbackFor = {Exception.class})
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public synchronized boolean publish(Long workflowInstanceId, String description,int maxVersionKeep) {
        //check instance exits
        WorkflowInstanceEntity entity = workflowInstanceDao.selectById(workflowInstanceId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "flow instance not exist");
        }

        WorkflowBody draftWorkflowBody = this.getDraftWorkflow(workflowInstanceId);
//        WorkflowBody draftWorkflowBody = getFlowBody(entity, FlowVersion.DRAFT.getVersion(), true);

        if (draftWorkflowBody.isEmpty()) {
            throw WorkflowPublishException.buildDraftMissingException(this.getClass(), "node or edge is missing");
        }

        if (entity.getRevision() > 0) {
            WorkflowBody workflowBody = fillNodeAndEdge(entity);
            if (draftWorkflowBody.equalContent(workflowBody)) {
                throw WorkflowPublishException.buildUnchangedException(this.getClass(), "the workflow is not be changed, revision:" + workflowBody.getRevision());
            }
        }

        IFlowEngine flowEngine = FlowEngineManager.eventFlowEngine();
        BuildProcessInfo buildProcessInfo = flowEngine.prepare(workflowInstanceId, FlowVersion.DRAFT.getVersion(), true);

        if (!buildProcessInfo.isLoaded()) {
            throw WorkflowPublishException.buildBuildingFailedException(this.getClass(), buildProcessInfo.getBuildException(), "workInstanceId:" + workflowInstanceId);
        }

        int revision = 1;
        // get lasted revision
        WorkflowRevisionEntity revisionEntity = workflowRevisionDao.getLastedByWorkflowInstanceId(workflowInstanceId);
        if (revisionEntity != null) {
            revision = revisionEntity.getRevision() + 1;
        }

        entity.setStatus(DataStatus.ONLINE);


        // save node instance of new revision
        int nodeSize = nodeInstanceOperator.publishRevision(workflowInstanceId, revision);
        // save edge instance of new revision
        int edgeSize = edgeOperator.publishRevision(workflowInstanceId, revision);
        // save new revision
        WorkflowRevisionEntity newRevision = WorkflowRevisionEntity.build(workflowInstanceId, revision, Integer.toUnsignedLong(nodeSize), description);
        workflowRevisionDao.insert(newRevision);
        // update flow instance revision
        entity.setRevision(revision);
        entity.setUpdatedTime(LocalDateTime.now());
        workflowInstanceDao.updateById(entity);
        workflowInstanceCacheDao.deleteById(workflowInstanceId);

        // delete not keep revision


        QueryWrapper<WorkflowRevisionEntity> allRevisionQuery = new QueryWrapper<>();
        allRevisionQuery.lambda().eq(WorkflowRevisionEntity::getWorkflowInstanceId, workflowInstanceId)
                .gt(WorkflowRevisionEntity::getRevision, 0)
                .orderByDesc(WorkflowRevisionEntity::getRevision);
        List<WorkflowRevisionEntity> revisionEntities = workflowRevisionDao.selectList(allRevisionQuery);

        if (revisionEntities != null && revisionEntities.size() > maxVersionKeep) {
            for (int i = maxVersionKeep; i < revisionEntities.size(); i++) {
                WorkflowRevisionEntity existingRevision = revisionEntities.get(i);
                log.info("delete workflow revision, instanceId: {}, revisionId:{}", workflowInstanceId, existingRevision.getRevision());
                workflowRevisionDao.deleteById(existingRevision.getFlowRevisionId());
                nodeInstanceOperator.deleteByWorkflowInstanceIdAndRevision(workflowInstanceId, existingRevision.getRevision());
                edgeOperator.deleteByWorkflowInstanceIdAndRevision(workflowInstanceId, existingRevision.getRevision());
            }
        }


        log.info("publish workflow, workflowInstanceId:{}, revision:{}, nodeSize:{},edgeSize:{}", workflowInstanceId, revision, nodeSize, edgeSize);
        //ApplicationContextUtils.sendAppEvent(new FlowPublishEvent(workflowInstanceId,revision));
        return revision > 0;
    }


    private WorkflowBody getDraftWorkflowById(Long workflowInstanceId) {
        WorkflowInstanceEntity entity = workflowInstanceDao.selectById(workflowInstanceId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), workflowInstanceId);
        }

        return fillNodeAndEdge(entity);
    }


    private WorkflowBody fillNodeAndEdge(WorkflowInstanceEntity entity) {
        WorkflowBody flowInstance = WorkflowInstanceConverter.INSTANCE.entityToFlowBody(entity);
        flowInstance.setNodes(nodeInstanceOperator.getNodeInstanceByFlowInstanceId(entity.getWorkflowInstanceId(), entity.getRevision()));
        flowInstance.setEdges(edgeOperator.getEdgeByFlowInstanceId(entity.getWorkflowInstanceId(), entity.getRevision()));
        //初始化
        flowInstance.initialize();
        return flowInstance;
    }


}
