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
import io.innospots.base.enums.DataScope;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.utils.StringConverter;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.innospots.workflow.core.node.definition.converter.FlowNodeDefinitionConverter;
import io.innospots.workflow.core.node.definition.converter.FlowNodeGroupConverter;
import io.innospots.workflow.core.node.definition.dao.FlowNodeDefinitionDao;
import io.innospots.workflow.core.node.definition.dao.FlowNodeGroupDao;
import io.innospots.workflow.core.node.definition.dao.FlowNodeGroupNodeDao;
import io.innospots.workflow.core.node.definition.dao.FlowTemplateDao;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.core.node.definition.entity.FlowNodeGroupEntity;
import io.innospots.workflow.core.node.definition.entity.FlowNodeGroupNodeEntity;
import io.innospots.workflow.core.node.definition.entity.FlowTemplateEntity;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import io.innospots.workflow.core.node.definition.model.NodeGroup;
import io.innospots.workflow.core.node.definition.model.NodeGroupBaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2020/11/28
 */
@Slf4j
public class FlowNodeGroupOperator {


    private final FlowNodeGroupDao flowNodeGroupDao;
    private final FlowNodeGroupNodeDao flowNodeGroupNodeDao;
    private final FlowNodeDefinitionDao flowNodeDefinitionDao;
    private final FlowTemplateDao flowTemplateDao;

    public FlowNodeGroupOperator(FlowTemplateDao flowTemplateDao, FlowNodeGroupDao flowNodeGroupDao, FlowNodeGroupNodeDao flowNodeGroupNodeDao,
                                 FlowNodeDefinitionDao flowNodeDefinitionDao) {
        this.flowTemplateDao = flowTemplateDao;
        this.flowNodeGroupDao = flowNodeGroupDao;
        this.flowNodeGroupNodeDao = flowNodeGroupNodeDao;
        this.flowNodeDefinitionDao = flowNodeDefinitionDao;
    }


