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

package io.innospots.workflow.console.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.enums.ImageType;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.events.NewAvatarEvent;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.core.node.definition.entity.FlowNodeGroupNodeEntity;
import io.innospots.workflow.console.model.NodeQueryRequest;
import io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator;
import io.innospots.workflow.console.operator.node.FlowNodeGroupOperator;
import io.innospots.workflow.core.instance.operator.NodeInstanceOperator;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.innospots.workflow.core.enums.NodeVendor;
import io.innospots.workflow.core.node.NodeInfo;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator.IMAGE_PREFIX;

/**
 * @author chenc
 * @version 1.0.0
 * @date 2023/3/26
 */
@Slf4j
@Service
public class NodeDefinitionService {


    private final FlowNodeDefinitionOperator flowNodeDefinitionOperator;

    private final FlowNodeGroupOperator flowNodeGroupOperator;

    private final NodeInstanceOperator nodeInstanceOperator;

    public NodeDefinitionService(FlowNodeGroupOperator flowNodeGroupOperator, FlowNodeDefinitionOperator flowNodeDefinitionOperator, NodeInstanceOperator nodeInstanceOperator) {
        this.flowNodeGroupOperator = flowNodeGroupOperator;
        this.nodeInstanceOperator = nodeInstanceOperator;
        this.flowNodeDefinitionOperator = flowNodeDefinitionOperator;
    }

    public PageBody<NodeInfo> pageAppInfos(NodeQueryRequest request) {
        PageBody<NodeInfo> body = flowNodeDefinitionOperator.pageAppDefinitions(request);
        List<NodeInfo> nodeInfos = body.getList();
        if (!CollectionUtils.isEmpty(nodeInfos)) {
            List<Integer> nodeIds = nodeInfos.stream().map(NodeInfo::getNodeId).collect(Collectors.toList());
            List<FlowNodeGroupNodeEntity> entityList = flowNodeGroupOperator.getGroupNodeByNodeIds(1, nodeIds);
            Map<Integer, Integer> entityMap = entityList.stream().collect(Collectors.toMap(FlowNodeGroupNodeEntity::getNodeId, FlowNodeGroupNodeEntity::getNodeGroupId));
            for (NodeInfo nodeInfo : nodeInfos) {
                nodeInfo.setNodeGroupId(entityMap.get(nodeInfo.getNodeId()));
            }
        }
        return body;
    }

    public List<NodeDefinition> listOnlineNodes(NodePrimitive primitive) {
        return flowNodeDefinitionOperator.listOnlineNodes(primitive);
    }

    @Transactional(rollbackFor = Exception.class)
    public NodeInfo createAppInfo(NodeInfo nodeInfo) {
        nodeInfo.setUsed(Boolean.FALSE);
        nodeInfo.setVendor(NodeVendor.official.name());
        nodeInfo = flowNodeDefinitionOperator.createAppInfo(nodeInfo);
        Integer nodeId = nodeInfo.getNodeId();
        if (nodeId != null) {
            List<Integer> nodeIds = new ArrayList<>();
            nodeIds.add(nodeId);
            flowNodeGroupOperator.saveOrUpdateNodeGroupNode(1, nodeInfo.getNodeGroupId(), nodeIds);
            if (StringUtils.isNotEmpty(nodeInfo.getIcon()) && nodeInfo.getIcon().startsWith(IMAGE_PREFIX)) {
                EventBusCenter.async(new NewAvatarEvent(nodeId, ImageType.APP, null, nodeInfo.getIcon()));
            }
            // update app icon
            nodeInfo = flowNodeDefinitionOperator.updateAppInfo(nodeInfo);
        }
        return nodeInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public NodeInfo updateAppInfo(NodeInfo nodeInfo) {
        /*
        long count = nodeInstanceOperator.countByNodeDefinitionId(appInfo.getNodeId());
        if (count > 0) {
            throw ResourceException.buildUpdateException(this.getClass(), "This App has been referenced by the workflow and cannot be edited!");
        }
         */
        String icon = nodeInfo.getIcon();
        Integer nodeId = nodeInfo.getNodeId();
        if (StringUtils.isNotEmpty(icon) && icon.startsWith(IMAGE_PREFIX)) {
            EventBusCenter.async(new NewAvatarEvent(nodeId, ImageType.APP, null, icon));
        }
        nodeInfo = flowNodeDefinitionOperator.updateAppInfo(nodeInfo);
        List<Integer> nodeIds = new ArrayList<>();
        nodeIds.add(nodeId);
        flowNodeGroupOperator.saveOrUpdateNodeGroupNode(1, nodeInfo.getNodeGroupId(), nodeIds);

        return nodeInfo;
    }

    public Boolean updateNodeDefinitionStatus(Integer nodeId, DataStatus status) {
        if (DataStatus.OFFLINE == status) {
            long count = nodeInstanceOperator.countByNodeDefinitionId(nodeId);
            if (count > 0) {
                throw ResourceException.buildUpdateException(this.getClass(), "This App has been referenced by the workflow and cannot be offline!");
            }
        }
        return flowNodeDefinitionOperator.updateNodeDefinitionStatus(nodeId, status);
    }

    public Boolean deleteNodeDefinition(Integer nodeId) {
        long count = nodeInstanceOperator.countByNodeDefinitionId(nodeId);
        if (count > 0) {
            throw ResourceException.buildUpdateException(this.getClass(), "This App has been referenced by the workflow and cannot be deleted!");
        }
        return flowNodeDefinitionOperator.deleteNodeDefinition(nodeId);
    }

    public NodeDefinition updateAppNodeDefinition(NodeDefinition appNodeDefinition) {
        return flowNodeDefinitionOperator.updateNodeDefinition(appNodeDefinition);
    }

    public NodeDefinition getAppNodeDefinitionById(Integer nodeId) {
        NodeDefinition appNodeDefinition = flowNodeDefinitionOperator.getNodeDefinition(nodeId);
        List<FlowNodeGroupNodeEntity> entityList = flowNodeGroupOperator.getGroupNodeByNodeIds(1, Collections.singletonList(nodeId));
        if (!CollectionUtils.isEmpty(entityList)) {
            appNodeDefinition.setNodeGroupId(entityList.get(0).getNodeGroupId());
        }
        return appNodeDefinition;
    }

    public Map<String, String> getAppNodeIcons() {
        List<FlowNodeDefinitionEntity> entityList = flowNodeDefinitionOperator.list(
                new QueryWrapper<FlowNodeDefinitionEntity>().lambda().eq(FlowNodeDefinitionEntity::getStatus, DataStatus.ONLINE)
        );

        Map<String, String> iconMap = new HashMap<>();
        for (FlowNodeDefinitionEntity flowNodeDefinitionEntity : entityList) {
            iconMap.put(flowNodeDefinitionEntity.getCode(), flowNodeDefinitionEntity.getIcon());
        }
        return iconMap;
    }
}
