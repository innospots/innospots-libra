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
import io.innospots.libra.base.events.AvatarRemoveEvent;
import io.innospots.workflow.core.node.NodeInfo;
import io.innospots.workflow.core.node.definition.converter.FlowNodeDefinitionConverter;
import io.innospots.workflow.core.node.definition.dao.FlowNodeDefinitionDao;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.console.model.AppQueryRequest;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.innospots.workflow.core.node.definition.model.NodeNodeDefinition;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Raydian
 * @date 2020/11/28
 */
public class FlowNodeDefinitionOperator extends ServiceImpl<FlowNodeDefinitionDao, FlowNodeDefinitionEntity> {


    public static final String IMAGE_PREFIX = "data:image";
    public static final String CACHE_NAME = "CACHE_NODE_DEFINITION";

    /**
     * node definition page info
     *
     * @param queryRequest
     * @return Page<NodeDefinition>
     */
    public PageBody<NodeInfo> pageAppDefinitions(AppQueryRequest queryRequest) {
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.FLOW_TPL_ID", 1);
        if (queryRequest.getDataStatus() != null) {
            queryWrapper.eq(queryRequest.getDataStatus() != null, "s.STATUS", queryRequest.getDataStatus());
        }
        if (StringUtils.isNotBlank(queryRequest.getQueryInput())) {
            queryWrapper.like("s.NAME", "%" + queryRequest.getQueryInput() + "%");
        }
        if (queryRequest.getCategoryId() != null && queryRequest.getCategoryId() > 1) {
            queryWrapper.eq("t.NODE_GROUP_ID", queryRequest.getCategoryId());
        }

        queryWrapper.orderBy(true, true, "UPDATED_TIME");

        IPage<FlowNodeDefinitionEntity> queryPage = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        queryPage = this.baseMapper.selectAppPage(queryPage, queryWrapper);
        PageBody<NodeInfo> result = new PageBody<>();
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

    public List<NodeNodeDefinition> listOnlineNodes(NodePrimitive primitive) {
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda()
                .eq(FlowNodeDefinitionEntity::getStatus, DataStatus.ONLINE);
        if (primitive != null) {
            queryWrapper.lambda().eq(FlowNodeDefinitionEntity::getPrimitive, primitive);
        }
        return FlowNodeDefinitionConverter.INSTANCE.entitiesToModels(this.list(queryWrapper));
    }

    public List<FlowNodeDefinitionEntity> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }
        QueryWrapper<FlowNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(FlowNodeDefinitionEntity::getCode, codes);
        return this.list(queryWrapper);
    }

    /**
     * create app info
     *
     * @param nodeInfo app info
     * @return AppInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public NodeInfo createAppInfo(NodeInfo nodeInfo) {
        this.checkDifferentName(nodeInfo);
        this.checkDifferentCode(nodeInfo);
        FlowNodeDefinitionEntity entity = FlowNodeDefinitionConverter.INSTANCE.infoToEntity(nodeInfo);
        entity.setIcon(null);
        boolean s = this.save(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "create app node definition error");
        }
        nodeInfo.setNodeId(entity.getNodeId());
        return nodeInfo;
    }

    /**
     * modify app info
     *
     * @param nodeInfo app info
     * @return AppInfo
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, key = "#nodeInfo.nodeId")
    public NodeInfo updateAppInfo(NodeInfo nodeInfo) {
        this.checkDifferentName(nodeInfo);
        this.checkDifferentCode(nodeInfo);
        FlowNodeDefinitionEntity entity = this.getById(nodeInfo.getNodeId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "node definition not exits");
        }
        FlowNodeDefinitionConverter.INSTANCE.infoToEntity(nodeInfo, entity);
        if(StringUtils.isNotEmpty(nodeInfo.getIcon()) && nodeInfo.getIcon().startsWith(IMAGE_PREFIX)){
            entity.setIcon("/image/APP/" + nodeInfo.getNodeId() + "?t=" + RandomStringUtils.randomNumeric(5));
        }

        entity.setUpdatedTime(LocalDateTime.now());
        boolean s = this.updateById(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "modify app node definition error");
        }

        return FlowNodeDefinitionConverter.INSTANCE.entityToSimple(entity);
    }

    /**
     * modify node definition
     *
     * @param appNodeDefinition node definition info
     * @return NodeDefinition
     */
    @CacheEvict(cacheNames = CACHE_NAME, key = "#appNodeDefinition.nodeId")
    public NodeNodeDefinition updateNodeDefinition(NodeNodeDefinition appNodeDefinition) {
        FlowNodeDefinitionEntity entity = this.getById(appNodeDefinition.getNodeId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "node definition not exits");
        }
        FlowNodeDefinitionConverter.INSTANCE.modelToEntity(appNodeDefinition, entity);
        boolean s = this.updateById(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "modify node definition error");
        }
        return FlowNodeDefinitionConverter.INSTANCE.entityToModel(entity);
    }

    public Boolean updateAppUsed(List<Integer> nodeIds, Boolean used) {
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
    public NodeNodeDefinition getNodeDefinition(Integer nodeId) {
        FlowNodeDefinitionEntity entity = getById(nodeId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "get node definition not exits", nodeId);
        }
        NodeNodeDefinition appNodeDefinition = FlowNodeDefinitionConverter.INSTANCE.entityToModel(entity);
        return appNodeDefinition;
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
        if(res){
            EventBusCenter.async(new AvatarRemoveEvent(nodeId, ImageType.APP));
        }
        return res;
    }

    /**
     * check different app have the same name
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
            throw ResourceException.buildExistException(this.getClass(), "app name", nodeInfo.getName());
        }
    }

    /**
     * check different app have the same code
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
            throw ResourceException.buildExistException(this.getClass(), "app code", nodeInfo.getCode());
        }
    }
}
