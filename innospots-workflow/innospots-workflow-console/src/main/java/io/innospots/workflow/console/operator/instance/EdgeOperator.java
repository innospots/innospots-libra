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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ResourceException;
import io.innospots.workflow.console.dao.instance.EdgeInstanceDao;
import io.innospots.workflow.console.entity.instance.EdgeInstanceEntity;
import io.innospots.workflow.console.enums.FlowVersion;
import io.innospots.workflow.console.mapper.instance.EdgeInstanceConvertMapper;
import io.innospots.workflow.core.node.instance.Edge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class EdgeOperator extends ServiceImpl<EdgeInstanceDao, EdgeInstanceEntity> {


    /**
     * 保存修改删除工作流实例边
     *
     * @param workflowInstanceId
     * @param edges
     * @return
     */
    @Transactional(rollbackFor = {Exception.class})
    public boolean saveDraftEdgeInstances(Long workflowInstanceId, List<Edge> edges) {
        //query all edge of this workflow instance that is the draft version
        Map<Long, EdgeInstanceEntity> entityMap = new HashMap<>();
        QueryWrapper<EdgeInstanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EdgeInstanceEntity::getWorkflowInstanceId, workflowInstanceId).eq(EdgeInstanceEntity::getRevision, FlowVersion.DRAFT.getVersion());
        List<EdgeInstanceEntity> entityList = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(entityList)) {
            entityMap = entityList.stream().collect(Collectors.toMap(EdgeInstanceEntity::getEdgeId, entity -> entity));
        }
        //remove edge
        List<Long> deleteIds = null;
        List<EdgeInstanceEntity> newEntityList = new ArrayList<>();
        List<EdgeInstanceEntity> updateEntityList = new ArrayList<>();
        //List
        if (CollectionUtils.isEmpty(edges)) {
            if (CollectionUtils.isNotEmpty(entityList)) {
                deleteIds = new ArrayList<>(entityMap.keySet());
            }
        } else {
            List<EdgeInstanceEntity> requestEntities = EdgeInstanceConvertMapper.INSTANCE.modelToEntityList(edges);
            for (EdgeInstanceEntity newEntity : requestEntities) {
                newEntity.setRevision(FlowVersion.DRAFT.getVersion());
                if (newEntity.getEdgeId() != null && newEntity.getEdgeId() > 0) {
                    entityMap.remove(newEntity.getEdgeId());
                } else {
                    newEntity.setWorkflowInstanceId(workflowInstanceId);
                }
                if (newEntity.getEdgeId() == null) {
                    newEntityList.add(newEntity);
                } else {
                    updateEntityList.add(newEntity);
                }
            }
            if (!entityMap.isEmpty()) {
                deleteIds = new ArrayList<>(entityMap.keySet());
            }
        }
        boolean up = false;
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            up = this.removeByIds(deleteIds);
        }
        if (CollectionUtils.isNotEmpty(newEntityList)) {
            up = this.saveBatch(newEntityList) & up;
        }
        if (CollectionUtils.isNotEmpty(updateEntityList)) {
            up = this.updateBatchById(updateEntityList) & up;
        }
        return up;
    }


    /**
     * @param flowInstanceId
     * @return
     */
    public List<Edge> getEdgeByFlowInstanceId(Long flowInstanceId, Integer revision) {
        QueryWrapper<EdgeInstanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EdgeInstanceEntity::getWorkflowInstanceId, flowInstanceId).eq(EdgeInstanceEntity::getRevision, revision);
        return EdgeInstanceConvertMapper.INSTANCE.entityToModelList(this.list(queryWrapper));
    }

    public Set<String> selectSourceNodeKey(Long flowInstanceId, Integer revision, String targetKey) {
        QueryWrapper<EdgeInstanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(EdgeInstanceEntity::getSource)
                .eq(EdgeInstanceEntity::getWorkflowInstanceId, flowInstanceId)
                .eq(EdgeInstanceEntity::getRevision, revision)
                .eq(EdgeInstanceEntity::getTarget, targetKey);
        Set<String> sourceNodeKeys = null;
        List<EdgeInstanceEntity> edgeEntityList = this.list(queryWrapper);
        if (edgeEntityList != null) {
            sourceNodeKeys = edgeEntityList.stream().map(EdgeInstanceEntity::getSource).collect(Collectors.toSet());
        }
        return sourceNodeKeys;
    }

    /**
     * 发布最新版本
     *
     * @param flowInstanceId
     * @param revision
     * @return 边数量
     */
    @Transactional(rollbackFor = {Exception.class})
    public Integer publishRevision(Long flowInstanceId, Integer revision) {
        QueryWrapper<EdgeInstanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EdgeInstanceEntity::getWorkflowInstanceId, flowInstanceId).eq(EdgeInstanceEntity::getRevision, 0);
        List<EdgeInstanceEntity> entityList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(entityList)) {
            return 0;
        }

        for (EdgeInstanceEntity edgeInstanceEntity : entityList) {
            edgeInstanceEntity.setEdgeId(null);
            edgeInstanceEntity.setCreatedTime(null);
            edgeInstanceEntity.setCreatedBy(null);
            edgeInstanceEntity.setRevision(revision);
        }
        boolean insertState = this.saveBatch(entityList);
        if (!insertState) {
            log.error("publish Revision edge save error:flowInstanceId:{}, revision:{}, edgeSize:{}", flowInstanceId, revision, entityList.size());
            throw ResourceException.buildCreateException(this.getClass(), "publish Revision edge save error");
        }
        return entityList.size();
    }


    public boolean deleteEdges(Long workflowInstanceId) {
        QueryWrapper<EdgeInstanceEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(EdgeInstanceEntity::getWorkflowInstanceId, workflowInstanceId);
        return this.remove(wrapper);
    }

    public boolean deleteByWorkflowInstanceIdAndRevision(Long workflowInstanceId, Integer revision) {
        QueryWrapper<EdgeInstanceEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(EdgeInstanceEntity::getWorkflowInstanceId, workflowInstanceId)
                .eq(EdgeInstanceEntity::getRevision, revision);
        return this.remove(wrapper);
    }


}
