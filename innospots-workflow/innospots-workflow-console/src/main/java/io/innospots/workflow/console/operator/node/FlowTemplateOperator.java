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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.workflow.core.node.definition.converter.FlowTemplateConverter;
import io.innospots.workflow.core.node.definition.dao.FlowTemplateDao;
import io.innospots.workflow.core.node.definition.entity.FlowTemplateEntity;
import io.innospots.workflow.core.node.definition.model.FlowTemplate;
import io.innospots.workflow.core.node.definition.model.FlowTemplateBase;
import io.innospots.workflow.core.node.definition.model.NodeGroup;
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
public class FlowTemplateOperator extends ServiceImpl<FlowTemplateDao, FlowTemplateEntity> {

    public static final String CACHE_NAME = "CACHE_FLOW_TEMPLATE";

    private FlowNodeGroupOperator flowNodeGroupOperator;

    public FlowTemplateOperator(FlowNodeGroupOperator flowNodeGroupOperator) {
        this.flowNodeGroupOperator = flowNodeGroupOperator;
    }

    /**
     * create flow template
     *
     * @return WorkflowTemplate
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public FlowTemplateBase createTemplate(FlowTemplateBase flowTemplateBase) {

        boolean checkCode = this.checkTemplate(flowTemplateBase.getTplCode());
        if (checkCode) {
            throw ResourceException.buildDuplicateException(this.getClass(), "template code is exists");
        }
        FlowTemplateEntity flowTemplateEntity = FlowTemplateConverter.INSTANCE.baseModelToEntity(flowTemplateBase);
        boolean row = this.save(flowTemplateEntity);
        if (!row) {
            throw ResourceException.buildCreateException(this.getClass(), "flow template create fail");
        }

        return FlowTemplateConverter.INSTANCE.entityToModel(flowTemplateEntity);
    }

    /**
     * modify flow template
     *
     * @return Boolean
     */
    public Boolean updateTemplate(FlowTemplateBase flowTemplateBase) {
        String code = flowTemplateBase.getTplCode();
        Integer flowTplId = flowTemplateBase.getFlowTplId();
        if (StringUtils.isNotEmpty(code)) {
            QueryWrapper<FlowTemplateEntity> checkQuery = new QueryWrapper<>();
            checkQuery.lambda().eq(FlowTemplateEntity::getTplCode, code)
                    .ne(FlowTemplateEntity::getFlowTplId, flowTplId);
            long count = this.count(checkQuery);
            if (count > 0) {
                throw ResourceException.buildDuplicateException(this.getClass(), "template code is exists");
            }
        }
        FlowTemplateEntity flowTemplateEntity = FlowTemplateConverter.INSTANCE.baseModelToEntity(flowTemplateBase);


        boolean row = this.updateById(flowTemplateEntity);
        if (!row) {
            throw ResourceException.buildCreateException(this.getClass(), "flow template save fail");
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Integer flowTplId) {
        List<NodeGroup> nodeGroups = this.flowNodeGroupOperator.getGroupByFlowTplId(flowTplId, false);
        if (CollectionUtils.isNotEmpty(nodeGroups)) {
            for (NodeGroup nodeGroup : nodeGroups) {
                flowNodeGroupOperator.removeNodeGroup(nodeGroup.getNodeGroupId());
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
        UpdateWrapper<FlowTemplateEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .set(FlowTemplateEntity::getStatus, status)
                .set(FlowTemplateEntity::getUpdatedTime, LocalDateTime.now())
                .set(FlowTemplateEntity::getUpdatedBy, "")
                .eq(FlowTemplateEntity::getFlowTplId, flowTplId);
        boolean row = this.update(null, updateWrapper);
        if (!row) {
            throw ResourceException.buildCreateException(this.getClass(), "flow template save fail");
        }
        return true;
    }

    public boolean checkTemplate(String templateCode) {
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTemplateEntity::getTplCode, templateCode);
        return this.count(queryWrapper) > 0;
    }


    /**
     * get flow template info
     *
     * @param flowTplId    template id
     * @param includeNodes include nodes flag
     * @return WorkflowTemplate
     */
    public FlowTemplate getTemplate(Integer flowTplId, boolean includeNodes, boolean onlyConnector, boolean excludeTrigger) {
        FlowTemplateEntity entity = this.getById(flowTplId);
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
    public FlowTemplate getTemplate(Integer flowTplId, boolean includeNodes) {
        return getTemplate(flowTplId, includeNodes, false, true);
    }

    public FlowTemplate getTemplate(String templateCode, boolean includeNodes) {
        QueryWrapper<FlowTemplateEntity> query = new QueryWrapper<>();
        query.lambda().eq(FlowTemplateEntity::getTplCode, templateCode);
        FlowTemplateEntity entity = this.getOne(query);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "flow template " + templateCode + " not exits");
        }
        return getTemplate(entity, includeNodes, false, true);
    }

    private FlowTemplate getTemplate(FlowTemplateEntity entity, boolean includeNodes, boolean onlyConnector, boolean excludeTrigger) {
        FlowTemplate appFlowTemplate = FlowTemplateConverter.INSTANCE.entityToModel(entity);
        List<NodeGroup> nodeGroups = flowNodeGroupOperator.getGroupByFlowTplId(entity.getFlowTplId(), includeNodes);
        if (includeNodes && onlyConnector) {
            /*
            nodeGroups.forEach(group -> group.setNodes(group.getNodes()
                    .stream().filter(appNode -> StringUtils.isNotEmpty(appNode.getConnectorName()) && !"None".equals(appNode.getConnectorName()))
                    .collect(Collectors.toList())));

             */
        }
        nodeGroups.forEach(appNodeGroup -> {
                    if("trigger".equals(appNodeGroup.getCode())){
                        appNodeGroup.setHidden(true);
                    }
                }
        );
        appFlowTemplate.setNodeGroups(nodeGroups);
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
    public PageBody<FlowTemplateBase> pageTemplates(String name, DataStatus status, Integer page, Integer size) {
        PageBody<FlowTemplateBase> result = new PageBody<>();
        Page<FlowTemplateEntity> queryPage = new Page<>(page, size);
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            queryWrapper.lambda().eq(FlowTemplateEntity::getTplName, name);
        }
        if (status != null) {
            queryWrapper.lambda().eq(FlowTemplateEntity::getStatus, status);
        }
        queryPage = this.page(queryPage, queryWrapper);
        if (queryPage != null) {
            result.setTotal(queryPage.getTotal());
            result.setCurrent(queryPage.getCurrent());
            result.setPageSize(queryPage.getSize());
            result.setList(CollectionUtils.isEmpty(queryPage.getRecords()) ? new ArrayList<FlowTemplateBase>() :
                    FlowTemplateConverter.INSTANCE.entityToBaseModelList(queryPage.getRecords()));
        }
        return result;
    }

    public List<FlowTemplateBase> listOnlineFlowTemplates() {
        QueryWrapper<FlowTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", DataStatus.ONLINE);
        return FlowTemplateConverter.INSTANCE.entityToBaseModelList(this.list(queryWrapper));
    }

}
