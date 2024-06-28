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

package io.innospots.workflow.core.node.definition.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.node.NodeInfo;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfo;
import io.innospots.workflow.core.node.definition.model.NodeConnectorConfig;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import io.innospots.workflow.core.node.definition.model.NodeResource;
import io.innospots.workflow.core.node.definition.model.NodeSetting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * @author Wren
 * @date 2021-12-25 17:49:00
 */
@Mapper
public interface FlowNodeDefinitionConverter extends BaseBeanConverter<NodeDefinition, FlowNodeDefinitionEntity> {

    FlowNodeDefinitionConverter INSTANCE = Mappers.getMapper(FlowNodeDefinitionConverter.class);

    @Mapping(target = "deletable", expression = "java( !\"innospots\".equals(meta.getVendor()) )")
    NodeInfo metaToInfo(NodeMetaInfo meta);

    List<NodeInfo> metaToInfoList(List<NodeMetaInfo> metaList);

    FlowNodeDefinitionEntity infoToEntity(NodeInfo nodeInfo);

    @Mapping(target = "deletable", expression = "java( !(model.getDeletable() !=null && model.getDeletable()) || !\"innospots\".equals(model.getVendor()) )")
    NodeInfo modelToSimple(NodeDefinition model);

    @Mapping(target = "deletable", expression = "java( !(entity.getDeletable() !=null && entity.getDeletable()) || !\"innospots\".equals(entity.getVendor()) )")
    NodeInfo entityToSimple(FlowNodeDefinitionEntity entity);

    List<NodeInfo> modelToInfoList(List<NodeDefinition> nodeDefinitionList);

    /**
     * NodeDefinition fill to FlowNodeDefinitionEntity
     *
     * @param model
     * @param entity
     */
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "primitive", ignore = true)
    @Mapping(target = "icon", ignore = true)
    @Mapping(target = "description", ignore = true)
    void modelToEntity(NodeDefinition model, @MappingTarget FlowNodeDefinitionEntity entity);

    @Mapping(target = "icon", ignore = true)
    void infoToEntity(NodeInfo nodeInfo, @MappingTarget FlowNodeDefinitionEntity entity);


    default List<NodeConnectorConfig> strToConnectorConfigs(String connectorConfig){
        return JSONUtils.toList(connectorConfig, NodeConnectorConfig.class);
    }

    default String connectorConfigsToStr(List<NodeConnectorConfig> connectorConfigs){
        return JSONUtils.toJsonString(connectorConfigs);
    }

    default String resourceToStr(NodeResource nodeResource){
        return JSONUtils.toJsonString(nodeResource);
    }

    default NodeResource strToResource(String resource){
        return JSONUtils.parseObject(resource, NodeResource.class);
    }

    default String settingToStr(NodeSetting nodeSetting){
        return JSONUtils.toJsonString(nodeSetting);
    }

    default NodeSetting strToSetting(String setting){
        return JSONUtils.parseObject(setting, NodeSetting.class);
    }
}
