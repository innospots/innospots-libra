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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.workflow.console.converter.apps.AppFlowTemplateConverter;
import io.innospots.workflow.console.dao.apps.AppFlowTemplateDao;
import io.innospots.workflow.console.entity.apps.AppFlowTemplateEntity;
import io.innospots.workflow.core.node.apps.AppFlowTemplate;
import io.innospots.workflow.core.node.apps.AppFlowTemplateBase;
import io.innospots.workflow.core.node.apps.AppNodeGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2020/11/28
 */
public class AppFlowTemplateOperator extends ServiceImpl<AppFlowTemplateDao, AppFlowTemplateEntity> {

    public static final String CACHE_NAME = "CACHE_FLOW_TEMPLATE";

    private AppNodeGroupOperator appNodeGroupOperator;

    public AppFlowTemplateOperator(AppNodeGroupOperator appNodeGroupOperator) {
        this.appNodeGroupOperator = appNodeGroupOperator;
    }

    /**
     * create flow template
     *
     * @return WorkflowTemplate
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public AppFlowTemplateBase createTemplate(AppFlowTemplateBase appFlowTemplateBase) {

        boolean checkCode = this.checkTemplate(appFlowTemplateBase.getTplCode());
        if (checkCode) {
            throw ResourceException.buildDuplicateException(this.getClass(), "template code is exists");
        }
        AppFlowTemplateEntity appFlowTemplateEntity = AppFlowTemplateConverter.INSTANCE.baseModelToEntity(appFlowTemplateBase);
        boolean row = this.save(appFlowTemplateEntity);
        if (!row) {
            throw ResourceException.buildCreateException(this.getClass(), "flow template create fail");
        }

        return AppFlowTemplateConverter.INSTANCE.entityToModel(appFlowTemplateEntity);
    }

    /**
     * modify flow template
     *
     * @return Boolean
     */
    public Boolean updateTemplate(AppFlowTemplateBase appFlowTemplateBase) {
        String code = appFlowTemplateBase.getTplCode();
        Integer flowTplId = appFlowTemplateBase.getFlowTplId();
        if (StringUtils.isNotEmpty(code)) {
            QueryWrapper<AppFlowTemplateEntity> checkQuery = new QueryWrapper<>();
            checkQuery.lambda().eq(AppFlowTemplateEntity::getTplCode, code)
                    .ne(AppFlowTemplateEntity::getFlowTplId, flowTplId);
            long count = this.count(checkQuery);
            if (count > 0) {
                throw ResourceException.buildDuplicateException(this.getClass(), "template code is exists");
            }
        }
        AppFlowTemplateEntity appFlowTemplateEntity = AppFlowTemplateConverter.INSTANCE.baseModelToEntity(appFlowTemplateBase);


        boolean row = this.updateById(appFlowTemplateEntity);
        if (!row) {
            throw ResourceException.buildCreateException(this.getClass(), "flow template save fail");
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Integer flowTplId) {
        List<AppNodeGroup> appNodeGroups = this.appNodeGroupOperator.getGroupByFlowTplId(flowTplId, false);
        if (CollectionUtils.isNotEmpty(appNodeGroups)) {
            for (AppNodeGroup appNodeGroup : appNodeGroups) {
                appNodeGroupOperator.removeNodeGroup(appNodeGroup.getNodeGroupId());
            }
        }
        return this.removeById(flowTplId);
    }

    /**
     * modify flow template status
     *
     * @param flowTplId template id
     * @param status    template status  {@link DataStatus}
     * @return Boolean
     */
    public Boolean updateStatus(Integer flowTplId, DataStatus status) {
        if (!status.equals(DataStatus.ONLINE) && !status.equals(DataStatus.OFFLINE)) {
            throw ResourceException.buildStatusException(this.getClass(), "modify flow template, status error ");
        }
        UpdateWrapper<AppFlowTemplateEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .set(AppFlowTemplateEntity::getStatus, status)
                .set(AppFlowTemplateEntity::getUpdatedTime, LocalDateTime.now())
                .set(AppFlowTemplateEntity::getUpdatedBy, "")
                .eq(AppFlowTemplateEntity::getFlowTplId, flowTplId);
        boolean row = this.update(null, updateWrapper);
        if (!row) {
            throw ResourceException.buildCreateException(this.getClass(), "flow template save fail");
        }
        return true;
    }

    public boolean checkTemplate(String templateCode) {
        QueryWrapper<AppFlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppFlowTemplateEntity::getTplCode, templateCode);
        return this.count(queryWrapper) > 0;
    }


