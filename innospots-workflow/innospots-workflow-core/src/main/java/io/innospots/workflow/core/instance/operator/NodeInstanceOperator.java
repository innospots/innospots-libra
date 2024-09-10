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


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.enums.FlowVersion;
import io.innospots.workflow.core.instance.converter.NodeInstanceConverter;
import io.innospots.workflow.core.instance.dao.NodeInstanceDao;
import io.innospots.workflow.core.instance.entity.NodeInstanceEntity;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.definition.dao.FlowNodeDefinitionDao;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import io.innospots.workflow.core.node.executor.TriggerNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class NodeInstanceOperator extends ServiceImpl<NodeInstanceDao, NodeInstanceEntity> {


    private final FlowNodeDefinitionDao flowNodeDefinitionDao;

    public NodeInstanceOperator(FlowNodeDefinitionDao flowNodeDefinitionDao) {
        this.flowNodeDefinitionDao = flowNodeDefinitionDao;
    }

    public boolean deleteNodes(Long workflowInstanceId) {
        QueryWrapper<NodeInstanceEntity> query = new QueryWrapper<>();
        query.lambda().eq(NodeInstanceEntity::getWorkflowInstanceId, workflowInstanceId);
        return this.remove(query);
    }

    /**
     * saveNode
     *
     * @param workflowInstanceId
     * @param nodeInstances
     * @return
     */
    @Transactional(rollbackFor = {Exception.class})
    public boolean saveDraftNodeInstances(Long workflowInstanceId, List<NodeInstance> nodeInstances) {
        //获取工作流实例草稿版本的所有节点实例
        Map<Long, NodeInstanceEntity> entityMap = new HashMap<>();
        QueryWrapper<NodeInstanceEntity> nodeQuery = new QueryWrapper<>();
        nodeQuery.lambda().eq(NodeInstanceEntity::getWorkflowInstanceId, workflowInstanceId).eq(NodeInstanceEntity::getRevision, FlowVersion.DRAFT.getVersion());
        List<NodeInstanceEntity> entityList = this.list(nodeQuery);
//        List<NodeInstanceEntity> entityList = this.getBaseMapper().getByWorkFlowInstanceIdAndRevision(workflowInstanceId, FlowVersion.DRAFT.getVersion());
        if (CollectionUtils.isNotEmpty(entityList)) {
            entityMap = entityList.stream().collect(Collectors.toMap(NodeInstanceEntity::getNodeInstanceId, entity -> entity));
        }
        //找到要删除的实例ID
        List<Long> deleteIds = null;
        List<NodeInstanceEntity> newEntityList = new ArrayList<>();
        List<NodeInstanceEntity> updateEntityList = new ArrayList<>();
        if (CollectionUtils.isEmpty(nodeInstances)) {
            if (CollectionUtils.isNotEmpty(entityList)) {
                deleteIds = new ArrayList<>(entityMap.keySet());
            }
        } else {
              List<NodeInstanceEntity> requestEntities = parseEntities(nodeInstances);
            for (NodeInstanceEntity newEntity : requestEntities) {
                newEntity.setRevision(FlowVersion.DRAFT.getVersion());
                newEntity.setProjectId(CCH.projectId());
                if (newEntity.getNodeInstanceId() != null && newEntity.getNodeInstanceId() > 0) {
                    entityMap.remove(newEntity.getNodeInstanceId());
                } else {
                    newEntity.setWorkflowInstanceId(workflowInstanceId);
                }
                if (newEntity.getNodeInstanceId() == null) {
                    newEntityList.add(newEntity);
                } else {
                    updateEntityList.add(newEntity);
                }
            }//end for
            if (!entityMap.isEmpty()) {
                deleteIds = new ArrayList<>(entityMap.keySet());
            }
        }
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        if (CollectionUtils.isNotEmpty(newEntityList)) {
            this.saveBatch(newEntityList);
        }
        if (CollectionUtils.isNotEmpty(updateEntityList)) {
            this.updateBatchById(updateEntityList);
        }

        return true;
    }

    public List<NodeInstance> listNodeInstances(List<Long> workflowInstanceIds,List<Integer> nodeDefinitionIds){
        QueryWrapper<NodeInstanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(NodeInstanceEntity::getWorkflowInstanceId,workflowInstanceIds)
                .in(NodeInstanceEntity::getNodeDefinitionId,nodeDefinitionIds);
        return NodeInstanceConverter.INSTANCE.entitiesToModels(this.list(queryWrapper));
    }

    public void filledNodeInstanceByExecutor(List<NodeInstance> nodeInstances) {
        if(CollectionUtils.isEmpty(nodeInstances)){
            return;
        }
        for (NodeInstance nodeInstance : nodeInstances) {
            try {
                BaseNodeExecutor nodeExecutor = NodeExecutorFactory.newInstance(nodeInstance);
                if(nodeExecutor instanceof TriggerNode){
                    // build input fields to instance
                    nodeExecutor.build();
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                     InvocationTargetException e) {
            }
        }
    }

    private List<NodeInstanceEntity> parseEntities(List<NodeInstance> nodeInstances) {
        filledNodeInstanceByExecutor(nodeInstances);
        List<NodeInstanceEntity> list = new ArrayList<>();
        if (nodeInstances != null) {
            for (NodeInstance nodeInstance : nodeInstances) {
                NodeInstanceEntity instanceEntity = NodeInstanceConverter.INSTANCE.modelToEntity(nodeInstance);
                list.add(instanceEntity);
            }
        }
        return list;
    }

    /**
     * 转换NodeInstance对象
     *
     * @param nodeInstanceEntities
     * @return
     */
    private List<NodeInstance> parseNodes(List<NodeInstanceEntity> nodeInstanceEntities) {
        List<NodeInstance> list = new ArrayList<>();
        if (nodeInstanceEntities != null) {

            for (NodeInstanceEntity nodeInstanceEntity : nodeInstanceEntities) {
                try {
                    FlowNodeDefinitionEntity nodeDefinitionEntity = flowNodeDefinitionDao.selectById(nodeInstanceEntity.getNodeDefinitionId());
                    if (nodeDefinitionEntity == null) {
                        log.warn("node definition not exist,{} ", nodeInstanceEntity.getNodeInstanceId());
                        continue;
                    }
                    list.add(NodeInstanceConverter.INSTANCE.entityToModel(nodeInstanceEntity, nodeDefinitionEntity));
//                    NodeDefinition appNodeDefinition = flowNodeDefinitionOperator.getNodeDefinition(nodeInstanceEntity.getNodeDefinitionId());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw ValidatorException.buildInvalidException(this.getClass(), e);
                }

            }
        }
        return list;
    }

    public List<NodeInstanceEntity> getInstanceNodeByFlowInstanceId(Long flowInstanceId) {
        QueryWrapper<NodeInstanceEntity> query = new QueryWrapper<>();
        query.lambda().eq(NodeInstanceEntity::getWorkflowInstanceId, flowInstanceId);
        return this.list(query);
    }

    /**
     * @param flowInstanceId 实例ID
     * @param revision       版本
     * @return
     */
    public List<NodeInstance> getNodeInstanceByFlowInstanceId(Long flowInstanceId, Integer revision) {
        QueryWrapper<NodeInstanceEntity> query = new QueryWrapper<>();
        query.lambda().eq(NodeInstanceEntity::getWorkflowInstanceId, flowInstanceId).eq(NodeInstanceEntity::getRevision, revision);
        List<NodeInstanceEntity> entityList = this.list(query);
        return parseNodes(entityList);
    }

    public long countByNodeDefinitionId(Integer nodeDefinitionId) {
        QueryWrapper<NodeInstanceEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<NodeInstanceEntity> lambda = queryWrapper.lambda();
        lambda.eq(NodeInstanceEntity::getNodeDefinitionId, nodeDefinitionId);
        return super.count(queryWrapper);
    }

    /*
    public Map<Boolean, List<Integer>> countByNodeIds(List<Integer> nodeIds) {
        if (CollectionUtils.isEmpty(nodeIds)) {
            return null;
        }
        List<Map<String, Object>> nodeList = super.listMaps(
                new QueryWrapper<NodeInstanceEntity>()
                        .select("NODE_DEFINITION_ID, COUNT(1) CNT ")
                        .in("NODE_DEFINITION_ID", nodeIds)
                        .groupBy("NODE_DEFINITION_ID"));
        Map<Integer, Integer> nodeMap = nodeList.stream().collect(
                Collectors.toMap(
                        k -> Integer.valueOf(k.get("NODE_DEFINITION_ID").toString()),
                        v -> Integer.valueOf(v.get("CNT").toString()))
        );
        Map<Boolean, List<Integer>> map = new HashMap<>();
        List<Integer> nodeUsed = new ArrayList<>();
        List<Integer> nodeUnUsed = new ArrayList<>();
        for (Integer nodeId : nodeIds) {
            if (MapUtils.isNotEmpty(nodeMap) && nodeMap.get(nodeId) > 0) {
                nodeUsed.add(nodeId);

            } else {
                nodeUnUsed.add(nodeId);
            }
        }
        if(!nodeUsed.isEmpty()){
            map.put(Boolean.TRUE, nodeUsed);
        }
        if(!nodeUnUsed.isEmpty()){
            map.put(Boolean.FALSE, nodeUnUsed);
        }

        return map;
    }
     */

    public List<NodeInstance> listNodeInstancesByNodeKeys(Long flowInstanceId, Integer revision, Set<String> nodeKeys) {
        QueryWrapper<NodeInstanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NodeInstanceEntity::getWorkflowInstanceId, flowInstanceId)
                .eq(NodeInstanceEntity::getRevision, revision)
                .in(NodeInstanceEntity::getNodeKey, nodeKeys);
        List<NodeInstanceEntity> entityList = this.list(queryWrapper);
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream().map(NodeInstanceConverter.INSTANCE::entityToModel).collect(Collectors.toList());
    }

    /**
     * 发布最新版本
     *
     * @param flowInstanceId
     * @param revision
     * @return 节点数量
     */
    @Transactional(rollbackFor = {Exception.class})
    public Integer publishRevision(Long flowInstanceId, Integer revision) {
        QueryWrapper<NodeInstanceEntity> query = new QueryWrapper<>();
        query.lambda().eq(NodeInstanceEntity::getWorkflowInstanceId, flowInstanceId).eq(NodeInstanceEntity::getRevision, 0);
        List<NodeInstanceEntity> entityList = this.list(query);
        if (CollectionUtils.isEmpty(entityList)) {
            return 0;
        }

        for (NodeInstanceEntity nodeInstanceEntity : entityList) {
            nodeInstanceEntity.setNodeInstanceId(null);
            nodeInstanceEntity.setCreatedTime(null);
            nodeInstanceEntity.setCreatedBy(null);
            nodeInstanceEntity.setRevision(revision);
        }
        boolean insertState = this.saveBatch(entityList);
        if (!insertState) {
            log.error("publish Revision node save error:flowInstanceId:{}, revision:{}, nodeSize:{}", flowInstanceId, revision, entityList.size());
            throw ResourceException.buildCreateException(this.getClass(), "publish Revision node save error");
        }
        return entityList.size();
    }


    public boolean deleteByWorkflowInstanceIdAndRevision(Long workflowInstanceId, Integer revision) {
        QueryWrapper<NodeInstanceEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(NodeInstanceEntity::getWorkflowInstanceId, workflowInstanceId)
                .eq(NodeInstanceEntity::getRevision, revision);
        return this.remove(wrapper);
    }


}
