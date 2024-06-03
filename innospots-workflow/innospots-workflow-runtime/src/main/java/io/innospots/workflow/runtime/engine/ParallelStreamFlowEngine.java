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

import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.IFlowExecutionListener;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.executor.TriggerNode;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.manage.FlowManager;
import io.innospots.workflow.runtime.parallel.ExecutionUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2021/1/2
 */
@Slf4j
public class ParallelStreamFlowEngine extends BaseFlowEngine {

    public static final int MAX_TIMES = 1000;
    private ThreadTaskExecutor taskExecutor;
//    private CompletionService<NodeExecution> completionService;

    public ParallelStreamFlowEngine(List<IFlowExecutionListener> flowExecutionListeners, FlowManager flowManager) {
        super(flowExecutionListeners, flowManager);
//        completionService = new ExecutorCompletionService<>(ThreadPoolBuilder.build(100, "parallel-flow"));
        taskExecutor = ThreadPoolBuilder.build(100, "parallel-flow");
    }


    @Override
    protected void execute(Flow flow, FlowExecution flowExecution) {
        List<BaseNodeExecutor> nodeExecutors = null;
        Map<String, ExecutionUnit> executionUnits = new ConcurrentHashMap<>();
        if (CollectionUtils.isEmpty(flowExecution.getCurrentNodeKeys())) {
            nodeExecutors = flow.startNodes();
        } else {
            nodeExecutors = flow.findNodes(flowExecution.getCurrentNodeKeys());
        }
        if (nodeExecutors == null) {
            flowExecution.setStatus(ExecutionStatus.FAILED);
            flowExecution.setMessage("start node is null");
            return;
        }
        if (nodeExecutors.size() == 1) {
            ExecutionUnit executionUnit = ExecutionUnit.build(nodeExecutors.get(0), executionUnits, flowExecution, flow, taskExecutor);
            executionUnit.run(false);
        } else {
            for (BaseNodeExecutor nodeExecutor : nodeExecutors) {
                if (!flowExecution.shouldExecute(nodeExecutor.nodeKey())) {
                    continue;
                }
                ExecutionUnit executionUnit = ExecutionUnit.build(nodeExecutor, executionUnits, flowExecution, flow, taskExecutor);
                if (nodeExecutor instanceof TriggerNode) {
                    executionUnit.run(false);
                } else {
                    executionUnit.run(true);
                }
            }
        }
        TimeoutTask timeoutTask = new TimeoutTask(executionUnits, flowExecution);
        Thread timeoutThread = new Thread(timeoutTask);
        timeoutThread.start();

        boolean isDone;
        int times = 0;
        int count = 0;
        do {
            isDone = true;
            count = 0;
            for (ExecutionUnit unit : executionUnits.values()) {
                if (unit.unitStatus() == ExecutionStatus.READY) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }
                isDone = unit.isDone() && isDone;
                if (log.isDebugEnabled()) {
                    log.debug("unit output:{},{},{}", unit.nodeKey(), unit.unitStatus(), unit.isTaskDone());
                }
                count++;
            }
            if (flowExecution.shouldStopped()) {
                if (!isDone) {
                    List<ExecutionUnit> ll = executionUnits.values().stream()
                            .filter(unit -> !unit.unitStatus().isDone())
                            .collect(Collectors.toList());
                    log.warn("flow execution has been stopped, not all of node have executed completely,{}", ll);
                }
                break;
            }
            times++;
        } while ((!isDone || count < executionUnits.size()) && times < MAX_TIMES);
        timeoutThread.interrupt();
        if (log.isDebugEnabled()) {
            log.debug("times:{}, unitCount:{},size:{}, flow execution: {}", times, count, executionUnits.size(), flowExecution);
        }

    }

    private static final class TimeoutTask implements Runnable {

        private final Map<String, ExecutionUnit> executionUnits;
        private final FlowExecution flowExecution;
        private boolean running = false;

        public TimeoutTask(Map<String, ExecutionUnit> executionUnits, FlowExecution flowExecution) {
            this.executionUnits = executionUnits;
            this.flowExecution = flowExecution;
        }

        @SneakyThrows
        @Override
        public void run() {
            this.running = true;
            long startTime = System.currentTimeMillis();
            int totalCount = executionUnits.size();
            if (log.isDebugEnabled()) {
                log.debug("start timeout task:{}, unit size:{}", flowExecution.getFlowExecutionId(), totalCount);
            }
            int doneCount = 0;
            try {
                while (running && doneCount < totalCount) {
                    totalCount = 0;
                    doneCount = 0;
                    for (ExecutionUnit executionUnit : executionUnits.values()) {
                        totalCount++;
                        if (executionUnit.isTaskDone()) {
                            doneCount++;
                        }
                        if (executionUnit.isTimeout()) {
                            executionUnit.cancel();
                        }
                        TimeUnit.MILLISECONDS.sleep(10);
                    }//end for
                }//end while
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (log.isDebugEnabled()) {
                log.debug("execution unit:{}", executionUnits.keySet());
                String consume = DateTimeUtils.consume(startTime);
                log.debug("timeout task consume:{} unit done:{}, total:{}", consume, doneCount, totalCount);
            }
        }

        public void close() {
            this.running = false;
        }
    }


    @Override
    public FlowExecution run(Long workflowInstanceId, Integer revisionId, String targetNodeKey, List<Map<String, Object>> payloads) {
        return null;
    }
}