    public NodeGroupBaseInfo createNodeGroup(String name, String templateCode) {
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getTplCode, templateCode);
        FlowTemplateEntity templateEntity = flowTemplateDao.selectOne(queryWrapper);
        if (templateEntity == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "flow template note exist");
        }
        String groupCode = StringConverter.randomKey(8);
        NodeGroup nodeGroup = this.createNodeGroup(templateEntity.getFlowTplId(), name, groupCode);
        return FlowNodeGroupConverter.INSTANCE.modelToBase(nodeGroup);
    }

    /**
     * create node group
     *
     * @param flowTplId template id
     * @param name      group name
     * @param code      group code
     * @return NodeGroup
     */
    public NodeGroup createNodeGroup(Integer flowTplId, String name, String code) {
        //check name and code exits
        QueryWrapper<FlowNodeGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowNodeGroupEntity::getFlowTplId, flowTplId)
                .and(wrapper -> wrapper.eq(FlowNodeGroupEntity::getName, name).or().eq(FlowNodeGroupEntity::getCode, code));

        long count = flowNodeGroupDao.selectCount(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildDuplicateException(this.getClass(), "node group name exits");
        }
        QueryWrapper<FlowNodeGroupEntity> maxGroupQuery = new QueryWrapper<>();
        maxGroupQuery.lambda().eq(FlowNodeGroupEntity::getFlowTplId, flowTplId)
                .orderByDesc(FlowNodeGroupEntity::getPosition);
        FlowNodeGroupEntity maxGroup = flowNodeGroupDao.selectOne(maxGroupQuery, false);
        int position = 1;
        if (maxGroup != null) {
            position = maxGroup.getPosition() + 1;
        }
        //save
        FlowNodeGroupEntity entity = FlowNodeGroupEntity.constructor(flowTplId, name, code, position, DataScope.user);
        int row = flowNodeGroupDao.insert(entity);
        if (row != 1) {
            throw ResourceException.buildCreateException(this.getClass(), "create node group error");
        }
        return FlowNodeGroupConverter.INSTANCE.entityToModel(entity);
    }

    public NodeGroup getNodeGroupByCode(String tplFlowCode, String groupCode) {
        FlowTemplateEntity templateEntity = null;
        if (tplFlowCode != null) {
            QueryWrapper<FlowTemplateEntity> ftqw = new QueryWrapper<>();
            ftqw.lambda().eq(FlowTemplateEntity::getTplCode, tplFlowCode);
            templateEntity = flowTemplateDao.selectOne(ftqw);
        }

        QueryWrapper<FlowNodeGroupEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(FlowNodeGroupEntity::getCode, groupCode);
        if (templateEntity != null) {
            qw.lambda().eq(FlowNodeGroupEntity::getFlowTplId, templateEntity.getFlowTplId());
        }
        FlowNodeGroupEntity entity = flowNodeGroupDao.selectOne(qw);
        return FlowNodeGroupConverter.INSTANCE.entityToModel(entity);
    }


    /**
     * modify node group
     *
     * @param nodeGroupId group id
     * @param name        group name
     * @return Boolean
     */
    public Boolean reNameNodeGroup(Integer nodeGroupId, String name) {
        //check name and code exits
        QueryWrapper<FlowNodeGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(FlowNodeGroupEntity::getNodeGroupId)
                .ne(FlowNodeGroupEntity::getNodeGroupId, nodeGroupId)
                .eq(FlowNodeGroupEntity::getName, name);
        long count = flowNodeGroupDao.selectCount(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildDuplicateException(this.getClass(), "node group name exits");
        }
        UpdateWrapper<FlowNodeGroupEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(StringUtils.isNoneBlank(name), FlowNodeGroupEntity::getName, name)
                .eq(FlowNodeGroupEntity::getNodeGroupId, nodeGroupId);

        int row = flowNodeGroupDao.update(null, updateWrapper);
        if (row != 1) {
            throw ResourceException.buildUpdateException(this.getClass(), "update node group error");
        }
        return Boolean.TRUE;
    }

    public List<NodeGroupBaseInfo> swapPosition(Integer fromGroupId, Integer toGroupId) {
        FlowNodeGroupEntity fromGroup = flowNodeGroupDao.selectById(fromGroupId);
        FlowNodeGroupEntity toGroup = flowNodeGroupDao.selectById(toGroupId);
        flowNodeGroupDao.update(new UpdateWrapper<FlowNodeGroupEntity>().lambda()
                .set(FlowNodeGroupEntity::getPosition, toGroup.getPosition())
                .eq(FlowNodeGroupEntity::getNodeGroupId, fromGroup.getNodeGroupId()));

        flowNodeGroupDao.update(new UpdateWrapper<FlowNodeGroupEntity>().lambda()
                .set(FlowNodeGroupEntity::getPosition, fromGroup.getPosition())
                .eq(FlowNodeGroupEntity::getNodeGroupId, toGroup.getNodeGroupId()));
        return FlowNodeGroupConverter.INSTANCE.modelToBaseList(
                getGroupByFlowTplId(fromGroup.getFlowTplId(), false, false));
    }

    /**
     * remove node group
     *
     * @param nodeGroupId group id
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeNodeGroup(Integer nodeGroupId) {
        FlowNodeGroupEntity nodeGroupEntity = flowNodeGroupDao.selectById(nodeGroupId);
        if (nodeGroupEntity == null) {
            throw ResourceException.buildDeleteException(this.getClass(), "node group not exist");
        }
        //can't delete system scope
        if (Objects.equals(DataScope.system.name(), nodeGroupEntity.getScopes())) {
            return false;
        }
        int row = flowNodeGroupDao.deleteById(nodeGroupId);
        if (row != 1) {
            throw ResourceException.buildDeleteException(this.getClass(), "delete node group error");
        }
        UpdateWrapper<FlowNodeGroupNodeEntity> queryWrapper = new UpdateWrapper<>();
        queryWrapper.lambda()
                .set(FlowNodeGroupNodeEntity::getNodeGroupNodeId, -1)
                .eq(FlowNodeGroupNodeEntity::getNodeGroupId, nodeGroupId);
        this.flowNodeGroupNodeDao.delete(queryWrapper);
        /*
        QueryWrapper<FlowNodeGroupNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowNodeGroupNodeEntity::getNodeGroupId, nodeGroupId);
        this.flowNodeGroupNodeDao.delete(queryWrapper);
         */
        return Boolean.TRUE;
    }

    public boolean removeNodeGroupByNodeId(Integer nodeId) {
        QueryWrapper<FlowNodeGroupNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowNodeGroupNodeEntity::getNodeId, nodeId);
        int count = this.flowNodeGroupNodeDao.delete(queryWrapper);
        return count > 0;
    }

    /**
     * save or modify NodeGroup and Node relation
     *
     * @param nodeGroupId
     * @param nodeIds     node ids type is list for example:[1,3,2]
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateNodeGroupNode(Integer nodeGroupId, List<Integer> nodeIds) {
        FlowNodeGroupEntity groupEntity = flowNodeGroupDao.selectById(nodeGroupId);
        if (groupEntity == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "node group not exist", nodeGroupId);
        }
        List<FlowNodeGroupNodeEntity> entityList = this.getGroupNodeByNodeIds(groupEntity.getFlowTplId(), nodeIds);

        List<Integer> exitsNodeIds = new ArrayList<>();
        List<Integer> notExitsGroupIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityList)) {
            for (FlowNodeGroupNodeEntity nodeGroupNodeEntity : entityList) {
                if (nodeGroupId.equals(nodeGroupNodeEntity.getNodeGroupId())) {
                    exitsNodeIds.add(nodeGroupNodeEntity.getNodeId());
                } else {
                    notExitsGroupIds.add(nodeGroupNodeEntity.getNodeGroupNodeId());
                }
            }

        }

        if (CollectionUtils.isNotEmpty(notExitsGroupIds)) {
            //delete not exits ids
            flowNodeGroupNodeDao.deleteBatchIds(notExitsGroupIds);
        }
        if (CollectionUtils.isNotEmpty(nodeIds)) {
            if (CollectionUtils.isNotEmpty(exitsNodeIds)) {
                nodeIds.removeAll(exitsNodeIds);
            }

            if (CollectionUtils.isNotEmpty(nodeIds)) {
                nodeIds.forEach(id -> {
                    flowNodeGroupNodeDao.insert(new FlowNodeGroupNodeEntity(groupEntity.getFlowTplId(), nodeGroupId, id));
                });

            }
        }

        return Boolean.TRUE;
    }

    public List<NodeGroup> getGroupByFlowTplCode(String flowTplCode, boolean includeNodes, boolean excludeTrigger, boolean includeAll) {
        if (flowTplCode == null) {
            throw ValidatorException.buildMissingException(this.getClass(), "flow template code is null");
        }
        FlowTemplateEntity templateEntity = flowTemplateDao.selectOne(new QueryWrapper<FlowTemplateEntity>().lambda().eq(FlowTemplateEntity::getTplCode, flowTplCode));
        return fillNodeGroup(flowTplCode, templateEntity.getFlowTplId(), includeNodes, excludeTrigger, includeAll);
    }


    public List<NodeGroup> getGroupByFlowTplId(Integer flowTplId, boolean includeNodes, boolean excludeTrigger) {
        FlowTemplateEntity templateEntity = flowTemplateDao.selectById(flowTplId);
        return fillNodeGroup(templateEntity.getTplCode(), flowTplId, includeNodes, excludeTrigger, true);
    }

    private List<NodeGroup> fillNodeGroup(String tplCode, Integer flowTplId, boolean includeNodes, boolean excludeTrigger, boolean includeAll) {
        QueryWrapper<FlowNodeGroupEntity> ngEntityQuery = new QueryWrapper<>();
        ngEntityQuery.lambda().eq(FlowNodeGroupEntity::getFlowTplId, flowTplId).orderByAsc(FlowNodeGroupEntity::getPosition)
                .orderByDesc(FlowNodeGroupEntity::getUpdatedTime);
        List<FlowNodeGroupEntity> list = flowNodeGroupDao.selectList(ngEntityQuery);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Map<Integer, List<NodeDefinition>> ndMap = new HashMap<>();
        List<NodeDefinition> allNodes = new ArrayList<>();
        if (includeNodes) {
            QueryWrapper<FlowNodeDefinitionEntity> nodeQuery = new QueryWrapper<>();
            nodeQuery.lambda().eq(FlowNodeDefinitionEntity::getFlowCode, tplCode);
            List<FlowNodeDefinitionEntity> ndList = this.flowNodeDefinitionDao.selectList(nodeQuery);
            if (CollectionUtils.isNotEmpty(ndList)) {
                Map<Integer, NodeDefinition> nodeDefinitionMap = ndList.stream()
                        .map(FlowNodeDefinitionConverter.INSTANCE::entityToModel)
                        .collect(Collectors.toMap(NodeDefinition::getNodeId, Function.identity()));
                if (excludeTrigger) {
                    allNodes.addAll(nodeDefinitionMap.values().stream().filter(n -> n.getPrimitive() != NodePrimitive.trigger).collect(Collectors.toList()));
                } else {
                    allNodes.addAll(nodeDefinitionMap.values());
                }

                QueryWrapper<FlowNodeGroupNodeEntity> gnq = new QueryWrapper<>();
                gnq.lambda().eq(FlowNodeGroupNodeEntity::getFlowTplId, flowTplId);
                List<FlowNodeGroupNodeEntity> groupNodeList = flowNodeGroupNodeDao.selectList(gnq);
                for (FlowNodeGroupNodeEntity groupNode : groupNodeList) {
                    List<NodeDefinition> nodeDefinitions = ndMap.computeIfAbsent(groupNode.getNodeGroupId(), k -> new ArrayList<>());
                    NodeDefinition node = nodeDefinitionMap.get(groupNode.getNodeId());
                    if (node != null) {
                        nodeDefinitions.add(node);
                    } else {
                        log.warn("node definition has been removed, but node group have this relation record, nodeId:{}, groupId:{}, primaryId:{}",
                                groupNode.getNodeId(), groupNode.getNodeGroupId(), groupNode.getNodeGroupNodeId());
                    }
                }//end for group
            }//end for node definition list
        }//end include

        List<NodeGroup> resultList = new ArrayList<>();
        for (FlowNodeGroupEntity nodeGroupEntity : list) {
            NodeGroup nodeGroup = FlowNodeGroupConverter.INSTANCE.entityToModel(nodeGroupEntity);
            nodeGroup.setDeletable(!Objects.equals(DataScope.system.name(), nodeGroupEntity.getScopes()));
            if (excludeTrigger && "trigger".equals(nodeGroup.getCode())) {
                nodeGroup.setHidden(true);
            }
            if ("all".equalsIgnoreCase(nodeGroup.getCode())) {
                if (includeAll) {
                    nodeGroup.setNodes(allNodes);
                    resultList.add(nodeGroup);
                }
                nodeGroup.setHidden(!includeAll);
            } else {
                nodeGroup.setNodes(ndMap.getOrDefault(nodeGroup.getNodeGroupId(), new ArrayList<>()));
                resultList.add(nodeGroup);
            }
        }//end for node group
        return resultList;
    }

    public List<FlowNodeGroupNodeEntity> getGroupNodeByNodeIds(Integer flowTplId, List<Integer> nodeIds) {
        QueryWrapper<FlowNodeGroupNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowNodeGroupNodeEntity::getFlowTplId, flowTplId)
                .in(FlowNodeGroupNodeEntity::getNodeId, nodeIds);

        return flowNodeGroupNodeDao.selectList(queryWrapper);
    }
}
