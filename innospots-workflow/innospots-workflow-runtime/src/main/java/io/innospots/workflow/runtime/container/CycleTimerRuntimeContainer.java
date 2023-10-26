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

import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;
import io.innospots.workflow.node.app.trigger.CycleTimerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/20
 */
public class CycleTimerRuntimeContainer extends BaseRuntimeContainer {


    private static final Logger logger = LoggerFactory.getLogger(CycleTimerRuntimeContainer.class);

    private final ThreadPoolTaskExecutor taskExecutor;

    /**
     * running timer thread
     */
    private AtomicInteger runningCounter = new AtomicInteger();
    /**
     * 运行状态
     */
    private boolean isRunning;
    /**
     * 线程池最大线程数量
     */
    private int maxTimerNumber;

    private Map<String, CycleTimerThread> timerCaches = new HashMap<>();

    public CycleTimerRuntimeContainer(int maxTimerNumber) {
        taskExecutor = ThreadPoolBuilder.build(1, maxTimerNumber, 0, CycleTimerRuntimeContainer.class.getSimpleName());
        this.maxTimerNumber = maxTimerNumber;
        isRunning = true;
    }

    @Override
    protected void updateTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        if (runningCounter.get() >= maxTimerNumber) {
            logger.warn("timer thread has reached max count:{}, trigger registration failed, {}", runningCounter, flowRuntimeRegistry.key());
            return;
        }

        if (timerCaches.containsKey(flowRuntimeRegistry.key())) {
            removeTrigger(flowRuntimeRegistry);
        }//end if

        super.updateTrigger(flowRuntimeRegistry);

        CycleTimerThread cycleTimerThread = new CycleTimerThread(flowRuntimeRegistry);
        timerCaches.put(flowRuntimeRegistry.key(), cycleTimerThread);
        taskExecutor.execute(cycleTimerThread);
        runningCounter.incrementAndGet();
    }


    @Override
    protected void removeTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        super.removeTrigger(flowRuntimeRegistry);
        CycleTimerThread cycleTimerThread = timerCaches.get(flowRuntimeRegistry.key());
        cycleTimerThread.close();
        timerCaches.remove(flowRuntimeRegistry.key());
    }

    @Override
    public void close() {
        isRunning = false;
        try {
            for (CycleTimerThread receiverThread : timerCaches.values()) {
                receiverThread.close();
            }
            TimeUnit.SECONDS.sleep(3);
            taskExecutor.shutdown();
            logger.info("close cycle timer container.");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }


    private class CycleTimerThread implements Runnable {

        private final FlowRuntimeRegistry flowRuntimeRegistry;
        private boolean isRunning;
        private int errorCount = 0;
        private final int timeInterval;
        private final TimeUnit timeUnit;


        public CycleTimerThread(FlowRuntimeRegistry flowRuntimeRegistry) {
            this.flowRuntimeRegistry = flowRuntimeRegistry;
            CycleTimerNode timingNode = (CycleTimerNode) flowRuntimeRegistry.getRegistryNode();
            timeInterval = timingNode.getTimeInterval();
            timeUnit = timingNode.getTimeUnit();
        }

        @Override
        public void run() {
            isRunning = true;
            logger.info("start cycle timer : {}", flowRuntimeRegistry.key());

            while (this.isRunning && CycleTimerRuntimeContainer.this.isRunning && errorCount < 20) {
                process();
                try {
                    timeUnit.sleep(timeInterval);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                    errorCount++;
                }
            }
            logger.info("closed cycle timer thread: {}", flowRuntimeRegistry.key());
            CycleTimerRuntimeContainer.this.runningCounter.decrementAndGet();
        }//end run

        public void close() {
            if (isRunning) {
                logger.info("closing cycle timer: {}, threadName:{}", flowRuntimeRegistry.key(), Thread.currentThread().getName());
            }
            isRunning = false;
        }

        private void process() {
            try {
                execute(flowRuntimeRegistry, null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                errorCount++;
            }
        }
    }//end thread
}
