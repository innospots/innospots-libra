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

package io.innospots.workflow.runtime.container;

import io.innospots.workflow.core.context.WorkflowRuntimeContext;
import io.innospots.workflow.core.engine.FlowEngineManager;
import io.innospots.workflow.core.engine.IFlowEngine;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.lisenter.WorkflowRuntimeListener;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;
import io.innospots.workflow.core.runtime.RuntimeContainer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Smars
 * @date 2021/3/10
 */
@Slf4j
public abstract class BaseRuntimeContainer implements RuntimeContainer {


    protected List<WorkflowRuntimeListener> runtimeListeners = new ArrayList<>();

    protected Map<String, FlowRuntimeRegistry> triggerNodeCache = new HashMap<>();


    protected WorkflowRuntimeContext execute(FlowRuntimeRegistry triggerInfo, Map<String, Object> payload) {
        return execute(triggerInfo, payload, null);
    }

    protected WorkflowRuntimeContext execute(FlowRuntimeRegistry triggerInfo, Map<String, Object> payload, Map<String, Object> context) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (payload != null) {
            items.add(payload);
        }
        return execute(triggerInfo, items, context);
    }

    protected WorkflowRuntimeContext execute(FlowRuntimeRegistry triggerInfo, List<Map<String, Object>> payloads, Map<String, Object> context) {
        WorkflowRuntimeContext workflowRuntimeContext = buildContext(triggerInfo, payloads, context);
        execute(workflowRuntimeContext);
        return workflowRuntimeContext;
    }


    protected void execute(WorkflowRuntimeContext runtimeContext) {
        start(runtimeContext);

        IFlowEngine flowEngine = FlowEngineManager.eventFlowEngine();

        flowEngine.execute(runtimeContext.getFlowExecution());

        end(runtimeContext);

    }

    protected WorkflowRuntimeContext buildContext(FlowRuntimeRegistry triggerInfo, Map<String, Object> payload, Map<String, Object> context) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(payload);
        return buildContext(triggerInfo, items, context);
    }

    protected WorkflowRuntimeContext buildContext(FlowRuntimeRegistry triggerInfo, List<Map<String, Object>> payloads, Map<String, Object> context) {
        log.debug("run trigger,{}:{} {}", triggerInfo.key(), triggerInfo, payloads);
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(
                triggerInfo.getWorkflowInstanceId(),
                triggerInfo.getRevision(), payloads);
        flowExecution.addContext(context);
        flowExecution.setSource(triggerInfo.getRegistryNode().nodeCode());
        WorkflowRuntimeContext workflowRuntimeContext = WorkflowRuntimeContext.build(flowExecution);
        workflowRuntimeContext.addContext(context);
        return workflowRuntimeContext;
    }


    public void addListener(WorkflowRuntimeListener runtimeListener) {
        if (runtimeListener != null) {
            this.runtimeListeners.add(runtimeListener);
        }
    }


    protected void start(WorkflowRuntimeContext workflowRuntimeContext) {
        for (WorkflowRuntimeListener runtimeListener : runtimeListeners) {
            runtimeListener.start(workflowRuntimeContext);
        }
    }

    protected void end(WorkflowRuntimeContext workflowRuntimeContext) {
        workflowRuntimeContext.fillResponse();
        for (WorkflowRuntimeListener runtimeListener : runtimeListeners) {
            runtimeListener.end(workflowRuntimeContext);
        }
    }

    protected void updateTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        log.info("update trigger,{}: {}", flowRuntimeRegistry.key(), flowRuntimeRegistry);
        triggerNodeCache.put(flowRuntimeRegistry.key(), flowRuntimeRegistry);
    }

    protected void removeTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        log.info("remove trigger,{}: {}", flowRuntimeRegistry.key(), flowRuntimeRegistry);
        triggerNodeCache.remove(flowRuntimeRegistry.key());
    }

    /**
     * 向执行容器中注册流程触发配置
     *
     * @param triggerNodes
     */
    @Override
    public void register(List<FlowRuntimeRegistry> triggerNodes) {
        Set<String> shouldRemovedTriggers = new HashSet<>(triggerNodeCache.keySet());
        List<FlowRuntimeRegistry> newTriggers = new ArrayList<>();
        for (FlowRuntimeRegistry triggerInfo : triggerNodes) {
            if (triggerNodeCache.containsKey(triggerInfo.key())) {
                FlowRuntimeRegistry cached = triggerNodeCache.get(triggerInfo.key());
                if (!cached.getRegistryNode().equals(triggerInfo.getRegistryNode())) {
                    updateTrigger(triggerInfo);
                }
                shouldRemovedTriggers.remove(triggerInfo.key());
            } else {
                newTriggers.add(triggerInfo);
            }
        }//end for

        //TODO 如何找到需要删除的触发器，没有节点的状态
        if (!shouldRemovedTriggers.isEmpty()) {
            for (String trigger : shouldRemovedTriggers) {
                removeTrigger(triggerNodeCache.get(trigger));
            }
        }

        if (!newTriggers.isEmpty()) {
            for (FlowRuntimeRegistry newTrigger : newTriggers) {
                updateTrigger(newTrigger);
                ;
            }
        }
    }

    public List<Map<String, Object>> runtimeFlowTriggers() {
        if (triggerNodeCache.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> infos = new ArrayList<>();
        for (FlowRuntimeRegistry runtimeRegistry : triggerNodeCache.values()) {
            infos.add(runtimeRegistry.info());
        }
        return infos;
    }
}
