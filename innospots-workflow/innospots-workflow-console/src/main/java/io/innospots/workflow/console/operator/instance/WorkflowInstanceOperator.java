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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.StringConverter;
import io.innospots.libra.base.events.NewPageEvent;
import io.innospots.libra.base.events.NotificationAnnotation;
import io.innospots.workflow.core.enums.WorkflowType;
import io.innospots.workflow.console.listener.WorkflowPageListener;
import io.innospots.workflow.console.model.WorkflowQuery;
import io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator;
import io.innospots.workflow.console.operator.node.FlowTemplateOperator;
import io.innospots.workflow.core.enums.FlowVersion;
import io.innospots.workflow.core.flow.model.WorkflowBaseInfo;
import io.innospots.workflow.core.flow.model.WorkflowInfo;
import io.innospots.workflow.core.instance.converter.WorkflowInstanceConverter;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceDao;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import io.innospots.workflow.core.instance.model.WorkflowInstance;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.innospots.workflow.core.instance.operator.WorkflowBodyOperator.CACHE_NAME;

/**
 * Workflow instance operator
 *
 * @author castor_ling
 * @date 2021-02-20
 */
@Slf4j
@Service
public class WorkflowInstanceOperator extends ServiceImpl<WorkflowInstanceDao, WorkflowInstanceEntity> {


    private FlowTemplateOperator flowTemplateOperator;

    private FlowNodeDefinitionOperator flowNodeDefinitionOperator;

    public WorkflowInstanceOperator(FlowTemplateOperator flowTemplateOperator, FlowNodeDefinitionOperator flowNodeDefinitionOperator) {
        this.flowTemplateOperator = flowTemplateOperator;
        this.flowNodeDefinitionOperator = flowNodeDefinitionOperator;
    }