    /**
     * get flow template info
     *
     * @param flowTplId    template id
     * @param includeNodes include nodes flag
     * @return WorkflowTemplate
     */
    public AppFlowTemplate getTemplate(Integer flowTplId, boolean includeNodes, boolean onlyConnector, boolean excludeTrigger) {
        AppFlowTemplateEntity entity = this.getById(flowTplId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "flow template " + flowTplId + " not exits");
        }
        return getTemplate(entity, includeNodes, onlyConnector, excludeTrigger);
    }

    /**
     * exclude trigger group app node
     * @param flowTplId
     * @param includeNodes
     * @return
     */
    public AppFlowTemplate getTemplate(Integer flowTplId, boolean includeNodes) {
        return getTemplate(flowTplId, includeNodes, false, true);
    }

    public AppFlowTemplate getTemplate(String templateCode, boolean includeNodes) {
        QueryWrapper<AppFlowTemplateEntity> query = new QueryWrapper<>();
        query.lambda().eq(AppFlowTemplateEntity::getTplCode, templateCode);
        AppFlowTemplateEntity entity = this.getOne(query);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "flow template " + templateCode + " not exits");
        }
        return getTemplate(entity, includeNodes, false, true);
    }

    private AppFlowTemplate getTemplate(AppFlowTemplateEntity entity, boolean includeNodes, boolean onlyConnector, boolean excludeTrigger) {
        AppFlowTemplate appFlowTemplate = AppFlowTemplateConverter.INSTANCE.entityToModel(entity);
        List<AppNodeGroup> nodeGroups = appNodeGroupOperator.getGroupByFlowTplId(entity.getFlowTplId(), includeNodes);
        if (includeNodes && onlyConnector) {
            nodeGroups.forEach(group -> group.setNodes(group.getNodes()
                    .stream().filter(appNode -> StringUtils.isNotEmpty(appNode.getConnectorName()) && !"None".equals(appNode.getConnectorName()))
                    .collect(Collectors.toList())));
        }
        nodeGroups.forEach(appNodeGroup -> {
                    if("trigger".equals(appNodeGroup.getCode())){
                        appNodeGroup.setHidden(true);
                    }
                }
        );
        appFlowTemplate.setAppNodeGroups(nodeGroups);
        return appFlowTemplate;
    }


    /**
     * get flow template page according to status and name
     *
     * @param name   template name
     * @param status template status
     * @param page   page
     * @param size   size
     * @return Page<WorkflowTemplate>
     */
    public PageBody<AppFlowTemplateBase> pageTemplates(String name, DataStatus status, Integer page, Integer size) {
        PageBody<AppFlowTemplateBase> result = new PageBody<>();
        Page<AppFlowTemplateEntity> queryPage = new Page<>(page, size);
        QueryWrapper<AppFlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            queryWrapper.lambda().eq(AppFlowTemplateEntity::getTplName, name);
        }
        if (status != null) {
            queryWrapper.lambda().eq(AppFlowTemplateEntity::getStatus, status);
        }
        queryPage = this.page(queryPage, queryWrapper);
        if (queryPage != null) {
            result.setTotal(queryPage.getTotal());
            result.setCurrent(queryPage.getCurrent());
            result.setPageSize(queryPage.getSize());
            result.setList(CollectionUtils.isEmpty(queryPage.getRecords()) ? new ArrayList<AppFlowTemplateBase>() :
                    AppFlowTemplateConverter.INSTANCE.entityToBaseModelList(queryPage.getRecords()));
        }
        return result;
    }

    public List<AppFlowTemplateBase> listOnlineFlowTemplates() {
        QueryWrapper<AppFlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", DataStatus.ONLINE);
        return AppFlowTemplateConverter.INSTANCE.entityToBaseModelList(this.list(queryWrapper));
    }

}
