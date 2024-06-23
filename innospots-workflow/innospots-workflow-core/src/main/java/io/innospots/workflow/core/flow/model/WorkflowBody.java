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

package io.innospots.workflow.core.flow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.instance.model.Edge;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2020/11/28
 */
@Slf4j
@Getter
@Setter
public class WorkflowBody extends WorkflowBaseBody implements Initializer {

    /**
     * start node
     */
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private List<NodeInstance> starts = new ArrayList<>();

    /**
     * node cache,key is nodeKey
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, NodeInstance> nodeCache = new HashMap<>();

    /**
     * target node cache
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, List<NodeInstance>> targetNodes = new HashMap<>();

    /**
     * source nodes cache
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, List<NodeInstance>> sourceNodes = new HashMap<>();


    public WorkflowBody() {
    }

    public NodeInstance findNode(String nodeKey) {
        return nodeCache.get(nodeKey);
    }

    public List<NodeInstance> nextNodes(String nodeKey) {
        return targetNodes.get(nodeKey);
    }


    public List<NodeInstance> sourceNodes(String nodeKey) {
        return sourceNodes.get(nodeKey);
    }

    public List<String> sourceNodeKeys(String nodeKey) {
        List<NodeInstance> instances = sourceNodes.get(nodeKey);
        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyList();
        }
        return instances.stream().map(NodeInstance::getNodeKey).collect(Collectors.toList());
    }


    /**
     * 初始化 解析edge，赋值instance.nextNodeKeys
     */
    @Override
    public void initialize() {
        //解析
        //CollectionUtils.isEmpty(this.getEdges()) ||
        if (CollectionUtils.isEmpty(this.getNodes()) || !nodeCache.isEmpty()) {
            return;
        }

        HashSetValuedHashMap<String, String> nextNodeCache = new HashSetValuedHashMap<>();
        HashSetValuedHashMap<String, String> sourceNodeCache = new HashSetValuedHashMap<>();

        //key: nodeKey, map: anchor key, values: target nodeKeys
        HashMap<String, Map<String, List<String>>> nodeAnchorCache = new HashMap<>();

        if (CollectionUtils.isNotEmpty(this.getEdges())) {
            for (Edge edge : this.getEdges()) {
                nextNodeCache.put(edge.getSource(), edge.getTarget());
                sourceNodeCache.put(edge.getTarget(), edge.getSource());
                Map<String, List<String>> nodeAnchors = nodeAnchorCache.computeIfAbsent(edge.getSource(), k -> new HashMap<>());

                List<String> nodeKeys = nodeAnchors.computeIfAbsent(edge.getSourceAnchor(), k -> new ArrayList<>());
                if (!nodeKeys.contains(edge.getTarget())) {
                    nodeKeys.add(edge.getTarget());
                }
            }
        }

        for (NodeInstance nodeInstance : this.getNodes()) {
            nodeInstance.setNextNodeKeys(new ArrayList<>(nextNodeCache.get(nodeInstance.getNodeKey())));
            nodeInstance.setNodeAnchors(nodeAnchorCache.get(nodeInstance.getNodeKey()));
            nodeCache.put(nodeInstance.getNodeKey(), nodeInstance);
            if (!sourceNodeCache.containsKey(nodeInstance.getNodeKey())) {
                //not sourceKey
                starts.add(nodeInstance);
            }
        }

        log.debug("parseInstance starts:{}", this.starts);

        for (NodeInstance nodeInstance : this.getNodes()) {
            fillNodeRelation(nextNodeCache, nodeInstance, targetNodes);
            fillNodeRelation(sourceNodeCache, nodeInstance, sourceNodes);
            //set node prevNodeKeys
            nodeInstance.setPrevNodeKeys(sourceNodeKeys(nodeInstance.getNodeKey()));
        }
    }

    private void fillNodeRelation(HashSetValuedHashMap<String, String> sourceNodeCache,
                                  NodeInstance nodeInstance,
                                  Map<String, List<NodeInstance>> sourceNodes) {
        List<NodeInstance> sourceList = sourceNodes.getOrDefault(nodeInstance.getNodeKey(), new ArrayList<>());
        Set<String> sourceKeys = sourceNodeCache.get(nodeInstance.getNodeKey());
        if (CollectionUtils.isNotEmpty(sourceKeys)) {
            for (String sourceKey : sourceKeys) {
                if(sourceKey == null){
                    log.error("source key is null:{}",nodeInstance);
                    continue;
                }
                NodeInstance ni = nodeCache.get(sourceKey);
                if(ni!=null){
                    sourceList.add(ni);
                }

            }
        }
        sourceNodes.putIfAbsent(nodeInstance.getNodeKey(), sourceList);
    }


}
