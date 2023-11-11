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

package io.innospots.workflow.core.node.definition.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/28
 */
@Getter
@Setter
@Schema(title = "the template define the node group")
public class FlowTemplate extends FlowTemplateBase {

    private List<AppNodeGroup> appNodeGroups;

    /**
     * key: nodeCode ,value: nodeDefinition
     */
    @JsonIgnore
    private Map<String, NodeNodeDefinition> nodeCache = new HashMap<>();

    private void initialize() {
        if (isNotEmpty(appNodeGroups)) {
            for (AppNodeGroup appNodeGroup : appNodeGroups) {
                for (NodeNodeDefinition node : appNodeGroup.getNodes()) {
                    nodeCache.put(node.getCode(), node);
                }
            }
        }
    }

    public Map<String, NodeNodeDefinition> nodes() {
        if (nodeCache.isEmpty() && !appNodeGroups.isEmpty()) {
            initialize();
        }
        return nodeCache;
    }

    public NodeNodeDefinition getNode(String nodeCode) {
        return nodes().get(nodeCode);
    }


}
