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
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.PageBody;
import io.innospots.base.utils.ApplicationContextUtils;
import io.innospots.libra.base.event.NewAvatarEvent;
import io.innospots.workflow.console.entity.apps.AppNodeDefinitionEntity;
import io.innospots.workflow.console.entity.apps.AppNodeGroupNodeEntity;
import io.innospots.workflow.console.model.AppQueryRequest;
import io.innospots.workflow.console.operator.apps.AppNodeDefinitionOperator;
import io.innospots.workflow.console.operator.apps.AppNodeGroupOperator;
import io.innospots.workflow.console.operator.instance.NodeInstanceOperator;
import io.innospots.workflow.core.enums.AppPrimitive;
import io.innospots.workflow.core.enums.AppSource;
import io.innospots.workflow.core.node.AppInfo;
import io.innospots.workflow.core.node.apps.AppNodeDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.innospots.workflow.console.operator.apps.AppNodeDefinitionOperator.IMAGE_PREFIX;

/**
 * @author chenc
 * @version 1.0.0
 * @date 2023/3/26
 */
@Slf4j
@Service
public class AppService {


    private final AppNodeDefinitionOperator appNodeDefinitionOperator;

    private final AppNodeGroupOperator appNodeGroupOperator;

    private final NodeInstanceOperator nodeInstanceOperator;

    public AppService(AppNodeGroupOperator appNodeGroupOperator, AppNodeDefinitionOperator appNodeDefinitionOperator, NodeInstanceOperator nodeInstanceOperator) {
        this.appNodeGroupOperator = appNodeGroupOperator;
        this.nodeInstanceOperator = nodeInstanceOperator;
        this.appNodeDefinitionOperator = appNodeDefinitionOperator;
    }

    public PageBody<AppInfo> pageAppInfos(AppQueryRequest request) {
        PageBody<AppInfo> body = appNodeDefinitionOperator.pageAppDefinitions(request);
        List<AppInfo> appInfos = body.getList();
        if (!CollectionUtils.isEmpty(appInfos)) {
            List<Integer> nodeIds = appInfos.stream().map(AppInfo::getNodeId).collect(Collectors.toList());
            List<AppNodeGroupNodeEntity> entityList = appNodeGroupOperator.getGroupNodeByNodeIds(1, nodeIds);
            Map<Integer, Integer> entityMap = entityList.stream().collect(Collectors.toMap(AppNodeGroupNodeEntity::getNodeId, AppNodeGroupNodeEntity::getNodeGroupId));
            for (AppInfo appInfo : appInfos) {
                appInfo.setNodeGroupId(entityMap.get(appInfo.getNodeId()));
            }
        }
        return body;
    }

    public List<AppNodeDefinition> listOnlineNodes(AppPrimitive primitive) {
        return appNodeDefinitionOperator.listOnlineNodes(primitive);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppInfo createAppInfo(AppInfo appInfo) {
        appInfo.setUsed(Boolean.FALSE);
        appInfo.setAppSource(AppSource.non_system);
        appInfo = appNodeDefinitionOperator.createAppInfo(appInfo);
        Integer nodeId = appInfo.getNodeId();
        if (nodeId != null) {
            List<Integer> nodeIds = new ArrayList<>();
            nodeIds.add(nodeId);
            appNodeGroupOperator.saveOrUpdateNodeGroupNode(1, appInfo.getNodeGroupId(), nodeIds);
            if (StringUtils.isNotEmpty(appInfo.getIcon()) && appInfo.getIcon().startsWith(IMAGE_PREFIX)) {
                ApplicationContextUtils.sendAppEvent(new NewAvatarEvent(nodeId, ImageType.APP, null, appInfo.getIcon()));
            }
            // update app icon
            appInfo = appNodeDefinitionOperator.updateAppInfo(appInfo);
        }
        return appInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppInfo updateAppInfo(AppInfo appInfo) {
        /*
        long count = nodeInstanceOperator.countByNodeDefinitionId(appInfo.getNodeId());
        if (count > 0) {
            throw ResourceException.buildUpdateException(this.getClass(), "This App has been referenced by the workflow and cannot be edited!");
        }
         */
        String icon = appInfo.getIcon();
        Integer nodeId = appInfo.getNodeId();
        if (StringUtils.isNotEmpty(icon) && icon.startsWith(IMAGE_PREFIX)) {
            ApplicationContextUtils.sendAppEvent(new NewAvatarEvent(nodeId, ImageType.APP, null, icon));
        }
        appInfo = appNodeDefinitionOperator.updateAppInfo(appInfo);
        List<Integer> nodeIds = new ArrayList<>();
        nodeIds.add(nodeId);
        appNodeGroupOperator.saveOrUpdateNodeGroupNode(1, appInfo.getNodeGroupId(), nodeIds);

        return appInfo;
    }

    public Boolean updateNodeDefinitionStatus(Integer nodeId, DataStatus status) {
        if (DataStatus.OFFLINE == status) {
            long count = nodeInstanceOperator.countByNodeDefinitionId(nodeId);
            if (count > 0) {
                throw ResourceException.buildUpdateException(this.getClass(), "This App has been referenced by the workflow and cannot be offline!");
            }
        }
        return appNodeDefinitionOperator.updateNodeDefinitionStatus(nodeId, status);
    }

    public Boolean deleteNodeDefinition(Integer nodeId) {
        long count = nodeInstanceOperator.countByNodeDefinitionId(nodeId);
        if (count > 0) {
            throw ResourceException.buildUpdateException(this.getClass(), "This App has been referenced by the workflow and cannot be deleted!");
        }
        return appNodeDefinitionOperator.deleteNodeDefinition(nodeId);
    }

    public AppNodeDefinition updateAppNodeDefinition(AppNodeDefinition appNodeDefinition) {
        return appNodeDefinitionOperator.updateNodeDefinition(appNodeDefinition);
    }

    public AppNodeDefinition getAppNodeDefinitionById(Integer nodeId) {
        AppNodeDefinition appNodeDefinition = appNodeDefinitionOperator.getNodeDefinition(nodeId);
        List<AppNodeGroupNodeEntity> entityList = appNodeGroupOperator.getGroupNodeByNodeIds(1, Collections.singletonList(nodeId));
        if (!CollectionUtils.isEmpty(entityList)) {
            appNodeDefinition.setNodeGroupId(entityList.get(0).getNodeGroupId());
        }
        return appNodeDefinition;
    }

    public Map<String, String> getAppNodeIcons() {
        List<AppNodeDefinitionEntity> entityList = appNodeDefinitionOperator.list(
                new QueryWrapper<AppNodeDefinitionEntity>().lambda().eq(AppNodeDefinitionEntity::getStatus, DataStatus.ONLINE)
        );

        Map<String, String> iconMap = new HashMap<>();
        for (AppNodeDefinitionEntity appNodeDefinitionEntity : entityList) {
            iconMap.put(appNodeDefinitionEntity.getCode(), appNodeDefinitionEntity.getIcon());
        }
        return iconMap;
    }
}
