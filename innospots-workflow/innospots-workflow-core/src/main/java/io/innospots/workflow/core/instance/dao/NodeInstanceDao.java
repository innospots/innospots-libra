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

package io.innospots.workflow.core.instance.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.enums.FlowVersion;
import io.innospots.workflow.core.instance.entity.NodeInstanceEntity;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2021/1/16
 */
public interface NodeInstanceDao extends BaseMapper<NodeInstanceEntity> {



    /**
     * saveNode
     *
     * @param workflowInstanceId
     * @param nodeInstances
     * @return
     */
    default boolean saveDraftNodeInstances(Long workflowInstanceId, List<NodeInstance> nodeInstances) {
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

        /*
        List<Integer> nodeIds = entityList.stream().map(NodeInstanceEntity::getNodeDefinitionId).distinct().collect(Collectors.toList());
        Map<Boolean, List<Integer>> nodeIdMap = this.countByNodeIds(nodeIds);
        if (MapUtils.isNotEmpty(nodeIdMap)) {
            for (Map.Entry<Boolean, List<Integer>> map : nodeIdMap.entrySet()) {
                appNodeDefinitionOperator.updateAppUsed(map.getValue(), map.getKey());
            }
        }
         */
        return true;
    }

}
