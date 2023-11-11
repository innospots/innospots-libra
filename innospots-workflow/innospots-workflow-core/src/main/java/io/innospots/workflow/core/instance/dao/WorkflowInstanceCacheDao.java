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
import io.innospots.workflow.core.instance.entity.WorkflowInstanceCacheEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * @author Raydian
 * @date 2021/1/16
 */
public interface WorkflowInstanceCacheDao extends BaseMapper<WorkflowInstanceCacheEntity> {


    /**
     * delete flowInstanceCache le updateTime
     *
     * @param updateTime
     * @return
     */
    @Select("delete FROM flow_instance_cache " +
            " where updated_time <= #{updateTime}")
    int deleteByUpdateTime(@Param("updateTime") LocalDateTime updateTime);

}