    /**
     * create workflow
     *
     * @param workflowInfo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @NotificationAnnotation(name = "${event.create.workflow}", code = "create_strategy",
            module = "workflow-management",
            title = "${event.create.workflow.title}", content = "${event.create.workflow.content}")
    public WorkflowInstance createWorkflow(WorkflowInfo workflowInfo) {

        boolean check = flowTemplateOperator.checkTemplate(workflowInfo.getTemplateCode());
        if (!check) {
            throw ResourceException.buildNotExistException(this.getClass(), "workflow template is not exist, code: " + workflowInfo.getTemplateCode());
        }
        check = this.checkWorkflowName(workflowInfo.getName());
        if (check) {
            throw ResourceException.buildDuplicateException(this.getClass(), "create workflow name exist, name: " + workflowInfo.getName());
        }

        WorkflowInstanceEntity workflowInstanceEntity = WorkflowInstanceConverter.INSTANCE.workflowInfoToEntity(workflowInfo);

        boolean hasFlowKey;
        do {
            String flowKey = StringConverter.randomKey(4);
            hasFlowKey = this.checkFlowKey(flowKey);
            workflowInstanceEntity.setFlowKey(flowKey);
        } while (hasFlowKey);

        workflowInstanceEntity.setRevision(FlowVersion.DRAFT.getVersion());
        workflowInstanceEntity.setStatus(DataStatus.OFFLINE.name());
        this.save(workflowInstanceEntity);
        EventBusCenter.postSync(new NewPageEvent(workflowInstanceEntity.getWorkflowInstanceId(), WorkflowPageListener.PAGE_TYPE));
        return WorkflowInstanceConverter.INSTANCE.entityToModel(workflowInstanceEntity);
    }

    private boolean checkWorkflowName(String name) {
        QueryWrapper<WorkflowInstanceEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<WorkflowInstanceEntity> lambda = queryWrapper.lambda();
        lambda.eq(WorkflowInstanceEntity::getName, name);
        return this.count(queryWrapper) > 0;
    }

    public List<WorkflowBaseInfo> listWorkflows(String triggerCode) {
        QueryWrapper<WorkflowInstanceEntity> query = new QueryWrapper<>();
        query.lambda().eq(WorkflowInstanceEntity::getStatus, DataStatus.ONLINE)
                .eq(WorkflowInstanceEntity::getTriggerCode, triggerCode);
        List<WorkflowInstanceEntity> entities = this.list(query);
        return entities.stream().map(WorkflowInstanceConverter.INSTANCE::entityToBaseInfo).collect(Collectors.toList());
    }

    /**
     * List workflow infos
     *
     * @return
     */
    public PageBody<WorkflowInstance> pageWorkflows(WorkflowQuery request) {
        List<String> nodeCodes = null;
        if (request.getPrimitive() != null) {
            List<NodeDefinition> nodeDefinitions = flowNodeDefinitionOperator.listOnlineNodes(request.getPrimitive(), request.getWorkflowType());
            nodeCodes = nodeDefinitions.stream().map(NodeDefinition::getCode).toList();
        }

        QueryWrapper<WorkflowInstanceEntity> queryWrapper = this.queryWrapper(request);
        if (CollectionUtils.isNotEmpty(nodeCodes)) {
            queryWrapper.lambda().in(WorkflowInstanceEntity::getTriggerCode, nodeCodes);
        }
        PageBody<WorkflowInstance> pageBody = new PageBody<>();
        IPage<WorkflowInstanceEntity> oPage = new Page<>(request.getPage(), request.getSize());
        IPage<WorkflowInstanceEntity> entityPage = this.page(oPage, queryWrapper);
        List<WorkflowInstanceEntity> entities = entityPage.getRecords();
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setTotal(entityPage.getTotal());
        pageBody.setTotalPage(entityPage.getPages());
        pageBody.setList(entities.stream().map(WorkflowInstanceConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size()))));

        if (CollectionUtils.isNotEmpty(entities)) {
            List<String> triggerCodes = pageBody.getList().stream().map(WorkflowInstance::getTriggerCode).distinct().collect(Collectors.toList());
            List<NodeDefinition> nodeDefinitionEntities = flowNodeDefinitionOperator.listByCodes(triggerCodes);
            Map<String, NodeDefinition> nodeDefinitionEntityMap = nodeDefinitionEntities.stream()
                    .collect(Collectors.toMap(NodeDefinition::getCode, Function.identity()));
            for (WorkflowInstance workflowInstance : pageBody.getList()) {
                NodeDefinition nodeDefinition = nodeDefinitionEntityMap.get(workflowInstance.getTriggerCode());
                workflowInstance.setTriggerNode(nodeDefinition);
                workflowInstance.setIcon(nodeDefinition.getIcon());
            }
        }

        return pageBody;
    }

    /**
     * Update Strategy
     *
     * @param workflowInstance
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public boolean updateWorkflow(WorkflowInstance workflowInstance) {
        if (workflowInstance.getWorkflowInstanceId() == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "primary key is null, " + workflowInstance.getName());
        }
        WorkflowInstanceEntity entity = this.getWorkflowInstanceEntity(workflowInstance.getWorkflowInstanceId());
        if (entity == null || (StringUtils.isNotBlank(workflowInstance.getFlowKey()) && !workflowInstance.getFlowKey().equals(entity.getFlowKey()))) {
            throw ResourceException.buildAbandonException(this.getClass(), "illegal request, " + workflowInstance.getName());
        }
        boolean check = flowTemplateOperator.checkTemplate(workflowInstance.getTemplateCode());
        if (!check) {
            throw ResourceException.buildNotExistException(this.getClass(), "workflow template is not exist, code: " + workflowInstance.getTemplateCode());
        }
        WorkflowInstanceEntity workflowInstanceEntity = WorkflowInstanceConverter.INSTANCE.modelToEntity(workflowInstance);
        return super.updateById(workflowInstanceEntity);
    }

    public boolean bindPage(Integer pageId, Long workflowInstanceId) {
        UpdateWrapper<WorkflowInstanceEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(WorkflowInstanceEntity::getWorkflowInstanceId, workflowInstanceId)
                .set(WorkflowInstanceEntity::getPageId, pageId);
        return this.update(updateWrapper);
    }

    /**
     * remove to recycle
     *
     * @param workflowInstanceId
     * @return
     */
    public boolean removeWorkflowToRecycle(Long workflowInstanceId) {
        return updateWorkflowStatus(workflowInstanceId, DataStatus.REMOVED);
    }

    public boolean deleteWorkflowInstance(Long workflowInstanceId) {
        WorkflowInstanceEntity entity = this.getWorkflowInstanceEntity(workflowInstanceId);
        if (entity == null) {
            throw ResourceException.buildNotExistException(this.getClass(), workflowInstanceId);
        }
        return this.removeById(workflowInstanceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean onlineWorkflow(Long workflowInstanceId) {
        return updateWorkflowStatus(workflowInstanceId, DataStatus.ONLINE);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean offlineWorkflow(Long workflowInstanceId) {
        return updateWorkflowStatus(workflowInstanceId, DataStatus.OFFLINE);
    }


    /**
     * update status
     *
     * @param workflowInstanceId
     * @param dataStatus
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public boolean updateWorkflowStatus(Long workflowInstanceId, DataStatus dataStatus) {
        UpdateWrapper<WorkflowInstanceEntity> updater = new UpdateWrapper<>();
        updater.lambda().eq(WorkflowInstanceEntity::getWorkflowInstanceId, workflowInstanceId)
                .set(WorkflowInstanceEntity::getStatus, dataStatus);
        if (dataStatus == DataStatus.ONLINE) {
            updater.lambda().set(WorkflowInstanceEntity::getOnlineTime, LocalDateTime.now());
            WorkflowInstanceEntity workflowInstanceEntity = this.getById(workflowInstanceId);
            if (workflowInstanceEntity.getRevision() == null || workflowInstanceEntity.getRevision() <= 0) {
                throw ResourceException.buildStatusException(this.getClass(), "workflow haven't been published, the revision is zero, workflowInstance: " + workflowInstanceId);
            }

        }
        return this.update(updater);
    }

    /**
     * get WorkflowInfo
     *
     * @param workflowInstanceId
     * @return
     */
    public WorkflowInstance getWorkflowInstance(long workflowInstanceId) {
        WorkflowInstanceEntity workflowInstanceEntity = this.getById(workflowInstanceId);
        if (workflowInstanceEntity == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "workflow is not exist, id: " + workflowInstanceId);
        }
        return WorkflowInstanceConverter.INSTANCE.entityToModel(workflowInstanceEntity);
    }

    public WorkflowInstanceEntity getWorkflowInstanceEntity(Long workflowInstanceId) {
        return this.getById(workflowInstanceId);
    }

    public WorkflowInstanceEntity getWorkflowInstanceEntity(String flowKey) {
        QueryWrapper<WorkflowInstanceEntity> query = new QueryWrapper<>();
        query.lambda().eq(WorkflowInstanceEntity::getFlowKey, flowKey);
        return this.getOne(query);
    }

    /**
     * 查询最近x分钟内有更新的流程实例 或者 status为online
     *
     * @param recentMinutes
     * @return
     */
    public List<WorkflowInstanceEntity> selectRecentlyUpdateOrOnLine(int recentMinutes) {
        QueryWrapper<WorkflowInstanceEntity> query = new QueryWrapper<>();
        LocalDateTime updateTime = LocalDateTime.now().minusMinutes(recentMinutes);
        // greater than updatedTime params
        query.lambda().gt(WorkflowInstanceEntity::getRevision, 0).
                and(wrapper ->
                        wrapper.ge(WorkflowInstanceEntity::getUpdatedTime, updateTime).or()
                                .eq(WorkflowInstanceEntity::getStatus, DataStatus.ONLINE));

//        query.lambda().ge(WorkflowInstanceEntity::getUpdatedTime,updateTime).or()
//                .eq(WorkflowInstanceEntity::getStatus,DataStatus.ONLINE);

        return this.list(query);
    }


    /**
     * Query the data whose instance status is online or offline
     *
     * @return
     */
    public List<WorkflowInstanceEntity> selectUsedInstance() {
        QueryWrapper<WorkflowInstanceEntity> query = new QueryWrapper<>();
        query.select(WorkflowInstanceEntity.TABLE_ID);
        // greater than updatedTime params
        query.lambda().eq(WorkflowInstanceEntity::getStatus, DataStatus.ONLINE)
                .or().eq(WorkflowInstanceEntity::getStatus, DataStatus.OFFLINE);

        return this.list(query);
    }


    /**
     * query by multiple criteria
     *
     * @param request
     * @return
     */
    private QueryWrapper<WorkflowInstanceEntity> queryWrapper(WorkflowQuery request) {
        QueryWrapper<WorkflowInstanceEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<WorkflowInstanceEntity> lambda = query.lambda();
        if(StringUtils.isNotEmpty(request.getWorkflowType())){
            lambda.eq(WorkflowInstanceEntity::getTemplateCode, request.getWorkflowType());
        }else{
            lambda.eq(WorkflowInstanceEntity::getTemplateCode, WorkflowType.EVENTS.getName());
        }
        if (request.getCategoryId() == null) {
            lambda.ne(WorkflowInstanceEntity::getStatus, DataStatus.REMOVED);
            //request.setCategoryId(0);
        }else if (request.getCategoryId() < 0) {
            lambda.eq(WorkflowInstanceEntity::getStatus, DataStatus.REMOVED);
        } else {
            lambda.ne(WorkflowInstanceEntity::getStatus, DataStatus.REMOVED);
            lambda.eq(WorkflowInstanceEntity::getCategoryId, request.getCategoryId());
        }
        if(request.getDataStatus()!=null){
            lambda.eq(WorkflowInstanceEntity::getStatus,request.getDataStatus());
        }

        if (StringUtils.isNotBlank(request.getQueryInput())) {
            lambda.like(WorkflowInstanceEntity::getName, request.getQueryInput());
        }
        if (StringUtils.isNotBlank(request.getSort())) {
            query.orderBy(true, request.getAsc(),
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        } else {
            query.lambda().orderByDesc(WorkflowInstanceEntity::getUpdatedTime);
        }
        return query;
    }


    private boolean checkFlowKey(String flowKey) {
        QueryWrapper<WorkflowInstanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WorkflowInstanceEntity::getFlowKey, flowKey);
        return this.count(queryWrapper) > 0;
    }

}