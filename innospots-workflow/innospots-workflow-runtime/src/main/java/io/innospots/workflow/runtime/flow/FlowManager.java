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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.flow.BuildProcessInfo;
import io.innospots.workflow.core.flow.WorkflowBody;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.app.TriggerNode;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;
import io.innospots.workflow.node.app.StateNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * @author Smars
 * @date 2021/3/17
 */
public class FlowManager implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(FlowManager.class);

    private IWorkflowLoader workflowLoader;

    private List<INodeExecutionListener> nodeExecutionListeners;

    private Map<String, Flow> flowCache = new HashMap<>();

    private Cache<String, Flow> draftCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(3)).build();

    private FlowPrepareExecutor flowPrepareExecutor;

    private Map<String,String> flowKeyMap = new HashMap<>();


    public FlowManager(IWorkflowLoader workflowLoader) {
        this.workflowLoader = workflowLoader;
        flowPrepareExecutor = new FlowPrepareExecutor(this);
    }

    public Flow loadFlow(Long workflowInstanceId, Integer revision) {
        return loadFlow(workflowInstanceId, revision, false, true);
    }

    public Flow loadFlow(String flowKey){
        String key = flowKeyMap.get(flowKey);
        Flow flow = flowCache.get(key);
        if(flow == null){
            WorkflowBody workflowBody = workflowLoader.loadFlowInstance(flowKey);
            flow = load(workflowBody,true,false);
            if (flow.isLoaded()) {
                cacheFlow(flow);
            }
            flowKeyMap.put(flow.getFlowKey(),key);
        }
        return flow;
    }

    public Flow loadFlow(Long workflowInstanceId, Integer revision, boolean force, boolean async) {
        String key = key(workflowInstanceId, revision);
        Flow flow = null;
        if (revision != null && revision == 0) {
            flow = draftCache.getIfPresent(key);
        } else {
            flow = flowCache.get(key);
        }

        if (force || flow == null) {
            if (async) {
                flow = flowPrepareExecutor.getCachedFlow(key);
                if (flow != null) {
                    return flow;
                }
                logger.info("async loading flow: {}", key);
                WorkflowBody workflowBody = workflowLoader.loadFlowInstance(workflowInstanceId, revision);
                if (workflowBody != null) {
                    flow = load(workflowBody,force,true);
                } else {
                    logger.warn("flowInstance is null, instanceId:{}, revision:{}", workflowInstanceId, revision);
                }
            } else {
                logger.info("loading flow: {}", key);
                WorkflowBody workflowBody = workflowLoader.loadFlowInstance(workflowInstanceId, revision);
                flow = load(workflowBody,force,false);
                if (flow.isLoaded()) {
                    cacheFlow(flow);
                }
            }
        }

        if(flow!=null){
            flowKeyMap.put(flow.getFlowKey(),key);
        }

        return flow;
    }

    private Flow load(WorkflowBody workflowBody,boolean force,boolean async){
        Flow flow = new Flow(workflowBody, force);
        flow.setNodeExecutionListeners(this.nodeExecutionListeners);
        if(async){
            flowPrepareExecutor.asyncPrepare(flow);
        }else{;
            flow.setNodeExecutionListeners(this.nodeExecutionListeners);
            BuildProcessInfo buildProcessInfo = flow.prepare();
            logger.info("flow build stat:{}", buildProcessInfo);
        }
        return flow;
    }


    public void setNodeExecutionListeners(List<INodeExecutionListener> nodeExecutionListeners) {
        this.nodeExecutionListeners = nodeExecutionListeners;
    }

    public List<INodeExecutionListener> nodeExecutionListeners() {
        return nodeExecutionListeners;
    }

    /**
     * load flow used newest revision
     *
     * @param workflowInstanceId
     * @return
     */
    public Flow loadFlow(Long workflowInstanceId) {
        return loadFlow(workflowInstanceId, null);
    }

    public Flow findFlow(Long workFlowInstanceId, Integer revision) {
        return flowCache.get(key(workFlowInstanceId, revision));
    }

    public Flow findFlow(Long workFlowInstanceId) {
        return findFlow(workFlowInstanceId, null);
    }

    public boolean clear(Long workflowInstanceId, Integer revision) {
        flowPrepareExecutor.clear(key(workflowInstanceId, revision));
        Flow flow = flowCache.remove(key(workflowInstanceId, revision));
        return flow != null;
    }

    public boolean clear(Long workflowInstanceId) {
        return clear(workflowInstanceId, null);
    }

    public boolean clear(String key) {
        flowPrepareExecutor.clear(key);
        Flow flow = flowCache.remove(key);
        draftCache.invalidate(key);
        return flow != null;
    }


    public Set<String> cacheFlowKeys() {
        return flowCache.keySet();
    }


    private String key(Long workflowInstanceId, Integer revision) {
        return IWorkflowLoader.key(workflowInstanceId, revision);
    }


    @Override
    public void close() throws IOException {
        flowCache.clear();
        flowPrepareExecutor.close();
    }

    /**
     * cache flow that is compiled.
     *
     * @param flow
     */
    void cacheFlow(Flow flow) {
        if (flow.getWorkflowInstance().getRevision() == 0) {
            draftCache.put(flow.key(), flow);
        } else {
            flowCache.put(flow.key(), flow);
        }
    }

    public List<FlowRuntimeRegistry> currentFlowTriggers() {
        List<FlowRuntimeRegistry> triggerInfos = new ArrayList<>();
        for (Flow flow : this.flowCache.values()) {
            for (BaseAppNode startNode : flow.startNodes()) {
                FlowRuntimeRegistry flowRuntimeRegistry = new FlowRuntimeRegistry();
                flowRuntimeRegistry.setFlowKey(flow.getFlowKey());
                flowRuntimeRegistry.setFlowStatus(flow.getFlowStatus());
                flowRuntimeRegistry.setBuildInfo(flow.getBuildProcessInfo().detail());
                flowRuntimeRegistry.setWorkflowInstanceId(flow.getWorkflowInstanceId());
                flowRuntimeRegistry.setRevision(flow.getRevision());
                flowRuntimeRegistry.setUpdateTime(flow.getUpdatedTime());
                flowRuntimeRegistry.setRegistryNode(startNode);
                if (startNode instanceof TriggerNode) {
                    triggerInfos.add(flowRuntimeRegistry);
                    //logger.debug("flowTriggerInfo {}", flowTriggerInfo);
                }else if(startNode instanceof StateNode){
                    triggerInfos.add(flowRuntimeRegistry);
                }
            }

        }
        return triggerInfos;
    }
}
