/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.workflow.core.instance.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ResourceException;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.instance.converter.WorkflowInstanceConverter;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceDao;
import io.innospots.workflow.core.instance.dao.WorkflowRevisionDao;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import io.innospots.workflow.core.instance.entity.WorkflowRevisionEntity;
import io.innospots.workflow.core.instance.events.InstanceUpdateEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import static io.innospots.workflow.core.instance.events.InstanceUpdateEvent.NODE_USE_DELETE;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class WorkflowBodyOperator {

    public static final String CACHE_NAME = "CACHE_FLOW_INSTANCE";

    private WorkflowRevisionDao workflowRevisionDao;

    private WorkflowInstanceDao workflowInstanceDao;

    private NodeInstanceOperator nodeInstanceOperator;

    private EdgeOperator edgeOperator;

    public WorkflowBodyOperator(WorkflowRevisionDao workflowRevisionDao,
                                WorkflowInstanceDao workflowInstanceDao,
                                NodeInstanceOperator nodeInstanceOperator,
                                EdgeOperator edgeOperator) {
        this.workflowRevisionDao = workflowRevisionDao;
        this.workflowInstanceDao = workflowInstanceDao;
        this.nodeInstanceOperator = nodeInstanceOperator;
        this.edgeOperator = edgeOperator;
    }

    /**
     * get flow instance by instance id
     *
     * @param workflowInstanceId
     * @param includeNodes
     * @return
     */
//    @Cacheable(cacheNames = CACHE_NAME, key = "#workflowInstanceId + '-' + #revision", condition = "!#includeNodes")
    public WorkflowBody getWorkflowBody(Long workflowInstanceId, Integer revision, Boolean includeNodes) {
        WorkflowInstanceEntity entity = workflowInstanceDao.selectById(workflowInstanceId);
        if (entity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), workflowInstanceId, revision);
        }
        return getFlowBody(entity, revision, includeNodes);
    }

    public WorkflowBody getWorkflowBody(Long workflowInstanceId, Boolean includeNodes) {
        return getWorkflowBody(workflowInstanceId, null, includeNodes);
    }

    public WorkflowBody getWorkflowBody(String workflowKey) {
        WorkflowInstanceEntity entity =workflowInstanceDao.getInstanceByFlowKey(workflowKey);
        return getWorkflowBody(entity.getWorkflowInstanceId(), entity.getRevision(), true);
    }

    public WorkflowBody getFlowBody(WorkflowInstanceEntity entity, Integer revision, Boolean includeNodes) {
        WorkflowBody flowInstance = WorkflowInstanceConverter.INSTANCE.entityToFlowBody(entity);
        if (includeNodes) {
            //use current revision, if revision is null
            if (revision == null) {
                revision = entity.getRevision();
            } else if (revision != 0) {
                // check revision exist
                WorkflowRevisionEntity workflowRevisionEntity = workflowRevisionDao.getByWorkflowInstanceIdAndRevision(entity.getWorkflowInstanceId(), revision);
                if (workflowRevisionEntity == null) {
                    log.error("flow instance revision not exits workflowInstanceId:{} revision:{}", entity.getWorkflowInstanceId(), revision);
                    throw ResourceException.buildAbandonException(this.getClass(), entity.getWorkflowInstanceId());
                }
            }
            flowInstance.setRevision(revision);
            flowInstance.setNodes(nodeInstanceOperator.getNodeInstanceByFlowInstanceId(entity.getWorkflowInstanceId(), revision));
            flowInstance.setEdges(edgeOperator.getEdgeByFlowInstanceId(entity.getWorkflowInstanceId(), revision));

            //初始化
            flowInstance.initialize();
        }
        return flowInstance;
    }


    public boolean deleteByWorkflowBody(Long workflowInstanceId) {
        int res = this.workflowInstanceDao.deleteById(workflowInstanceId);
        if(res==0){
            return false;
        }
        boolean up = this.nodeInstanceOperator.deleteNodes(workflowInstanceId);
        EventBusCenter.postSync(new InstanceUpdateEvent(workflowInstanceId, NODE_USE_DELETE));

        up = this.edgeOperator.deleteEdges(workflowInstanceId) & up;
        QueryWrapper<WorkflowRevisionEntity> revisionQueryWrapper = new QueryWrapper<>();
        revisionQueryWrapper.lambda().eq(WorkflowRevisionEntity::getWorkflowInstanceId, workflowInstanceId);

        up = this.workflowRevisionDao.delete(revisionQueryWrapper) > 0 & up;

        return up;
    }

    /**
     * 查询最近x分钟内有更新的流程实例 或者 status为online
     *
     * @param recentMinutes
     * @return
     */
    public List<WorkflowBody> selectRecentlyUpdateOrOnLine(int recentMinutes) {
        QueryWrapper<WorkflowInstanceEntity> query = new QueryWrapper<>();
        LocalDateTime updateTime = LocalDateTime.now().minusMinutes(recentMinutes);
        // greater than updatedTime params
        query.lambda().gt(WorkflowInstanceEntity::getRevision, 0).
                and(wrapper ->
                        wrapper.ge(WorkflowInstanceEntity::getUpdatedTime, updateTime).or()
                                .eq(WorkflowInstanceEntity::getStatus, DataStatus.ONLINE));

        List<WorkflowInstanceEntity> entities = workflowInstanceDao.selectList(query);

        return WorkflowInstanceConverter.INSTANCE.entitiesToFlowBodies(entities);
    }

}