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

package io.innospots.workflow.runtime.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ResourceException;
import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.enums.FlowStatus;
import io.innospots.workflow.core.exception.FlowPrepareException;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.FlowExecutionTaskEvent;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.IFlowExecutionListener;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.flow.BuildProcessInfo;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.runtime.flow.Flow;
import io.innospots.workflow.runtime.flow.FlowManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Raydian
 * @date 2020/12/20
 */
public abstract class BaseFlowEngine implements IFlowEngine {

    private static final Logger logger = LoggerFactory.getLogger(BaseFlowEngine.class);

    protected List<IFlowExecutionListener> flowExecutionListeners;

    protected FlowManager flowManager;

    protected Cache<String, FlowExecution> flowExecutionCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    protected Cache<String,String> flowKeyCache = Caffeine.newBuilder().build();

    public BaseFlowEngine(List<IFlowExecutionListener> flowExecutionListeners, FlowManager flowManager) {
        this.flowExecutionListeners = flowExecutionListeners;
        this.flowManager = flowManager;
    }

    @Override
    public BuildProcessInfo prepare(Long flowInstanceId, Integer version, boolean force) throws FlowPrepareException {
        Flow flow = flowManager.loadFlow(flowInstanceId, version, force, false);

        return flow.getBuildProcessInfo();
    }

    public Flow flow(Long flowInstanceId, Integer version) {
        return flowManager.loadFlow(flowInstanceId, version);
    }

    @Override
    public void execute(FlowExecution flowExecution) {
        Flow flow = getFlow(flowExecution);

        startFlow(flow, flowExecution);
        try {
            if (flow.getFlowStatus() == FlowStatus.LOADED) {
                execute(flow, flowExecution);
            } else {
                failExecution(flow, flowExecution);
            }
        } catch (Exception e) {
            logger.error("flow execution fail!", e);
            flowExecution.setStatus(ExecutionStatus.FAILED);
        }
        completeFlow(flowExecution, false);
    }

    protected void failExecution(Flow flow, FlowExecution flowExecution) {
        BuildProcessInfo buildProcessInfo = flow.getBuildProcessInfo();
        logger.error("flow prepare failed, {}", buildProcessInfo);
        if (buildProcessInfo.getBuildException() != null) {
            flowExecution.setMessage(buildProcessInfo.errorMessage());
        } else {
            for (Map.Entry<String, Exception> exceptionEntry : buildProcessInfo.getErrorInfo().entrySet()) {
                NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution(exceptionEntry.getKey(), flowExecution);
                nodeExecution.setStartTime(LocalDateTime.now());
                nodeExecution.setEndTime(LocalDateTime.now());
                nodeExecution.setStatus(ExecutionStatus.FAILED);
                nodeExecution.setMessage(buildProcessInfo.getBuildMessage(exceptionEntry.getKey()));
                flowExecution.addNodeExecution(nodeExecution);
                if (CollectionUtils.isNotEmpty(flowManager.nodeExecutionListeners())) {
                    flowManager.nodeExecutionListeners().forEach(
                            listener -> listener.fail(nodeExecution)
                    );
                }
            }
        }
    }

    @Override
    public FlowExecution stop(String flowExecutionId) {
        FlowExecution flowExecution = flowExecutionCache.getIfPresent(flowExecutionId);
        if (flowExecution != null) {
            flowExecution.setStatus(ExecutionStatus.STOPPING);
        }
        return flowExecution;
    }

    @Override
    public FlowExecution stopByFlowKey(String flowKey) {
        return stop(flowKeyCache.getIfPresent(flowKey));
    }

    @Override
    public boolean continueExecute(FlowExecution flowExecution) {
        Flow flow = getFlow(flowExecution);
        if (!flow.isLoaded()) {
            return false;
        }

        try {
            BaseAppNode appNode = flow.findNode(flowExecution.getCurrentNodeKeys().get(0));
            flowExecution.setCurrentNodeKeys(appNode.nextNodeKeys());
            execute(flow, flowExecution);
        } catch (Exception e) {
            logger.error("flow execution fail!", e);
            flowExecution.setStatus(ExecutionStatus.FAILED);
        }
        completeFlow(flowExecution, true);
        return true;
    }

    protected abstract void execute(Flow flow, FlowExecution flowExecution);


    private void startFlow(Flow flow, FlowExecution flowExecution) {

        flowExecution.fillExecutionId(flow.getFlowKey());
        if (flow.getFlowStatus() == FlowStatus.LOADED) {
            flowExecution.setStatus(ExecutionStatus.RUNNING);
        } else if (flow.getFlowStatus() == FlowStatus.FAIL) {
            flowExecution.setStatus(ExecutionStatus.FAILED);
        } else {
            flowExecution.setStatus(ExecutionStatus.NOT_PREPARED);
        }

        if (StringUtils.isNotEmpty(flowExecution.getEndNodeKey())) {
            flowExecution.setShouldExecutes(flow.dependencyNodes(flowExecution.getEndNodeKey()));
        }

        if (flowExecutionListeners != null) {
            for (IFlowExecutionListener flowExecutionListener : flowExecutionListeners) {
                flowExecutionListener.start(flowExecution);
            }
        }

        flowExecutionCache.put(flowExecution.getFlowExecutionId(), flowExecution);
        flowKeyCache.put(flowExecution.getFlowKey(), flowExecution.getFlowExecutionId());
        EventBusCenter.async(FlowExecutionTaskEvent.build(flowExecution));
    }

    private void completeFlow(FlowExecution flowExecution, boolean isUpdate) {
        if (flowExecution.getStatus() != null && flowExecution.getStatus().isExecuting()) {
            flowExecution.setStatus(ExecutionStatus.COMPLETE);
        } else if (flowExecution.getStatus() != null && flowExecution.getStatus().isStopping()) {
            flowExecution.setStatus(ExecutionStatus.STOPPED);
        }
        flowExecution.setEndTime(LocalDateTime.now());
        if (flowExecutionListeners != null) {
            for (IFlowExecutionListener flowExecutionListener : flowExecutionListeners) {
                if (isUpdate) {
                    flowExecutionListener.update(flowExecution);
                } else {
                    flowExecutionListener.complete(flowExecution);
                }
            }
        }
        flowExecutionCache.invalidate(flowExecution.getFlowExecutionId());
        flowKeyCache.invalidate(flowExecution.getFlowKey());
    }

    private Flow getFlow(FlowExecution flowExecution) {
        Flow flow = flowManager.loadFlow(flowExecution.getFlowInstanceId(), flowExecution.getRevision());
        if (flow == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "flow not exist: " + flowExecution.getFlowInstanceId() + " ,revision: " + flowExecution.getRevision());
        }
        if (!flow.isLoaded()) {
            logger.warn("the flow is not loaded completed, {},{}", flow.getWorkflowInstanceId(), flow.getRevision());
            flowExecution.setStatus(ExecutionStatus.NOT_PREPARED);
        }
        flowExecution.setTotalCount(flow.nodeSize());
        return flow;
    }

    public void addFlowExecutionListener(IFlowExecutionListener flowExecutionListener) {
        if (this.flowExecutionListeners == null) {
            this.flowExecutionListeners = new ArrayList<>();
        }
        this.flowExecutionListeners.add(flowExecutionListener);
    }

}
