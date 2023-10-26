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

package io.innospots.workflow.console.operator.instance;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.workflow.console.dao.instance.WorkflowInstanceCacheDao;
import io.innospots.workflow.console.dao.instance.WorkflowRevisionDao;
import io.innospots.workflow.console.entity.instance.WorkflowInstanceCacheEntity;
import io.innospots.workflow.console.entity.instance.WorkflowInstanceEntity;
import io.innospots.workflow.console.entity.instance.WorkflowRevisionEntity;
import io.innospots.workflow.console.enums.FlowVersion;
import io.innospots.workflow.console.events.InstanceUpdateEvent;
import io.innospots.workflow.console.exception.WorkflowPublishException;
import io.innospots.workflow.console.listener.AppUseListener;
import io.innospots.workflow.console.mapper.instance.WorkflowInstanceBeanConverter;
import io.innospots.workflow.core.config.InnospotWorkflowProperties;
import io.innospots.workflow.core.engine.FlowEngineManager;
import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.flow.BuildProcessInfo;
import io.innospots.workflow.core.flow.WorkflowBaseBody;
import io.innospots.workflow.core.flow.WorkflowBody;
import io.innospots.workflow.core.flow.instance.IWorkflowCacheDraftOperator;
import io.innospots.workflow.core.node.instance.Edge;
import io.innospots.workflow.core.node.instance.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class WorkflowBuilderOperator implements IWorkflowCacheDraftOperator {

    public static final String CACHE_NAME = "CACHE_FLOW_INSTANCE";


    private InnospotWorkflowProperties innospotWorkflowProperties;

    private WorkflowRevisionDao workflowRevisionDao;

    private WorkflowInstanceCacheDao workflowInstanceCacheDao;

    private WorkflowInstanceOperator workflowInstanceOperator;

    private NodeInstanceOperator nodeInstanceOperator;

    private EdgeOperator edgeOperator;

    public WorkflowBuilderOperator(WorkflowRevisionDao workflowRevisionDao,
                                   WorkflowInstanceCacheDao workflowInstanceCacheDao,
                                   WorkflowInstanceOperator workflowInstanceOperator,
                                   NodeInstanceOperator nodeInstanceOperator, EdgeOperator edgeOperator,
                                   InnospotWorkflowProperties innospotWorkflowProperties) {
        this.workflowRevisionDao = workflowRevisionDao;
        this.workflowInstanceCacheDao = workflowInstanceCacheDao;
        this.workflowInstanceOperator = workflowInstanceOperator;
        this.nodeInstanceOperator = nodeInstanceOperator;
        this.edgeOperator = edgeOperator;
        this.innospotWorkflowProperties = innospotWorkflowProperties;
    }


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
    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#workflowInstanceId + '-' + #revision", condition = "!#includeNodes")
    public WorkflowBody getWorkflowBody(Long workflowInstanceId, Integer revision, Boolean includeNodes) {
        WorkflowInstanceEntity entity = workflowInstanceOperator.getWorkflowInstanceEntity(workflowInstanceId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), workflowInstanceId, revision);
        }

        return getFlowInstance(entity, revision, includeNodes);
    }

    public WorkflowBody getWorkflowBody(String flowKey, Boolean includeNodes){
        WorkflowInstanceEntity entity = workflowInstanceOperator.getWorkflowInstanceEntity(flowKey);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), flowKey);
        }
        return getFlowInstance(entity,entity.getRevision(),includeNodes);
    }

    private WorkflowBody getFlowInstance(WorkflowInstanceEntity entity, Integer revision, Boolean includeNodes) {
        WorkflowBody flowInstance = WorkflowInstanceBeanConverter.INSTANCE.entityToFlowBody(entity);
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

    public List<WorkflowBody> selectRecentlyUpdateOrOnLine(int recentMinutes){
        List<WorkflowInstanceEntity> entityList = workflowInstanceOperator.selectRecentlyUpdateOrOnLine(recentMinutes);
        if(CollectionUtils.isEmpty(entityList)){
            return Collections.emptyList();
        }
        return entityList.stream().map(WorkflowInstanceBeanConverter.INSTANCE::entityToFlowBody).collect(Collectors.toList());
    }

    /**
     * get flow instance by flowKey and revision
     *
     * @param flowKey
     * @param revision
     * @param includeNodes
     * @return
     */
    @Override
    public WorkflowBaseBody getWorkflowBodyByKey(String flowKey, Integer revision, Boolean includeNodes) {
        WorkflowInstanceEntity entity = workflowInstanceOperator.getWorkflowInstanceEntity(flowKey);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), flowKey);
        }
        return getFlowInstance(entity, revision, includeNodes);
    }

    public boolean deleteByWorkflowInstanceId(Long workflowInstanceId) {
        boolean up = this.workflowInstanceOperator.deleteWorkflowInstance(workflowInstanceId);
        if(!up){
            return false;
        }
        up = this.nodeInstanceOperator.deleteNodes(workflowInstanceId) & up;

        BeanContextAware.sendAppEvent(new InstanceUpdateEvent(workflowInstanceId, AppUseListener.APP_USE_DELETE));

        up = this.edgeOperator.deleteEdges(workflowInstanceId) & up;
        QueryWrapper<WorkflowRevisionEntity> revisionQueryWrapper = new QueryWrapper<>();
        revisionQueryWrapper.lambda().eq(WorkflowRevisionEntity::getWorkflowInstanceId, workflowInstanceId);

        up = this.workflowRevisionDao.delete(revisionQueryWrapper) > 0 & up;
        QueryWrapper<WorkflowInstanceCacheEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(WorkflowInstanceCacheEntity::getWorkflowInstanceId, workflowInstanceId);

        up = this.workflowInstanceCacheDao.delete(wrapper) > 0 & up;
        return up;
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
        WorkflowInstanceEntity entity = workflowInstanceOperator.getWorkflowInstanceEntity(workflowBaseBody.getWorkflowInstanceId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "workflow not exist , " + workflowBaseBody.getWorkflowInstanceId());
        }
        boolean change = false;

        if (StringUtils.isNotEmpty(workflowBaseBody.getName()) && !workflowBaseBody.getName().equals(entity.getName())) {
            entity.setName(workflowBaseBody.getName());
            change = true;
        }
        if (change) {
            workflowInstanceOperator.updateById(entity);
        }

        //如果节点和边为空，会情况当前实例的所有节点和边
        nodeInstanceOperator.saveDraftNodeInstances(workflowBaseBody.getWorkflowInstanceId(),
                workflowBaseBody.getNodes());

        edgeOperator.saveDraftEdgeInstances(workflowBaseBody.getWorkflowInstanceId(),
                workflowBaseBody.getEdges());

        workflowBaseBody = getWorkflowBody(workflowBaseBody.getWorkflowInstanceId(), FlowVersion.DRAFT.getVersion(), true);
        saveFlowInstanceToCache(workflowBaseBody);

        BeanContextAware.sendAppEvent(new InstanceUpdateEvent(workflowBaseBody.getWorkflowInstanceId(), AppUseListener.APP_USE_ADD));
        return workflowBaseBody;
    }


    /**
     * publish flow instance
     *
     * @param workflowInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class})
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public synchronized boolean publish(Long workflowInstanceId, String description) {
        //check instance exits
        WorkflowInstanceEntity entity = workflowInstanceOperator.getWorkflowInstanceEntity(workflowInstanceId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "flow instance not exist");
        }
        WorkflowBody draftWorkflowBody = getFlowInstance(entity, FlowVersion.DRAFT.getVersion(), true);

        if (draftWorkflowBody.isEmpty()) {
            throw WorkflowPublishException.buildDraftMissingException(this.getClass(), "node or edge is missing");
        }

        if (entity.getRevision() > 0) {
            WorkflowBody workflowBody = getFlowInstance(entity, null, true);

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
        workflowInstanceOperator.updateById(entity);
        workflowInstanceCacheDao.deleteById(workflowInstanceId);

        // delete not keep revision


        QueryWrapper<WorkflowRevisionEntity> allRevisionQuery = new QueryWrapper<>();
        allRevisionQuery.lambda().eq(WorkflowRevisionEntity::getWorkflowInstanceId, workflowInstanceId)
                .gt(WorkflowRevisionEntity::getRevision, 0)
                .orderByDesc(WorkflowRevisionEntity::getRevision);
        List<WorkflowRevisionEntity> revisionEntities = workflowRevisionDao.selectList(allRevisionQuery);

        if (revisionEntities != null && revisionEntities.size() > innospotWorkflowProperties.getWorkFlowInstanceKeepVersionAmount()) {
            for (int i = innospotWorkflowProperties.getWorkFlowInstanceKeepVersionAmount(); i < revisionEntities.size(); i++) {
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


}
