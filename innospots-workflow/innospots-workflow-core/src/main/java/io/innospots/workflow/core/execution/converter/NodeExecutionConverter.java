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

package io.innospots.workflow.core.execution.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.execution.entity.NodeExecutionEntity;
import io.innospots.workflow.core.execution.node.NodeExecution;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/18
 */
@Mapper
public interface NodeExecutionConverter extends BaseBeanConverter<NodeExecution,NodeExecutionEntity> {

    NodeExecutionConverter INSTANCE = Mappers.getMapper(NodeExecutionConverter.class);


    default Map<String, Object> modelToMap(NodeExecution nodeExecution, boolean underscore) {
        NodeExecutionEntity nodeExecutionEntity = INSTANCE.modelToEntity(nodeExecution);
        if (nodeExecutionEntity.getCreatedTime() == null) {
            nodeExecutionEntity.setCreatedTime(LocalDateTime.now());
        }
        nodeExecutionEntity.setUpdatedTime(LocalDateTime.now());
        nodeExecutionEntity.setUpdatedBy(CCH.authUser());
        if (nodeExecutionEntity.getCreatedBy() == null) {
            nodeExecutionEntity.setCreatedBy(CCH.authUser());
        }
        nodeExecutionEntity.setProjectId(CCH.projectId());
        if (nodeExecutionEntity.getMessage() != null && nodeExecutionEntity.getMessage().length() > 2048) {
            nodeExecutionEntity.setMessage(nodeExecutionEntity.getMessage().substring(0, 2048));
        }
        return BeanUtils.toMap(nodeExecutionEntity, underscore, true);
    }

    default NodeExecution mapToModel(Map<String, Object> data, boolean underscore) {
        NodeExecutionEntity entity = BeanUtils.toBean(data, NodeExecutionEntity.class, underscore);
        return INSTANCE.entityToModel(entity);
    }

}
