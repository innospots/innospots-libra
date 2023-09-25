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

package io.innospots.workflow.runtime.flow;


import io.innospots.base.exception.ScriptException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.re.ExpressionEngineFactory;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.enums.BuildStatus;
import io.innospots.workflow.core.enums.FlowStatus;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.flow.BuildProcessInfo;
import io.innospots.workflow.core.flow.WorkflowBody;
import io.innospots.workflow.core.flow.instance.WorkflowInstance;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
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
public class Flow extends WorkflowInstance {

    private static final Logger logger = LoggerFactory.getLogger(Flow.class);

    //protected IWorkflowLoader workflowLoader;

    private FlowStatus loadStatus = FlowStatus.UNLOAD;

    private HashSetValuedHashMap<String, String> nextNodeCache = new HashSetValuedHashMap<>();

    private HashSetValuedHashMap<String, String> sourceNodeCache = new HashSetValuedHashMap<>();

    private List<BaseAppNode> startNodes;

    private Map<String, BaseAppNode> nodeCache = new HashMap<>();

    private List<INodeExecutionListener> nodeExecutionListeners;

    private BuildProcessInfo buildProcessInfo = new BuildProcessInfo();

    private boolean force;

    private WorkflowBody workflowBody;


    public Flow(WorkflowBody workflowBody, boolean force) {
        this.workflowBody = workflowBody;
        this.force = force;
        BeanUtils.copyProperties(workflowBody, this);
        buildProcessInfo.setWorkflowInstanceId(this.workflowBody.getWorkflowInstanceId());
        buildProcessInfo.setFlowKey(workflowBody.getFlowKey());
        buildProcessInfo.setDatasourceCode(workflowBody.getDatasourceCode());
        buildProcessInfo.setTriggerCode(this.workflowBody.getTriggerCode());
    }

    public BuildProcessInfo prepare() {
        if (loadStatus == FlowStatus.UNLOAD && nextNodeCache.isEmpty()) {
            buildProcessInfo.setStartTime(System.currentTimeMillis());
            loadStatus = FlowStatus.LOADING;
            Map<String, BaseAppNode> tmpNodeCache = new HashMap<>();
            FlowCompiler flowCompiler = FlowCompiler.build(workflowBody);
            if (force) {
                flowCompiler.clear();
                flowCompiler = FlowCompiler.build(workflowBody);
            }

            if (!flowCompiler.isCompiled()) {
                try {
                    flowCompiler.compile();
                } catch (ScriptException e) {
                    logger.error(e.getMessage(), e);
                    loadStatus = FlowStatus.FAIL;
                    buildProcessInfo.setStatus(loadStatus);
                    buildProcessInfo.setMessage(e.getMessage());
                    buildProcessInfo.setBuildException(e);
                    return buildProcessInfo;
                }
            }

            List<BaseAppNode> tmpStartNodes = new ArrayList<>();
            for (NodeInstance nodeInstance : workflowBody.getStarts()) {
                try {
//                    BaseAppNode appNode = BaseAppNode.buildAppNode(identifier(), nodeInstance, buildProcessInfo);
                    BaseAppNode appNode = BaseAppNode.buildAppNode(workflowBody.identifier(), nodeInstance);
                    if (appNode.getBuildStatus() == BuildStatus.FAIL) {
                        buildProcessInfo.incrementFail();
                        buildProcessInfo.addNodeProcess(nodeInstance.getNodeKey(), appNode.getBuildException());
                        continue;
                    }
                    appNode.addNodeExecutionListener(nodeExecutionListeners);
                    tmpNodeCache.put(appNode.nodeKey(), appNode);
                    tmpStartNodes.add(appNode);
                    buildProcessInfo.incrementSuccess();
                } catch (Exception e) {
                    //TODO 构建失败异常返回至调用端
                    logger.error(e.getMessage(), e);
                    buildProcessInfo.incrementFail();
                    buildProcessInfo.addNodeProcess(nodeInstance.getNodeKey(), e);
                }

            }//end for

            startNodes = tmpStartNodes;

            HashSetValuedHashMap<String, String> tmpNextNodes = new HashSetValuedHashMap<>();
            for (NodeInstance node : workflowBody.getNodes()) {
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
                        BaseAppNode appNode = tmpNodeCache.get(nextNode.getNodeKey());
                        if (appNode == null) {
                            try {
                                appNode = BaseAppNode.buildAppNode(workflowBody.identifier(), nextNode);
                                appNode.addNodeExecutionListener(nodeExecutionListeners);
                                tmpNodeCache.put(appNode.nodeKey(), appNode);
                                if (appNode.getBuildException() != null) {
                                    buildProcessInfo.addNodeProcess(appNode.nodeKey(),appNode.getBuildException());
                                    //throw appNode.getBuildException();
                                }else{
                                    buildProcessInfo.incrementSuccess();
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                buildProcessInfo.incrementFail();
                                buildProcessInfo.addNodeProcess(nextNode.getNodeKey(), e);
                            }
                        }//end if
                        tmpNextNodes.put(node.getNodeKey(), nextNode.getNodeKey());
                    }//end for


                }//end if
            }//end for

            this.nodeCache = tmpNodeCache;
            this.nextNodeCache = tmpNextNodes;
            if (buildProcessInfo.getFailCount() == 0) {
                this.loadStatus = FlowStatus.LOADED;
            } else {
                this.loadStatus = FlowStatus.FAIL;
            }

            logger.info("flowId: {}, name:{}, loadStatus: {}, node size:{}", workflowBody.getWorkflowInstanceId(), workflowBody.getName(), loadStatus, nodeCache.size());
            buildProcessInfo.setStatus(loadStatus);
            ExpressionEngineFactory.clear(workflowBody.identifier());
            buildProcessInfo.setEndTime(System.currentTimeMillis());
        }
        return buildProcessInfo;
    }

    public List<BaseAppNode> startNodes() {
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

    public BaseAppNode findNode(String nodeKey) {
        return nodeCache.get(nodeKey);
    }

    public List<BaseAppNode> findNodes(List<String> nodeKeys) {
        List<BaseAppNode> nodes = new ArrayList<>();
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

    public void clear() {
        this.workflowBody = null;
    }

    public int nodeSize(){
        return this.nodeCache.size();
    }


    WorkflowBody getWorkflowInstance() {
        return workflowBody;
    }

    public BuildProcessInfo getBuildProcessInfo() {
        return buildProcessInfo;
    }
}
