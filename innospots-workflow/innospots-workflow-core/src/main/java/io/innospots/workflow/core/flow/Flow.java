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

package io.innospots.workflow.core.flow;


import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ValidatorException;
import io.innospots.script.base.ExecutorManagerFactory;
import io.innospots.workflow.core.enums.FlowStatus;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.flow.manage.FlowCompiler;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Raydian
 * @date 2020/12/20
 */
public class Flow {

    private static final Logger logger = LoggerFactory.getLogger(Flow.class);

    private FlowStatus loadStatus = FlowStatus.UNLOAD;

    private HashSetValuedHashMap<String, String> nextNodeCache = new HashSetValuedHashMap<>();

    private final HashSetValuedHashMap<String, String> sourceNodeCache = new HashSetValuedHashMap<>();

    private List<BaseNodeExecutor> startNodes;

    private Map<String, BaseNodeExecutor> nodeCache = new HashMap<>();

    private List<INodeExecutionListener> nodeExecutionListeners;

    private final BuildProcessInfo buildProcessInfo = new BuildProcessInfo();

    private final boolean force;

    private final WorkflowBody workflowBody;


    public Flow(WorkflowBody workflowBody, boolean force) {
        this.workflowBody = workflowBody;
        this.force = force;
        buildProcessInfo.setWorkflowInstanceId(this.workflowBody.getWorkflowInstanceId());
        buildProcessInfo.setFlowKey(workflowBody.getFlowKey());
        buildProcessInfo.setDatasourceCode(workflowBody.getDatasourceCode());
        buildProcessInfo.setTriggerCode(this.workflowBody.getTriggerCode());
    }

