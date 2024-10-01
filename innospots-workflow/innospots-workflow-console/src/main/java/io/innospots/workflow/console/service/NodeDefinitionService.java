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
import io.innospots.workflow.core.node.definition.model.NodeGroup;
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

    public PageBody<NodeInfo> pageNodeInfos(NodeQueryRequest request) {
        PageBody<NodeInfo> body = flowNodeDefinitionOperator.pageNodeDefinitions(request);
        List<NodeInfo> nodeInfos = body.getList();
        if (!CollectionUtils.isEmpty(nodeInfos)) {
            List<Integer> nodeIds = nodeInfos.stream().map(NodeInfo::getNodeId).collect(Collectors.toList());
            List<FlowNodeGroupNodeEntity> entityList = flowNodeGroupOperator.getGroupNodeByNodeIds(request.getFlowTplId(), nodeIds);
            if (!CollectionUtils.isEmpty(entityList)) {
                Map<Integer, Integer> entityMap = entityList.stream().collect(Collectors.toMap(FlowNodeGroupNodeEntity::getNodeId, FlowNodeGroupNodeEntity::getNodeGroupId));
                for (NodeInfo nodeInfo : nodeInfos) {
                    nodeInfo.setNodeGroupId(entityMap.get(nodeInfo.getNodeId()));
                }
            }
        }
        return body;
    }

    public List<NodeDefinition> listOnlineNodes(NodePrimitive primitive, String tplFlowCode) {
        return flowNodeDefinitionOperator.listOnlineNodes(primitive,tplFlowCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public NodeInfo createNodeInfo(NodeInfo nodeInfo) {
        nodeInfo.setUsed(Boolean.FALSE);
        nodeInfo.setVendor(NodeVendor.official.name());
        nodeInfo = flowNodeDefinitionOperator.createNodeInfo(nodeInfo);
        Integer nodeId = nodeInfo.getNodeId();
        if (nodeId != null) {
            List<Integer> nodeIds = new ArrayList<>();
            nodeIds.add(nodeId);
            flowNodeGroupOperator.saveOrUpdateNodeGroupNode(nodeInfo.getNodeGroupId(), nodeIds);
            if (StringUtils.isNotEmpty(nodeInfo.getIcon()) && nodeInfo.getIcon().startsWith(IMAGE_PREFIX)) {
                EventBusCenter.async(new NewAvatarEvent(nodeInfo.getCode(), ImageType.NODE, null, nodeInfo.getIcon()));
            }
            // update node icon
            nodeInfo = flowNodeDefinitionOperator.updateNodeInfo(nodeInfo);
        }
        return nodeInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public NodeInfo updateNodeInfo(NodeInfo nodeInfo) {
        /*
        long count = nodeInstanceOperator.countByNodeDefinitionId(appInfo.getNodeId());
        if (count > 0) {
            throw ResourceException.buildUpdateException(this.getClass(), "This App has been referenced by the workflow and cannot be edited!");
        }
         */
        String icon = nodeInfo.getIcon();
        Integer nodeId = nodeInfo.getNodeId();
        if (StringUtils.isNotEmpty(icon) && icon.startsWith(IMAGE_PREFIX)) {
            EventBusCenter.async(new NewAvatarEvent(nodeInfo.getCode(), ImageType.NODE, null, icon));
        }
        nodeInfo = flowNodeDefinitionOperator.updateNodeInfo(nodeInfo);
        List<Integer> nodeIds = new ArrayList<>();
        nodeIds.add(nodeId);
        flowNodeGroupOperator.saveOrUpdateNodeGroupNode(nodeInfo.getNodeGroupId(), nodeIds);

        return nodeInfo;
    }

    /**
     * update node group id
     * @param nodeId
     * @param nodeGroupId new group id
     * @return
     */
    public NodeInfo updateNodeGroup(Integer nodeId, Integer nodeGroupId) {
        NodeInfo nodeInfo = flowNodeDefinitionOperator.getNodeDefinition(nodeId);
        nodeInfo.setNodeGroupId(nodeGroupId);
        List<Integer> nodeIds = new ArrayList<>();
        nodeIds.add(nodeId);
        flowNodeGroupOperator.saveOrUpdateNodeGroupNode(nodeGroupId, nodeIds);
        return nodeInfo;
    }

    @Transactional
    public NodeDefinition createNodeDefinition(NodeDefinition nodeDefinition) {
        nodeDefinition = flowNodeDefinitionOperator.createNodeDefinition(nodeDefinition);
        NodeGroup nodeGroup = this.flowNodeGroupOperator.getNodeGroupByCode(nodeDefinition.getPrimitive().name());
        if(nodeGroup!=null){
            this.updateNodeGroup(nodeDefinition.getNodeId(), nodeGroup.getNodeGroupId());
        }
        return nodeDefinition;
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
            throw ResourceException.buildUpdateException(this.getClass(), "This Node has been referenced by the workflow and cannot be deleted!");
        }
        boolean r = flowNodeDefinitionOperator.deleteNodeDefinition(nodeId);
        if (r) {
            flowNodeGroupOperator.removeNodeGroupByNodeId(nodeId);
        }

        return r;
    }

    public NodeDefinition updateNodeDefinition(NodeDefinition nodeDefinition) {
        return flowNodeDefinitionOperator.updateNodeDefinition(nodeDefinition);
    }

    public NodeDefinition getNodeDefinitionById(Integer nodeId) {
        NodeDefinition nodeDefinition = flowNodeDefinitionOperator.getNodeDefinition(nodeId);
        List<FlowNodeGroupNodeEntity> entityList = flowNodeGroupOperator.getGroupNodeByNodeIds(1, Collections.singletonList(nodeId));
        if (!CollectionUtils.isEmpty(entityList)) {
            nodeDefinition.setNodeGroupId(entityList.get(0).getNodeGroupId());
        }
        return nodeDefinition;
    }

    public Map<String, String> getNodeIcons() {
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
