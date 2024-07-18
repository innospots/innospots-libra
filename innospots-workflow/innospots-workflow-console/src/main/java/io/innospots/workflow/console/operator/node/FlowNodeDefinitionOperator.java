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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.enums.ImageType;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.exception.ValidatorException;
import io.innospots.libra.base.events.AvatarRemoveEvent;
import io.innospots.workflow.core.node.NodeInfo;
import io.innospots.workflow.core.node.builder.NodeDefinitionCodeBuilder;
import io.innospots.workflow.core.node.definition.converter.FlowNodeDefinitionConverter;
import io.innospots.workflow.core.node.definition.dao.FlowNodeDefinitionDao;
import io.innospots.workflow.core.node.definition.dao.FlowNodeGroupDao;
import io.innospots.workflow.core.node.definition.dao.FlowNodeGroupNodeDao;
import io.innospots.workflow.core.node.definition.dao.FlowTemplateDao;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.console.model.NodeQueryRequest;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.innospots.workflow.core.node.definition.entity.FlowNodeGroupEntity;
import io.innospots.workflow.core.node.definition.entity.FlowNodeGroupNodeEntity;
import io.innospots.workflow.core.node.definition.entity.FlowTemplateEntity;
import io.innospots.workflow.core.node.definition.meta.NodeDefinitionBuilder;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2020/11/28
 */
public class FlowNodeDefinitionOperator extends ServiceImpl<FlowNodeDefinitionDao, FlowNodeDefinitionEntity> {


    public static final String IMAGE_PREFIX = "data:image";
    public static final String CACHE_NAME = "CACHE_NODE_DEFINITION";

    private final FlowTemplateDao flowTemplateDao;
    private final FlowNodeGroupDao flowNodeGroupDao;
    private final FlowNodeGroupNodeDao flowNodeGroupNodeDao;

    public FlowNodeDefinitionOperator(FlowTemplateDao flowTemplateDao,
                                      FlowNodeGroupDao flowNodeGroupDao,
                                      FlowNodeGroupNodeDao flowNodeGroupNodeDao) {
        this.flowNodeGroupDao = flowNodeGroupDao;
        this.flowTemplateDao = flowTemplateDao;
        this.flowNodeGroupNodeDao = flowNodeGroupNodeDao;
    }