    public BuildProcessInfo prepare() {
        if (loadStatus == FlowStatus.UNLOAD && nextNodeCache.isEmpty()) {
            buildProcessInfo.setStartTime(System.currentTimeMillis());
            loadStatus = FlowStatus.LOADING;
            Map<String, BaseNodeExecutor> tmpNodeCache = new HashMap<>();
            try {

                FlowCompiler flowCompiler = FlowCompiler.build(workflowBody);
                if (force) {
                    flowCompiler.clear();
                    flowCompiler = FlowCompiler.build(workflowBody);
                }

                if (!flowCompiler.isCompiled()) {
                    flowCompiler.compile();
                }

                List<BaseNodeExecutor> tmpStartNodes = new ArrayList<>();
                for (NodeInstance nodeInstance : workflowBody.getStarts()) {
                    BaseNodeExecutor nodeExecutor = buildNodeExecutor(nodeInstance);
                    if (nodeExecutor != null) {
                        tmpNodeCache.put(nodeExecutor.nodeKey(), nodeExecutor);
                        tmpStartNodes.add(nodeExecutor);
                    }
                }//end for

                startNodes = tmpStartNodes;

                HashSetValuedHashMap<String, String> tmpNextNodes = new HashSetValuedHashMap<>();
                for (NodeInstance node : workflowBody.getNodes()) {
                    BaseNodeExecutor nodeExecutor = tmpNodeCache.get(node.getNodeKey());
                    if (nodeExecutor == null) {
                        nodeExecutor = buildNodeExecutor(node);
                        if (nodeExecutor != null) {
                            tmpNodeCache.put(nodeExecutor.nodeKey(), nodeExecutor);
                        }
                    }
                    //the sources of this node
                    List<String> sourceNodeKeys = workflowBody.sourceNodeKeys(node.getNodeKey());
                    if (CollectionUtils.isNotEmpty(sourceNodeKeys)) {
                        sourceNodeCache.putAll(node.getNodeKey(), sourceNodeKeys);
                    }

                    if (!tmpNextNodes.containsKey(node.getNodeKey())) {
                        List<NodeInstance> nextNodes = workflowBody.nextNodes(node.getNodeKey());
                        if (CollectionUtils.isEmpty(nextNodes)) {
                            continue;
                        }
                        for (NodeInstance nextNode : nextNodes) {
                            if (nextNode == null) {
                                logger.warn("nextNode is null, sourceNode:{}", node.getNodeKey());
                                continue;
                            }
                            nodeExecutor = tmpNodeCache.get(nextNode.getNodeKey());
                            if (nodeExecutor == null) {
                                nodeExecutor = buildNodeExecutor(nextNode);
                                if (nodeExecutor != null) {
                                    tmpNodeCache.put(nodeExecutor.nodeKey(), nodeExecutor);
                                }
                            }//end if
                            tmpNextNodes.put(node.getNodeKey(), nextNode.getNodeKey());
                        }//end for

                    }//end if
                }//end for

                this.nodeCache = tmpNodeCache;
                this.nextNodeCache = tmpNextNodes;
                if (buildProcessInfo.getFailCount() == 0 &&
                        buildProcessInfo.getBuildException() == null &&
                        buildProcessInfo.getErrorInfo().size() == 0) {
                    this.loadStatus = FlowStatus.LOADED;
                } else {
                    this.loadStatus = FlowStatus.FAIL;
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                loadStatus = FlowStatus.FAIL;
                buildProcessInfo.incrementFail();
                buildProcessInfo.setStatus(loadStatus);
                buildProcessInfo.setMessage(e.getMessage());
                buildProcessInfo.setBuildException(e);
            } finally {
                logger.info("flowId: {}, name:{}, loadStatus: {}, node size:{}", workflowBody.getWorkflowInstanceId(), workflowBody.getName(), loadStatus, nodeCache.size());
                buildProcessInfo.setStatus(loadStatus);
                ExecutorManagerFactory.clear(workflowBody.identifier());
                buildProcessInfo.setEndTime(System.currentTimeMillis());
            }
        }
        return buildProcessInfo;
    }

    private BaseNodeExecutor buildNodeExecutor(NodeInstance nodeInstance) {
        BaseNodeExecutor nodeExecutor = null;
        try {
            nodeExecutor = NodeExecutorFactory.build(workflowBody.identifier(), nodeInstance);
            nodeExecutor.addNodeExecutionListener(nodeExecutionListeners);
//            tmpNodeCache.put(nodeExecutor.nodeKey(), nodeExecutor);
            if (nodeExecutor.getBuildException() != null) {
                buildProcessInfo.addNodeProcess(nodeExecutor.nodeKey(), nodeExecutor.getBuildException());
            } else {
                buildProcessInfo.incrementSuccess();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            buildProcessInfo.incrementFail();
            buildProcessInfo.addNodeProcess(nodeInstance.getNodeKey(), e);
        }
        return nodeExecutor;
    }

    public List<BaseNodeExecutor> startNodes() {
        if (CollectionUtils.isNotEmpty(startNodes)) {
            return startNodes;
        }
        return Collections.emptyList();
    }

    public Set<String> nextNodes(String nodeKey) {
        if (!nextNodeCache.isEmpty()) {
            return nextNodeCache.get(nodeKey);
        }
        return Collections.emptySet();
    }

    /*
    private List<INodeExecutor> convert(List<NodeInstance> nodeInstances) {
        List<INodeExecutor> nodeExecutors = new ArrayList<>();
        for (NodeInstance nodeInstance : nodeInstances) {
            BaseAppNode appNode = BaseAppNode.buildAppNode(identifier(),nodeInstance);
            appNode.setBuildCounter(nodeBuildCounter);
            appNode.addNodeExecutionListener(nodeExecutionListeners);
            nodeExecutors.add(appNode);
        }

        return nodeExecutors;
    }

     */

    public BaseNodeExecutor findNode(String nodeKey) {
        return nodeCache.get(nodeKey);
    }

    public List<BaseNodeExecutor> findNodes(List<String> nodeKeys) {
        List<BaseNodeExecutor> nodes = new ArrayList<>();
        for (String nodeKey : nodeKeys) {
            nodes.add(findNode(nodeKey));
        }
        return nodes;
    }

    public void setNodeExecutionListeners(List<INodeExecutionListener> nodeExecutionListeners) {
        this.nodeExecutionListeners = nodeExecutionListeners;
    }

    public Set<String> sourceKey(String nodeKey) {
        return sourceNodeCache.get(nodeKey);
    }

    public Set<String> dependencyNodes(String nodeKey) {
        HashSet<String> sourceNodes = new HashSet<>();
        sourceNodes.add(nodeKey);
        fillDependencies(sourceNodes, nodeKey);
        return sourceNodes;
    }

    private void fillDependencies(Set<String> nodeKeys, String nodeKey) {
        Set<String> sourceKeys = sourceKey(nodeKey);
        if (sourceKeys != null) {
            nodeKeys.addAll(sourceKeys);
            for (String sourceKey : sourceKeys) {
                fillDependencies(nodeKeys, sourceKey);
            }
        }
    }

    public FlowStatus getFlowStatus() {
        return loadStatus;
    }

    public boolean isLoaded() {
        return loadStatus == FlowStatus.LOADED;
    }

    public boolean hasUpdate(LocalDateTime updateTime) {
        if (this.getUpdatedTime() == null) {
            throw ValidatorException.buildMissingException(this.getClass(), "updateTime is null can not compare");
        }
        return !this.getUpdatedTime().equals(updateTime);
    }

    public int nodeSize() {
        return this.nodeCache.size();
    }

    public String key() {
        return workflowBody.key();
    }

    public Long getWorkflowInstanceId() {
        return workflowBody.getWorkflowInstanceId();
    }

    public Integer getRevision() {
        return workflowBody.getRevision();
    }

    public String getFlowKey() {
        return workflowBody.getFlowKey();
    }

    public LocalDateTime getUpdatedTime() {
        return workflowBody.getUpdatedTime();
    }


    public WorkflowBody getWorkflowBody() {
        return workflowBody;
    }

    public DataStatus getStatus() {
        return workflowBody.getStatus();
    }

    public BuildProcessInfo getBuildProcessInfo() {
        return buildProcessInfo;
    }
}
