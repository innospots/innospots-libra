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
import io.innospots.workflow.console.converter.apps.AppNodeDefinitionConverter;
import io.innospots.workflow.console.dao.apps.AppNodeDefinitionDao;
import io.innospots.workflow.console.entity.apps.AppNodeDefinitionEntity;
import io.innospots.workflow.console.model.AppQueryRequest;
import io.innospots.workflow.core.enums.AppPrimitive;
import io.innospots.workflow.core.node.AppInfo;
import io.innospots.workflow.core.node.apps.AppNodeDefinition;
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
public class AppNodeDefinitionOperator extends ServiceImpl<AppNodeDefinitionDao, AppNodeDefinitionEntity> {


    public static final String IMAGE_PREFIX = "data:image";
    public static final String CACHE_NAME = "CACHE_NODE_DEFINITION";

    /**
     * node definition page info
     *
     * @param queryRequest
     * @return Page<NodeDefinition>
     */
    public PageBody<AppInfo> pageAppDefinitions(AppQueryRequest queryRequest) {
        QueryWrapper<AppNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
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

        IPage<AppNodeDefinitionEntity> queryPage = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        queryPage = this.baseMapper.selectAppPage(queryPage, queryWrapper);
        PageBody<AppInfo> result = new PageBody<>();
        if (queryPage != null) {
            result.setTotal(queryPage.getTotal());
            result.setTotalPage(queryPage.getPages());
            result.setCurrent(queryPage.getCurrent());
            result.setPageSize(queryPage.getSize());
            List<AppInfo> appInfos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(queryPage.getRecords())) {
                List<AppNodeDefinitionEntity> entities = queryPage.getRecords();
                for (AppNodeDefinitionEntity entity : entities) {
                    if (entity.getUsed() == null) {
                        entity.setUsed(Boolean.TRUE);
                    }
                    AppInfo appInfo = AppNodeDefinitionConverter.INSTANCE.entityToSimple(entity);
                    appInfos.add(appInfo);
                }


            }
            result.setList(appInfos);
        }
        return result;
    }

    public List<AppNodeDefinition> listOnlineNodes(AppPrimitive primitive) {
        QueryWrapper<AppNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda()
                .eq(AppNodeDefinitionEntity::getStatus, DataStatus.ONLINE);
        if (primitive != null) {
            queryWrapper.lambda().eq(AppNodeDefinitionEntity::getPrimitive, primitive);
        }
        return AppNodeDefinitionConverter.INSTANCE.entitiesToModels(this.list(queryWrapper));
    }

    public List<AppNodeDefinitionEntity> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }
        QueryWrapper<AppNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(AppNodeDefinitionEntity::getCode, codes);
        return this.list(queryWrapper);
    }

    /**
     * create app info
     *
     * @param appInfo app info
     * @return AppInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public AppInfo createAppInfo(AppInfo appInfo) {
        this.checkDifferentName(appInfo);
        this.checkDifferentCode(appInfo);
        AppNodeDefinitionEntity entity = AppNodeDefinitionConverter.INSTANCE.infoToEntity(appInfo);
        entity.setIcon(null);
        boolean s = this.save(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "create app node definition error");
        }
        appInfo.setNodeId(entity.getNodeId());
        return appInfo;
    }

    /**
     * modify app info
     *
     * @param appInfo app info
     * @return AppInfo
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, key = "#appInfo.nodeId")
    public AppInfo updateAppInfo(AppInfo appInfo) {
        this.checkDifferentName(appInfo);
        this.checkDifferentCode(appInfo);
        AppNodeDefinitionEntity entity = this.getById(appInfo.getNodeId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "node definition not exits");
        }
        AppNodeDefinitionConverter.INSTANCE.infoToEntity(appInfo, entity);
        if(StringUtils.isNotEmpty(appInfo.getIcon()) && appInfo.getIcon().startsWith(IMAGE_PREFIX)){
            entity.setIcon("/image/APP/" + appInfo.getNodeId() + "?t=" + RandomStringUtils.randomNumeric(5));
        }

        entity.setUpdatedTime(LocalDateTime.now());
        boolean s = this.updateById(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "modify app node definition error");
        }

        return AppNodeDefinitionConverter.INSTANCE.entityToSimple(entity);
    }

    /**
     * modify node definition
     *
     * @param appNodeDefinition node definition info
     * @return NodeDefinition
     */
    @CacheEvict(cacheNames = CACHE_NAME, key = "#appNodeDefinition.nodeId")
    public AppNodeDefinition updateNodeDefinition(AppNodeDefinition appNodeDefinition) {
        AppNodeDefinitionEntity entity = this.getById(appNodeDefinition.getNodeId());
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "node definition not exits");
        }
        AppNodeDefinitionConverter.INSTANCE.modelToEntity(appNodeDefinition, entity);
        boolean s = this.updateById(entity);
        if (!s) {
            throw ResourceException.buildCreateException(this.getClass(), "modify node definition error");
        }
        return AppNodeDefinitionConverter.INSTANCE.entityToModel(entity);
    }

    public Boolean updateAppUsed(List<Integer> nodeIds, Boolean used) {
        UpdateWrapper<AppNodeDefinitionEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(AppNodeDefinitionEntity::getNodeId, nodeIds)
                .set(AppNodeDefinitionEntity::getUsed, used);
        return this.update(updateWrapper);
    }

    /**
     * node definition detail info
     *
     * @param nodeId
     * @return NodeDefinition
     */
    @Cacheable(cacheNames = CACHE_NAME, key = "#nodeId")
    public AppNodeDefinition getNodeDefinition(Integer nodeId) {
        AppNodeDefinitionEntity entity = getById(nodeId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "get node definition not exits", nodeId);
        }
        AppNodeDefinition appNodeDefinition = AppNodeDefinitionConverter.INSTANCE.entityToModel(entity);
        return appNodeDefinition;
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#nodeId")
    public Boolean updateNodeDefinitionStatus(Integer nodeId, DataStatus status) {

        UpdateWrapper<AppNodeDefinitionEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(AppNodeDefinitionEntity::getNodeId, nodeId)
                .set(AppNodeDefinitionEntity::getStatus, status.name());
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
     * @param appInfo
     */
    private void checkDifferentName(AppInfo appInfo) {
        QueryWrapper<AppNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<AppNodeDefinitionEntity> lambda = queryWrapper.lambda();
        lambda.eq(AppNodeDefinitionEntity::getName, appInfo.getName());
        if (appInfo.getNodeId() != null) {
            lambda.ne(AppNodeDefinitionEntity::getNodeId, appInfo.getNodeId());
        }
        long count = super.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "app name", appInfo.getName());
        }
    }

    /**
     * check different app have the same code
     *
     * @param appInfo
     */
    private void checkDifferentCode(AppInfo appInfo) {
        QueryWrapper<AppNodeDefinitionEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<AppNodeDefinitionEntity> lambda = queryWrapper.lambda();
        lambda.eq(AppNodeDefinitionEntity::getCode, appInfo.getCode());
        if (appInfo.getNodeId() != null) {
            lambda.ne(AppNodeDefinitionEntity::getNodeId, appInfo.getNodeId());
        }
        long count = super.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "app code", appInfo.getCode());
        }
    }
}
