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

package io.innospots.workflow.console.operator.node;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.workflow.console.converter.node.FlowNodeGroupConverter;
import io.innospots.workflow.console.converter.node.FlowNodeDefinitionConverter;
import io.innospots.workflow.console.dao.node.FlowNodeDefinitionDao;
import io.innospots.workflow.console.dao.node.FlowNodeGroupDao;
import io.innospots.workflow.console.dao.node.FlowNodeGroupNodeDao;
import io.innospots.workflow.console.entity.node.FlowNodeDefinitionEntity;
import io.innospots.workflow.console.entity.node.FlowNodeGroupEntity;
import io.innospots.workflow.console.entity.node.FlowNodeGroupNodeEntity;
import io.innospots.workflow.core.node.apps.AppNodeDefinition;
import io.innospots.workflow.core.node.apps.AppNodeGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Raydian
 * @date 2020/11/28
 */
public class FlowNodeGroupOperator {


    private FlowNodeGroupDao flowNodeGroupDao;
    private FlowNodeGroupNodeDao flowNodeGroupNodeDao;
    private FlowNodeDefinitionDao flowNodeDefinitionDao;

    public FlowNodeGroupOperator(FlowNodeGroupDao flowNodeGroupDao, FlowNodeGroupNodeDao flowNodeGroupNodeDao,
                                 FlowNodeDefinitionDao flowNodeDefinitionDao) {
        this.flowNodeGroupDao = flowNodeGroupDao;
        this.flowNodeGroupNodeDao = flowNodeGroupNodeDao;
        this.flowNodeDefinitionDao = flowNodeDefinitionDao;
    }

    /**
     * create node group
     *
     * @param flowTplId template id
     * @param name      group name
     * @param code      group code
     * @param position
     * @return NodeGroup
     */
    public AppNodeGroup createNodeGroup(Integer flowTplId, String name, String code, Integer position) {
        //check name and code exits
        QueryWrapper<FlowNodeGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowNodeGroupEntity::getFlowTplId, flowTplId)
                .and(wrapper -> wrapper.eq(FlowNodeGroupEntity::getName, name).or().eq(FlowNodeGroupEntity::getCode, code));

        long count = flowNodeGroupDao.selectCount(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildDuplicateException(this.getClass(), "create node group exits");
        }

