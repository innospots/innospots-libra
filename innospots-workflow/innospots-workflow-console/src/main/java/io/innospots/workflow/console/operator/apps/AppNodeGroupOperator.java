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

package io.innospots.workflow.console.operator.apps;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.workflow.console.dao.apps.AppNodeDefinitionDao;
import io.innospots.workflow.console.dao.apps.AppNodeGroupDao;
import io.innospots.workflow.console.dao.apps.AppNodeGroupNodeDao;
import io.innospots.workflow.console.entity.apps.AppNodeDefinitionEntity;
import io.innospots.workflow.console.entity.apps.AppNodeGroupEntity;
import io.innospots.workflow.console.entity.apps.AppNodeGroupNodeEntity;
import io.innospots.workflow.console.mapper.apps.AppNodeDefinitionBeanConverter;
import io.innospots.workflow.console.mapper.apps.AppNodeGroupBeanConverter;
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
public class AppNodeGroupOperator {


    private AppNodeGroupDao appNodeGroupDao;
    private AppNodeGroupNodeDao appNodeGroupNodeDao;
    private AppNodeDefinitionDao appNodeDefinitionDao;

    public AppNodeGroupOperator(AppNodeGroupDao appNodeGroupDao, AppNodeGroupNodeDao appNodeGroupNodeDao,
                                AppNodeDefinitionDao appNodeDefinitionDao) {
        this.appNodeGroupDao = appNodeGroupDao;
        this.appNodeGroupNodeDao = appNodeGroupNodeDao;
        this.appNodeDefinitionDao = appNodeDefinitionDao;
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
        QueryWrapper<AppNodeGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppNodeGroupEntity::getFlowTplId, flowTplId)
                .and(wrapper -> wrapper.eq(AppNodeGroupEntity::getName, name).or().eq(AppNodeGroupEntity::getCode, code));

        long count = appNodeGroupDao.selectCount(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildDuplicateException(this.getClass(), "create node group exits");
        }

        //save
        AppNodeGroupEntity entity = AppNodeGroupEntity.constructor(flowTplId, name, code, position);
        int row = appNodeGroupDao.insert(entity);
        if (row != 1) {
            throw ResourceException.buildCreateException(this.getClass(), "create node group error");
        }
        return AppNodeGroupBeanConverter.INSTANCE.entityToModel(entity);
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
        QueryWrapper<AppNodeGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(AppNodeGroupEntity::getNodeGroupId)
                .eq(AppNodeGroupEntity::getFlowTplId, flowTplId)
                .and(wrapper -> wrapper.eq(StringUtils.isNoneBlank(name), AppNodeGroupEntity::getName, name)
                        .or().eq(StringUtils.isNoneBlank(code), AppNodeGroupEntity::getCode, code));
        List<AppNodeGroupEntity> list = appNodeGroupDao.selectList(queryWrapper);
        if (list != null && (list.size() > 1 || !list.get(0).getNodeGroupId().equals(nodeGroupId))) {
            throw ResourceException.buildDuplicateException(this.getClass(), "update node group exits");
        }
        UpdateWrapper<AppNodeGroupEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(StringUtils.isNoneBlank(name), AppNodeGroupEntity::getName, name)
                .set(StringUtils.isNoneBlank(code), AppNodeGroupEntity::getCode, code)
                .set(AppNodeGroupEntity::getPosition, position)
                .eq(AppNodeGroupEntity::getNodeGroupId, nodeGroupId);

        int row = appNodeGroupDao.update(null, updateWrapper);
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
        int row = appNodeGroupDao.deleteById(nodeGroupId);
        if (row != 1) {
            throw ResourceException.buildDeleteException(this.getClass(), "delete node group error");
        }
        QueryWrapper<AppNodeGroupNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppNodeGroupNodeEntity::getNodeGroupId, nodeGroupId);
        this.appNodeGroupNodeDao.delete(queryWrapper);
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
        List<AppNodeGroupNodeEntity> entityList = this.getGroupNodeByNodeIds(flowTplId, nodeIds);

        List<Integer> exitsIds = new ArrayList<>();
        List<Integer> notExitsIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityList)) {
            for (AppNodeGroupNodeEntity nodeGroupNodeEntity : entityList) {
                if (nodeGroupId.equals(nodeGroupNodeEntity.getNodeGroupId())) {
                    exitsIds.add(nodeGroupNodeEntity.getNodeId());

                } else {
                    notExitsIds.add(nodeGroupNodeEntity.getNodeGroupNodeId());
                }
            }

        }

        if (CollectionUtils.isNotEmpty(notExitsIds)) {
            //delete not exits ids
            appNodeGroupNodeDao.deleteBatchIds(notExitsIds);
        }
        if (CollectionUtils.isNotEmpty(nodeIds)) {
            if (CollectionUtils.isNotEmpty(exitsIds)) {
                nodeIds.removeAll(exitsIds);
            }

            if (CollectionUtils.isNotEmpty(nodeIds)) {
                nodeIds.forEach(id -> {
                    appNodeGroupNodeDao.insert(new AppNodeGroupNodeEntity(flowTplId, nodeGroupId, id));
                });

            }
        }

        return Boolean.TRUE;
    }


    public List<AppNodeGroup> getGroupByFlowTplId(Integer flowTplId, boolean includeNodes) {
        QueryWrapper<AppNodeGroupEntity> ngEntityQuery = new QueryWrapper<>();
        ngEntityQuery.lambda().eq(AppNodeGroupEntity::getFlowTplId, flowTplId).orderByAsc(AppNodeGroupEntity::getPosition);

        List<AppNodeGroupEntity> list = appNodeGroupDao.selectList(ngEntityQuery);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Map<Integer, List<AppNodeDefinition>> ndMap = new HashMap<>();
        if (includeNodes) {
            List<AppNodeDefinitionEntity> ndList = appNodeDefinitionDao.getNodeDefinitionByFlowTplIdAndStatus(flowTplId, DataStatus.ONLINE);
            if (CollectionUtils.isNotEmpty(ndList)) {
                for (AppNodeDefinitionEntity entity : ndList) {
                    if (!ndMap.containsKey(entity.getNodeGroupId())) {
                        ndMap.put(entity.getNodeGroupId(), new ArrayList<>());
                    }
                    ndMap.get(entity.getNodeGroupId()).add(AppNodeDefinitionBeanConverter.INSTANCE.entityToModel(entity));
                }
            }
        }
        List<AppNodeGroup> resultList = new ArrayList<>();
        for (AppNodeGroupEntity nodeGroupEntity : list) {
            AppNodeGroup appNodeGroup = AppNodeGroupBeanConverter.INSTANCE.entityToModel(nodeGroupEntity);
            appNodeGroup.setNodes(ndMap.getOrDefault(appNodeGroup.getNodeGroupId(), new ArrayList<>()));
            resultList.add(appNodeGroup);
        }
        return resultList;
    }

    public List<AppNodeGroupNodeEntity> getGroupNodeByNodeIds(Integer flowTplId, List<Integer> nodeIds) {
        QueryWrapper<AppNodeGroupNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppNodeGroupNodeEntity::getFlowTplId, flowTplId)
                .in(AppNodeGroupNodeEntity::getNodeId, nodeIds);

        return appNodeGroupNodeDao.selectList(queryWrapper);
    }
}
