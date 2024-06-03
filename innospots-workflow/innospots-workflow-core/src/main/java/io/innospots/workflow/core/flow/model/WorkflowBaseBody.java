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

import io.innospots.workflow.core.instance.model.WorkflowInstanceBase;
import io.innospots.workflow.core.instance.model.Edge;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Raydian
 * @date 2020/11/28
 */
@Getter
@Setter
public class WorkflowBaseBody extends WorkflowInstanceBase {

    private static final Logger logger = getLogger(WorkflowBaseBody.class);


    @Schema(title = "edge collection")
    private List<Edge> edges;

    @Schema(title = "node collection")
    private List<NodeInstance> nodes;

    public WorkflowBaseBody() {
    }

    public WorkflowBaseBody(Long workflowInstanceId,
                            @NotNull String name,
                            String flowKey,
                            int revision) {
        this.workflowInstanceId = workflowInstanceId;
        this.name = name;
        this.flowKey = flowKey;
        this.revision = revision;
    }

    public void addNode(NodeInstance nodeInstance){
        if(nodes == null){
            this.nodes = new ArrayList<>();
        }
        this.nodes.add(nodeInstance);
    }

    /**
     * Check whether the node is cyclic dependent
     *
     * @return
     */
    public boolean checkNodeCircle() {
        if (this.edges == null || edges.isEmpty()) {
            return false;
        }

        List<String> firstKeys = this.findFirstNodeKeys();
        boolean circleFlag = false;
        if (firstKeys != null) {
            Map<String, List<String>> relationMap = findNodeRelationMap();
            for (String nextKey : firstKeys) {
                Set<String> parentKeys = new HashSet<>();
                circleFlag = findCircle(parentKeys, nextKey, relationMap);
                if (Boolean.TRUE.equals(circleFlag)) {
                    break;
                }
            }
        }

        return circleFlag;
    }

    /**
     * Gets the dependencies of the node
     *
     * @return
     */
    public Map<String, List<String>> findNodeRelationMap() {
        Map<String, List<String>> relationMap = new HashMap<>();
        for (Edge edge : this.getEdges()) {
            if (!relationMap.containsKey(edge.getSource())) {
                relationMap.put(edge.getSource(), new ArrayList<>());
            }
            relationMap.get(edge.getSource()).add(edge.getTarget());
        }
        return relationMap;
    }

    /**
     * get next node keys
     *
     * @return
     */
    public List<String> findFirstNodeKeys() {
        Set<String> sourceKeys = new HashSet<>();
        Set<String> targetKeys = new HashSet<>();
        for (Edge edge : this.getEdges()) {
            sourceKeys.add(edge.getSource());
            targetKeys.add(edge.getTarget());
        }

        //first node keys
        List<String> firstKeys = new ArrayList<>();
        for (String key : sourceKeys) {
            if (!targetKeys.contains(key)) {
                firstKeys.add(key);
            }
        }
        return firstKeys;
    }


    /**
     * Judge cyclic dependency
     *
     * @param parentKeys
     * @param key
     * @param relationMap
     * @return
     */
    private boolean findCircle(Set<String> parentKeys, String key, Map<String, List<String>> relationMap) {
        if (parentKeys.contains(key)) {
            return true;
        } else {
            List<String> nextKeys = relationMap.getOrDefault(key, null);
            if (nextKeys != null) {
                //存在下一级节点，再放入父节点集合
                parentKeys.add(key);
                for (String nextKey : nextKeys) {
                    Set<String> _parentKeys = new HashSet<>(parentKeys);
                    if (findCircle(_parentKeys, nextKey, relationMap)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(nodes) || CollectionUtils.isEmpty(edges);
    }

    public int nodeSize(){
        return CollectionUtils.isEmpty(nodes) ? 0 : nodes.size();
    }

    public boolean equalContent(WorkflowBaseBody instanceBaseBody) {

        if (!this.workflowInstanceId.equals(instanceBaseBody.workflowInstanceId)) {
            return false;
        }

        if (this.edges != null && instanceBaseBody.edges != null &&
                this.edges.size() != instanceBaseBody.edges.size()) {
            return false;
        }

        if (this.nodes != null && instanceBaseBody.nodes != null &&
                this.nodes.size() != instanceBaseBody.nodes.size()
        ) {
            return false;
        }

        Set<Edge> edges1 = new HashSet<>();
        if (this.edges != null) {
            edges1.addAll(edges);
        }
        Set<Edge> edges2 = new HashSet<>();
        if (instanceBaseBody.edges != null) {
            edges2.addAll(instanceBaseBody.edges);
        }
        if (!edges1.equals(edges2)) {
            return false;
        }

        Set<NodeInstance> nodes1 = new HashSet<>();
        if (this.nodes != null) {
            nodes1.addAll(this.nodes);
        }

        Set<NodeInstance> nodes2 = new HashSet<>();
        if (instanceBaseBody.nodes != null) {
            nodes2.addAll(instanceBaseBody.nodes);
        }

        if (!nodes1.equals(nodes2)) {
            return false;
        }

        return true;
    }


}
