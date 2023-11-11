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

package io.innospots.workflow.core.instance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.innospots.workflow.core.instance.entity.WorkflowRevisionEntity;
import org.apache.ibatis.annotations.Select;

/**
 * @author Raydian
 * @date 2021/1/16
 */
public interface WorkflowRevisionDao extends BaseMapper<WorkflowRevisionEntity> {

    /**
     * 获取工作流最新版本
     *
     * @param workflowInstanceId
     * @return
     */
    @Select("SELECT * FROM flow_instance_revision " +
            "where workflow_instance_id = #{workflowInstanceId} " +
            "order by flow_revision_id desc limit 0,1")
    WorkflowRevisionEntity getLastedByWorkflowInstanceId(Long workflowInstanceId);

    /**
     * 获取工作流版本
     *
     * @param workflowInstanceId
     * @param revision
     * @return
     */
    @Select("SELECT * FROM flow_instance_revision " +
            " where workflow_instance_id = #{workflowInstanceId} and revision = #{revision}" +
            " order by revision desc")
    WorkflowRevisionEntity getByWorkflowInstanceIdAndRevision(Long workflowInstanceId, Integer revision);


    /**
     * delete revision
     *
     * @param workflowInstanceId
     * @param revision
     * @return
     */
    @Select("delete FROM flow_instance_revision " +
            " where workflow_instance_id = #{workflowInstanceId} and revision <= #{revision}")
    Integer deleteByWorkflowInstanceIdAndRevision(Long workflowInstanceId, Integer revision);
}
