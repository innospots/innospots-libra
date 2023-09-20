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

import io.innospots.workflow.core.lisenter.WorkflowRuntimeListener;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;

import java.util.*;

/**
 * 策略运行容器管理器
 *
 * @author Smars
 * @date 2021/6/6
 */
public class RunTimeContainerManager {

    private WebhookRuntimeContainer webhookRuntimeContainer;

    private QueueRuntimeContainer queueRuntimeContainer;

    private ScheduleRuntimeContainer scheduleRuntimeContainer;

    private CycleTimerRuntimeContainer cycleTimerRuntimeContainer;

    private DummyRuntimeContainer dummyRuntimeContainer;

    public RunTimeContainerManager(WebhookRuntimeContainer webhookRuntimeContainer,
                                   CycleTimerRuntimeContainer cycleTimerRuntimeContainer,
                                   QueueRuntimeContainer queueRuntimeContainer,
                                   DummyRuntimeContainer dummyRuntimeContainer,
                                   ScheduleRuntimeContainer scheduleRuntimeContainer) {
        this.webhookRuntimeContainer = webhookRuntimeContainer;
        this.cycleTimerRuntimeContainer = cycleTimerRuntimeContainer;
        this.queueRuntimeContainer = queueRuntimeContainer;
        this.dummyRuntimeContainer = dummyRuntimeContainer;
        this.scheduleRuntimeContainer = scheduleRuntimeContainer;
    }

    /**
     * 注册事件触发器节点
     *
     * @param triggerNodes
     */
    public void registerEventTriggers(List<FlowRuntimeRegistry> triggerNodes) {
        webhookRuntimeContainer.register(triggerNodes);
    }

    /**
     * 注册调度任务执行节点
     *
     * @param triggerNodes
     */
    public void registerScheduleTriggers(List<FlowRuntimeRegistry> triggerNodes) {
        scheduleRuntimeContainer.register(triggerNodes);
    }


    public void registerQueueTriggers(List<FlowRuntimeRegistry> triggerNodes) {
        queueRuntimeContainer.register(triggerNodes);
    }

    public void registerDummyTriggers(List<FlowRuntimeRegistry> triggerNodes){
        dummyRuntimeContainer.register(triggerNodes);
    }

    public void registerCycleTimerTriggers(List<FlowRuntimeRegistry> triggerNodes){
        cycleTimerRuntimeContainer.register(triggerNodes);
    }

    public void addListener(Collection<WorkflowRuntimeListener> runtimeListeners) {
        for (WorkflowRuntimeListener runtimeListener : runtimeListeners) {
            webhookRuntimeContainer.addListener(runtimeListener);
        }
    }

    public void close() {
        if (webhookRuntimeContainer != null) {
            webhookRuntimeContainer.close();
        }
        if (scheduleRuntimeContainer != null) {
            scheduleRuntimeContainer.close();
        }
        if (queueRuntimeContainer != null) {
            queueRuntimeContainer.close();
        }
    }

    public Map<String, Object> runtimeTriggers() {
        Map<String, Object> triggers = new HashMap<>();
        List<Object> apis = new ArrayList<>();
        triggers.put("apis", apis);
        apis.addAll(webhookRuntimeContainer.runtimeFlowTriggers());
        List<Object> schedules = new ArrayList<>(scheduleRuntimeContainer.runtimeFlowTriggers());
        triggers.put("schedules", schedules);
        List<Object> queues = new ArrayList<>(queueRuntimeContainer.runtimeFlowTriggers());
        triggers.put("queues", queues);
        List<Object> dummies = new ArrayList<>(dummyRuntimeContainer.runtimeFlowTriggers());
        triggers.put("dummies",dummies);
        List<Object> cycles = new ArrayList<>(cycleTimerRuntimeContainer.runtimeFlowTriggers());
        triggers.put("cycles",cycles);
        return triggers;
    }
}
