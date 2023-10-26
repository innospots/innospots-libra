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

package io.innospots.base.utils.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author Smars
 * @date 2021/3/15
 */
public class ThreadPoolBuilder {

    private static int processorNumber = Runtime.getRuntime().availableProcessors();

    public static final int DEFAULT_MAX_QUEUE_CAPACITY = 20000;
    private static int keepAliveSeconds = 60;

    public static ThreadTaskExecutor build(int coreSize, int maxSize, int queueCapacity, String poolName) {
        ThreadTaskExecutor threadTaskExecutor = new ThreadTaskExecutor(coreSize,maxSize,keepAliveSeconds, TimeUnit.SECONDS,
                createQueue(queueCapacity),
                new ThreadFactoryBuilder().setNameFormat(poolName + "-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
        threadTaskExecutor.setPoolName(poolName);
        return threadTaskExecutor;
    }

    public static ThreadTaskExecutor build(int coreSize, int maxSize, String poolName) {
        return build(coreSize, maxSize, DEFAULT_MAX_QUEUE_CAPACITY, poolName);
    }

    public static ThreadTaskExecutor build(int coreSize, String poolName) {
        return build(coreSize, coreSize, poolName);
    }

    public static ThreadTaskExecutor build(String poolName, int queueCapacity) {
        return build(processorNumber, processorNumber, queueCapacity, poolName);
    }

    protected static BlockingQueue<Runnable> createQueue(int queueCapacity) {
        return queueCapacity > 0 ? new LinkedBlockingQueue(queueCapacity) : new SynchronousQueue();
    }

}