    /**
     * node definition page info
     *
     * @param queryRequest
     * @return Page<NodeDefinition>
     */
    public PageBody<NodeInfo> pageNodeDefinitions(NodeQueryRequest queryRequest) {
        PageBody<NodeInfo> result = new PageBody<>();
        if (queryRequest.getFlowTplId() == null) {
            throw ValidatorException.buildMissingException(this.getClass(), "flowTplId is null");
        }
        if (queryRequest.getNodeGroupId() != null) {
            QueryWrapper<FlowNodeGroupEntity> gQuery = new QueryWrapper<>();
            gQuery.lambda().eq(queryRequest.getFlowTplId() != null, FlowNodeGroupEntity::getFlowTplId, queryRequest.getFlowTplId())
                    .eq(queryRequest.getNodeGroupId() != null, FlowNodeGroupEntity::getNodeGroupId, queryRequest.getNodeGroupId());
            FlowNodeGroupEntity nodeGroup = this.flowNodeGroupDao.selectOne(gQuery);
            if (nodeGroup == null) {
                throw ResourceException.buildNotExistException(this.getClass(), "node group not found", queryRequest.getNodeGroupId());
            }
            if ("all".equalsIgnoreCase(nodeGroup.getCode())) {
                queryRequest.setNodeGroupId(null);
            }
        }


        QueryWrapper<FlowNodeGroupNodeEntity> groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.lambda()
                .eq(queryRequest.getNodeGroupId() != null, FlowNodeGroupNodeEntity::getNodeGroupId, queryRequest.getNodeGroupId())
                .eq(queryRequest.getFlowTplId() != null, FlowNodeGroupNodeEntity::getFlowTplId, queryRequest.getFlowTplId());

        List<FlowNodeGroupNodeEntity> flowNodeGroupEntities = flowNodeGroupNodeDao.selectList(groupQueryWrapper);
        QueryWrapper<FlowNodeDefinitionEntity> nodeQueryWrapper = new QueryWrapper<>();
        if (queryRequest.getNodeGroupId() == null) {
            FlowTemplateEntity flowTemplate = flowTemplateDao.selectById(queryRequest.getFlowTplId());
            if (flowTemplate == null) {
                throw ResourceException.buildNotExistException(this.getClass(), "flow template not found", queryRequest.getFlowTplId());
            }
            nodeQueryWrapper.lambda().eq(FlowNodeDefinitionEntity::getFlowCode, flowTemplate.getTplCode());
        } else if (CollectionUtils.isEmpty(flowNodeGroupEntities)) {
            result.setPageSize((long) queryRequest.getSize());
            result.setTotal(0L);
            result.setTotalPage(0L);
            result.setCurrent((long) queryRequest.getPage());
            result.setMessage("empty");
            result.setList(Collections.emptyList());
            return result;
        } else {
            Set<Integer> nodeIds = flowNodeGroupEntities.stream()
                    .map(FlowNodeGroupNodeEntity::getNodeId).collect(Collectors.toSet());

            nodeQueryWrapper.lambda().in(FlowNodeDefinitionEntity::getNodeId, nodeIds);
        }

        nodeQueryWrapper.lambda().eq(queryRequest.getDataStatus() != null, FlowNodeDefinitionEntity::getStatus, queryRequest.getDataStatus())
                .like(StringUtils.isNotBlank(queryRequest.getQueryInput()), FlowNodeDefinitionEntity::getName, queryRequest.getQueryInput())
                .orderByDesc(FlowNodeDefinitionEntity::getUpdatedTime);

        IPage<FlowNodeDefinitionEntity> queryPage = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        queryPage = this.page(queryPage, nodeQueryWrapper);


        if (queryPage != null) {
            result.setTotal(queryPage.getTotal());
            result.setTotalPage(queryPage.getPages());
            result.setCurrent(queryPage.getCurrent());
            result.setPageSize(queryPage.getSize());
            List<NodeInfo> nodeInfos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(queryPage.getRecords())) {
                List<FlowNodeDefinitionEntity> entities = queryPage.getRecords();
                for (FlowNodeDefinitionEntity entity : entities) {
                    if (entity.getUsed() == null) {
                        entity.setUsed(Boolean.TRUE);
                    }
                    NodeInfo nodeInfo = FlowNodeDefinitionConverter.INSTANCE.entityToSimple(entity);
                    nodeInfos.add(nodeInfo);
                }


            }
            result.setList(nodeInfos);
        }
        return result;
    }

    public List<NodeDefinition> listOnlineNodes(NodePrimitive primitive, String flowCode) {
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda()
                .eq(FlowNodeDefinitionEntity::getStatus, DataStatus.ONLINE);
        if (primitive != null) {
            queryWrapper.lambda().eq(FlowNodeDefinitionEntity::getPrimitive, primitive);
        }
        if (flowCode != null) {
            queryWrapper.lambda().eq(FlowNodeDefinitionEntity::getFlowCode, flowCode);
        }
        return FlowNodeDefinitionConverter.INSTANCE.entitiesToModels(this.list(queryWrapper));
    }

    public List<NodeDefinition> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(FlowNodeDefinitionEntity::getCode, codes);
        return FlowNodeDefinitionConverter.INSTANCE.entitiesToModels(this.list(queryWrapper));
    }

    /**
     * create node info
     *
     * @param nodeInfo info
     * @return AppInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public NodeInfo createNodeInfo(NodeInfo nodeInfo) {
        this.checkDifferentName(nodeInfo);
        this.checkDifferentCode(nodeInfo);
        NodeDefinition nodeDefinition = FlowNodeDefinitionConverter.INSTANCE.infoToDefinition(nodeInfo);
        NodeDefinition nTpl = NodeDefinitionBuilder.buildByPrimitive(nodeDefinition.getPrimitive().name(),
                StringUtils.isNotEmpty(nodeInfo.getCredentialTypeCode()));
        nodeDefinition.setSettings(nTpl.getSettings());
        nodeDefinition.setResources(nTpl.getResources());
        FlowNodeDefinitionEntity entity = FlowNodeDefinitionConverter.INSTANCE.modelToEntity(nodeDefinition);
        entity.setIcon(null);
        boolean s = this.save(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "create node definition error");
        }
        nodeInfo.setNodeId(entity.getNodeId());
        return nodeInfo;
    }

    public NodeDefinition createNodeDefinition(NodeDefinition nodeDefinition) {
        this.checkDifferentName(nodeDefinition);
        this.checkDifferentCode(nodeDefinition);
        fillNodeType(nodeDefinition);
        NodeDefinitionCodeBuilder.build(nodeDefinition);
        FlowNodeDefinitionEntity entity = FlowNodeDefinitionConverter.INSTANCE.modelToEntity(nodeDefinition);
        boolean s = this.save(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "create node definition error");
        }
        nodeDefinition.setNodeId(entity.getNodeId());

        return nodeDefinition;
    }

    public List<FlowNodeDefinitionEntity> listByNodeCodes(List<String> codes) {
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(FlowNodeDefinitionEntity::getCode, codes);
        return this.list(queryWrapper);
    }

    /**
     * modify node info
     *
     * @param nodeInfo info
     * @return AppInfo
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, key = "#nodeInfo.nodeId")
    public NodeInfo updateNodeInfo(NodeInfo nodeInfo) {
        this.checkDifferentName(nodeInfo);
        this.checkDifferentCode(nodeInfo);
        FlowNodeDefinitionEntity entity = this.getById(nodeInfo.getNodeId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "node definition not exits");
        }
        FlowNodeDefinitionConverter.INSTANCE.infoToEntity(nodeInfo, entity);
        if (StringUtils.isNotEmpty(nodeInfo.getIcon()) && nodeInfo.getIcon().startsWith(IMAGE_PREFIX)) {
            entity.setIcon("/image/APP/" + nodeInfo.getNodeId() + "?t=" + RandomStringUtils.randomNumeric(5));
        }

        entity.setUpdatedTime(LocalDateTime.now());
        boolean s = this.updateById(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "modify node definition error");
        }

        NodeInfo newInfo = FlowNodeDefinitionConverter.INSTANCE.entityToSimple(entity);
        newInfo.setNodeGroupId(nodeInfo.getNodeGroupId());

        return newInfo;
    }

    /**
     * modify node definition
     *
     * @param nodeDefinition node definition info
     * @return NodeDefinition
     */
    @CacheEvict(cacheNames = CACHE_NAME, key = "#nodeDefinition.nodeId")
    public NodeDefinition updateNodeDefinition(NodeDefinition nodeDefinition) {
        FlowNodeDefinitionEntity entity = this.getById(nodeDefinition.getNodeId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "node definition not exits");
        }
        fillNodeType(nodeDefinition);
        NodeDefinitionCodeBuilder.build(nodeDefinition);
        FlowNodeDefinitionConverter.INSTANCE.modelToEntity(nodeDefinition, entity);
        boolean s = this.updateById(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "modify node definition error");
        }
        return FlowNodeDefinitionConverter.INSTANCE.entityToModel(entity);
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#nodeDefinition.nodeId")
    public NodeDefinition updateAllNodeDefinition(NodeDefinition nodeDefinition) {
        FlowNodeDefinitionEntity entity = this.getById(nodeDefinition.getNodeId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "node definition not exits");
        }
        fillNodeType(nodeDefinition);
        NodeDefinitionCodeBuilder.build(nodeDefinition);
        entity = FlowNodeDefinitionConverter.INSTANCE.modelToEntity(nodeDefinition);
        boolean s = this.updateById(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "modify node definition error");
        }
        return FlowNodeDefinitionConverter.INSTANCE.entityToModel(entity);
    }


    public Boolean updateNodeUsed(List<Integer> nodeIds, Boolean used) {
        UpdateWrapper<FlowNodeDefinitionEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(FlowNodeDefinitionEntity::getNodeId, nodeIds)
                .set(FlowNodeDefinitionEntity::getUsed, used);
        return this.update(updateWrapper);
    }

    /**
     * node definition detail info
     *
     * @param nodeId
     * @return NodeDefinition
     */
    @Cacheable(cacheNames = CACHE_NAME, key = "#nodeId")
    public NodeDefinition getNodeDefinition(Integer nodeId) {
        FlowNodeDefinitionEntity entity = getById(nodeId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "get node definition not exits", nodeId);
        }
        NodeDefinition nodeDefinition = FlowNodeDefinitionConverter.INSTANCE.entityToModel(entity);
        fillNodeType(nodeDefinition);
        return nodeDefinition;
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#nodeId")
    public Boolean updateNodeDefinitionStatus(Integer nodeId, DataStatus status) {

        UpdateWrapper<FlowNodeDefinitionEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(FlowNodeDefinitionEntity::getNodeId, nodeId)
                .set(FlowNodeDefinitionEntity::getStatus, status.name());
        return this.update(updateWrapper);
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#nodeId")
    public Boolean deleteNodeDefinition(Integer nodeId) {
        boolean res = this.removeById(nodeId);
        if (res) {
            EventBusCenter.async(new AvatarRemoveEvent(nodeId, ImageType.APP));
        }
        return res;
    }

    /**
     * check different node have the same name
     *
     * @param nodeInfo
     */
    private void checkDifferentName(NodeInfo nodeInfo) {
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<FlowNodeDefinitionEntity> lambda = queryWrapper.lambda();
        lambda.eq(FlowNodeDefinitionEntity::getName, nodeInfo.getName());
        if (nodeInfo.getNodeId() != null) {
            lambda.ne(FlowNodeDefinitionEntity::getNodeId, nodeInfo.getNodeId());
        }
        long count = super.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "node name", nodeInfo.getName());
        }
    }

    /**
     * check different node have the same code
     *
     * @param nodeInfo
     */
    private void checkDifferentCode(NodeInfo nodeInfo) {
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<FlowNodeDefinitionEntity> lambda = queryWrapper.lambda();
        lambda.eq(FlowNodeDefinitionEntity::getCode, nodeInfo.getCode());
        if (nodeInfo.getNodeId() != null) {
            lambda.ne(FlowNodeDefinitionEntity::getNodeId, nodeInfo.getNodeId());
        }
        long count = super.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "node code", nodeInfo.getCode());
        }
    }

    private void fillNodeType(NodeDefinition nodeDefinition) {
        if (nodeDefinition.getScripts() != null) {
            try {
                String script = (String) nodeDefinition.getScripts().get(NodeDefinition.SCRIPT_ACTION);
                if (script != null && script.startsWith("class:")) {
                    script = script.split("\n")[0];
                    String cls = script.substring(6);
                    nodeDefinition.setNodeType(cls);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        if (nodeDefinition.getNodeType() != null) {
            if(MapUtils.isEmpty(nodeDefinition.getScripts())){
                nodeDefinition.setScripts(new LinkedHashMap<>());
            }
            Map<String,Object> scripts = nodeDefinition.getScripts();
            String src = (String) scripts.get(BaseNodeExecutor.FIELD_ACTION);
            if(StringUtils.isEmpty(src)){
                scripts.put(NodeDefinition.SCRIPT_ACTION, "class:" + nodeDefinition.getNodeType());
            }
        }
    }
}
