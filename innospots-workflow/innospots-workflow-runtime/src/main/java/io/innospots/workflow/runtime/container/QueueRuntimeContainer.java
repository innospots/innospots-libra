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

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IQueueConnectionMinder;
import io.innospots.base.data.operator.IQueueReceiver;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.workflow.core.runtime.WorkflowRuntimeContext;
import io.innospots.workflow.core.runtime.FlowRuntimeRegistry;
import io.innospots.workflow.node.app.trigger.QueueTriggerNode;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 队列运行容器，按注册的trigger节点创建消费节点
 *
 * @author Smars
 * @date 2021/5/31
 */
public class QueueRuntimeContainer extends BaseRuntimeContainer {

    private static final Logger logger = LoggerFactory.getLogger(QueueRuntimeContainer.class);

    private final DataConnectionMinderManager dataConnectionMinderManager;

    private final ThreadTaskExecutor taskExecutor;

    /**
     * 已注册线程数量
     */
    private AtomicInteger runningThreadCounter = new AtomicInteger();
    /**
     * 运行状态
     */
    private boolean isRunning;
    /**
     * 线程池最大线程数量
     */
    private int maxReceiveNumber;

    private ArrayListValuedHashMap<String, QueueMsgReceiverThread> receiverCaches = new ArrayListValuedHashMap<>();

    public QueueRuntimeContainer(DataConnectionMinderManager dataConnectionMinderManager, int maxReceiveNumber) {
        taskExecutor = ThreadPoolBuilder.build(1, maxReceiveNumber, 0, QueueRuntimeContainer.class.getSimpleName());
        this.maxReceiveNumber = maxReceiveNumber;
        isRunning = true;
        this.dataConnectionMinderManager = dataConnectionMinderManager;
    }

    @Override
    protected void updateTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        if (runningThreadCounter.get() >= maxReceiveNumber) {
            logger.warn("receiver thread has reached max count:{}, trigger registration failed, {}", runningThreadCounter, flowRuntimeRegistry.key());
            return;
        }

        if (receiverCaches.containsKey(flowRuntimeRegistry.key())) {
            removeTrigger(flowRuntimeRegistry);
        }//end if

        super.updateTrigger(flowRuntimeRegistry);

        QueueTriggerNode queueNode = (QueueTriggerNode) flowRuntimeRegistry.getRegistryNode();
        int parallel = queueNode.parallelNumber();

        for (int i = 0; i < parallel; i++) {
            QueueMsgReceiverThread receiverThread = new QueueMsgReceiverThread(flowRuntimeRegistry);
            receiverCaches.put(flowRuntimeRegistry.key(), receiverThread);
            taskExecutor.execute(receiverThread);
            runningThreadCounter.incrementAndGet();
        }//end create queue consumer thread

    }


    @Override
    protected void removeTrigger(FlowRuntimeRegistry flowRuntimeRegistry) {
        super.removeTrigger(flowRuntimeRegistry);
        List<QueueMsgReceiverThread> receivers = receiverCaches.get(flowRuntimeRegistry.key());
        for (QueueMsgReceiverThread receiver : receivers) {
            receiver.close();
        }

        receiverCaches.remove(flowRuntimeRegistry.key());
    }

    @Override
    public void close() {
        isRunning = false;
        try {
            for (QueueMsgReceiverThread receiverThread : receiverCaches.values()) {
                receiverThread.close();
            }
            TimeUnit.SECONDS.sleep(3);
            taskExecutor.shutdown();
            logger.info("close queue runtime container.");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }


    private class QueueMsgReceiverThread implements Runnable {

        private final IQueueReceiver queueMsgReceiver;
        private final FlowRuntimeRegistry flowRuntimeRegistry;
        private boolean isRunning;
        private int errorCount = 0;
        private final String topic;


        public QueueMsgReceiverThread(FlowRuntimeRegistry flowRuntimeRegistry) {
            this.flowRuntimeRegistry = flowRuntimeRegistry;
            QueueTriggerNode triggerNode = (QueueTriggerNode) flowRuntimeRegistry.getRegistryNode();
            topic = triggerNode.topic();
            IQueueConnectionMinder dataConnectionMinder = (IQueueConnectionMinder) dataConnectionMinderManager.getMinder(triggerNode.credentialKey());
            queueMsgReceiver = dataConnectionMinder.queueReceiver(triggerNode.topic(),
                    triggerNode.topicGroup(),
                    triggerNode.dataOffset(),
                    triggerNode.messageFormat());
        }

        @Override
        public void run() {
            try {
                isRunning = true;
                logger.info("start queue message receiver: {}", topic);
                queueMsgReceiver.openSubscribe();
                while (this.isRunning && QueueRuntimeContainer.this.isRunning && errorCount < 20) {
                    process();
                }

                while (queueMsgReceiver.hasCache()) {
                    process();
                }//end while
                queueMsgReceiver.close();
                logger.info("closed queue receiver thread, topic: {}", topic);
            } finally {
                QueueRuntimeContainer.this.runningThreadCounter.decrementAndGet();
            }
        }//end run

        public void close() {
            if (isRunning) {
                logger.info("closing queue receiver thread, topic: {}, threadName:{}", topic, Thread.currentThread().getName());
            }
            isRunning = false;
        }

        private void process() {
            try {
                DataBody<Map<String, Object>> innospotResponse = queueMsgReceiver.receive();
                //logger.debug("queue msg receiver topic:{} {}",topic, innospotResponse);
                if (innospotResponse != null && innospotResponse.getBody() != null) {
                    WorkflowRuntimeContext workflowRuntimeContext = execute(flowRuntimeRegistry, innospotResponse.getBody());
                } else {
                    if (!queueMsgReceiver.hasCache()) {
                        logger.debug("queue msg receiver no data flow:{} topic:{} sleep:{} ms", flowRuntimeRegistry.key(), topic, 1000);
                        Thread.sleep(1000L, 0);
                    }

                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                errorCount++;
            }
        }
    }//end thread
}
