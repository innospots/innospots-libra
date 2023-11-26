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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * @author Raydian
 * @date 2021/1/16
 */
public interface WorkflowInstanceDao extends BaseMapper<WorkflowInstanceEntity> {


    /**
     * get flow instance by flow key
     *
     * @param flowKey
     * @return
     */
    default WorkflowInstanceEntity getInstanceByFlowKey(String flowKey) {
        QueryWrapper<WorkflowInstanceEntity> query = new QueryWrapper<>();
        query.lambda().eq(WorkflowInstanceEntity::getFlowKey, flowKey);
        return this.selectOne(query);
    }

//    @Update({"update", WorkflowInstanceEntity.TABLE_NAME, "set updated_time = #{updatedTime,jdbcType=TIMESTAMP},",
//            "status = #{status,jdbcType=VARCHAR}",
//            "where workflow_instance_id = #{flowInstanceId,jdbcType=NUMERIC}"})
    default int updateStatus(Long flowInstanceId, DataStatus status, LocalDateTime updatedTime){
        UpdateWrapper<WorkflowInstanceEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(WorkflowInstanceEntity::getStatus, status)
               .set(WorkflowInstanceEntity::getUpdatedTime, updatedTime)
               .eq(WorkflowInstanceEntity::getWorkflowInstanceId, flowInstanceId);
        return this.update(updateWrapper);
    }
}