        //save
        FlowNodeGroupEntity entity = FlowNodeGroupEntity.constructor(flowTplId, name, code, position);
        int row = flowNodeGroupDao.insert(entity);
        if (row != 1) {
            throw ResourceException.buildCreateException(this.getClass(), "create node group error");
        }
        return FlowNodeGroupConverter.INSTANCE.entityToModel(entity);
    }


    /**
     * modify node group
     *
     * @param flowTplId   template id
     * @param nodeGroupId group id
     * @param name        group name
     * @param code        group code
     * @param position    group position
     * @return Boolean
     */
    public Boolean updateNodeGroup(Integer flowTplId, Integer nodeGroupId, String name, String code, Integer position) {
        //check name and code exits
        QueryWrapper<FlowNodeGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(FlowNodeGroupEntity::getNodeGroupId)
                .eq(FlowNodeGroupEntity::getFlowTplId, flowTplId)
                .and(wrapper -> wrapper.eq(StringUtils.isNoneBlank(name), FlowNodeGroupEntity::getName, name)
                        .or().eq(StringUtils.isNoneBlank(code), FlowNodeGroupEntity::getCode, code));
        List<FlowNodeGroupEntity> list = flowNodeGroupDao.selectList(queryWrapper);
        if (list != null && (list.size() > 1 || !list.get(0).getNodeGroupId().equals(nodeGroupId))) {
            throw ResourceException.buildDuplicateException(this.getClass(), "update node group exits");
        }
        UpdateWrapper<FlowNodeGroupEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(StringUtils.isNoneBlank(name), FlowNodeGroupEntity::getName, name)
                .set(StringUtils.isNoneBlank(code), FlowNodeGroupEntity::getCode, code)
                .set(FlowNodeGroupEntity::getPosition, position)
                .eq(FlowNodeGroupEntity::getNodeGroupId, nodeGroupId);

        int row = flowNodeGroupDao.update(null, updateWrapper);
        if (row != 1) {
            throw ResourceException.buildUpdateException(this.getClass(), "update node group error");
        }
        return Boolean.TRUE;
    }

    /**
     * remove node group
     *
     * @param nodeGroupId group id
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeNodeGroup(Integer nodeGroupId) {
        int row = flowNodeGroupDao.deleteById(nodeGroupId);
        if (row != 1) {
            throw ResourceException.buildDeleteException(this.getClass(), "delete node group error");
        }
        QueryWrapper<FlowNodeGroupNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowNodeGroupNodeEntity::getNodeGroupId, nodeGroupId);
        this.flowNodeGroupNodeDao.delete(queryWrapper);
        return Boolean.TRUE;
    }

    /**
     * save or modify NodeGroup and Node relation
     *
     * @param flowTplId
     * @param nodeGroupId
     * @param nodeIds     node ids type is list for example:[1,3,2]
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateNodeGroupNode(Integer flowTplId, Integer nodeGroupId, List<Integer> nodeIds) {
        List<FlowNodeGroupNodeEntity> entityList = this.getGroupNodeByNodeIds(flowTplId, nodeIds);

        List<Integer> exitsIds = new ArrayList<>();
        List<Integer> notExitsIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityList)) {
            for (FlowNodeGroupNodeEntity nodeGroupNodeEntity : entityList) {
                if (nodeGroupId.equals(nodeGroupNodeEntity.getNodeGroupId())) {
                    exitsIds.add(nodeGroupNodeEntity.getNodeId());

                } else {
                    notExitsIds.add(nodeGroupNodeEntity.getNodeGroupNodeId());
                }
            }

        }

        if (CollectionUtils.isNotEmpty(notExitsIds)) {
            //delete not exits ids
            flowNodeGroupNodeDao.deleteBatchIds(notExitsIds);
        }
        if (CollectionUtils.isNotEmpty(nodeIds)) {
            if (CollectionUtils.isNotEmpty(exitsIds)) {
                nodeIds.removeAll(exitsIds);
            }

            if (CollectionUtils.isNotEmpty(nodeIds)) {
                nodeIds.forEach(id -> {
                    flowNodeGroupNodeDao.insert(new FlowNodeGroupNodeEntity(flowTplId, nodeGroupId, id));
                });

            }
        }

        return Boolean.TRUE;
    }


    public List<AppNodeGroup> getGroupByFlowTplId(Integer flowTplId, boolean includeNodes) {
        QueryWrapper<FlowNodeGroupEntity> ngEntityQuery = new QueryWrapper<>();
        ngEntityQuery.lambda().eq(FlowNodeGroupEntity::getFlowTplId, flowTplId).orderByAsc(FlowNodeGroupEntity::getPosition);

        List<FlowNodeGroupEntity> list = flowNodeGroupDao.selectList(ngEntityQuery);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Map<Integer, List<AppNodeDefinition>> ndMap = new HashMap<>();
        if (includeNodes) {
            List<FlowNodeDefinitionEntity> ndList = flowNodeDefinitionDao.getNodeDefinitionByFlowTplIdAndStatus(flowTplId, DataStatus.ONLINE);
            if (CollectionUtils.isNotEmpty(ndList)) {
                for (FlowNodeDefinitionEntity entity : ndList) {
                    if (!ndMap.containsKey(entity.getNodeGroupId())) {
                        ndMap.put(entity.getNodeGroupId(), new ArrayList<>());
                    }
                    ndMap.get(entity.getNodeGroupId()).add(FlowNodeDefinitionConverter.INSTANCE.entityToModel(entity));
                }
            }
        }
        List<AppNodeGroup> resultList = new ArrayList<>();
        for (FlowNodeGroupEntity nodeGroupEntity : list) {
            AppNodeGroup appNodeGroup = FlowNodeGroupConverter.INSTANCE.entityToModel(nodeGroupEntity);
            appNodeGroup.setNodes(ndMap.getOrDefault(appNodeGroup.getNodeGroupId(), new ArrayList<>()));
            resultList.add(appNodeGroup);
        }
        return resultList;
    }

    public List<FlowNodeGroupNodeEntity> getGroupNodeByNodeIds(Integer flowTplId, List<Integer> nodeIds) {
        QueryWrapper<FlowNodeGroupNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowNodeGroupNodeEntity::getFlowTplId, flowTplId)
                .in(FlowNodeGroupNodeEntity::getNodeId, nodeIds);

        return flowNodeGroupNodeDao.selectList(queryWrapper);
    }
}
