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

package io.innospots.workflow.console.mapper.instance;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.workflow.console.entity.instance.EdgeInstanceEntity;
import io.innospots.workflow.core.node.instance.Edge;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * @author Wren
 * @date 2021-12-25 17:49:00
 */
@Mapper
public interface EdgeInstanceBeanConverter extends BaseBeanConverter {

    EdgeInstanceBeanConverter INSTANCE = Mappers.getMapper(EdgeInstanceBeanConverter.class);

    /**
     * Edge to FlowEdgeInstanceEntity
     *
     * @param edge
     * @return FlowEdgeInstanceEntity
     */
    EdgeInstanceEntity modelToEntity(Edge edge);


    /**
     * Edge to FlowEdgeInstanceEntity List
     *
     * @param list
     * @return
     */
    List<EdgeInstanceEntity> modelToEntityList(List<Edge> list);

    /**
     * FlowEdgeInstanceEntity to Edge
     *
     * @param entity
     * @return
     */
    Edge entityToModel(EdgeInstanceEntity entity);

    /**
     * FlowEdgeInstanceEntity to Edge List
     *
     * @param list
     * @return
     */
    List<Edge> entityToModelList(List<EdgeInstanceEntity> list);
}
