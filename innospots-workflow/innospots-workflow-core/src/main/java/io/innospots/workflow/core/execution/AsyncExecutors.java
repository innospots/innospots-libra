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

package io.innospots.workflow.core.execution;

import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;

/**
 * @author Smars
 * @date 2021/3/16
 */
public class AsyncExecutors {

    private static ThreadTaskExecutor threadPoolTaskExecutor;

    public static void initialize(int coreThreadSize, int queueCapacity) {
        if (threadPoolTaskExecutor == null) {
            threadPoolTaskExecutor = ThreadPoolBuilder.build(coreThreadSize, coreThreadSize, queueCapacity, "async-execution-pool");
        }
    }

    public static Future execute(Runnable runner) {
        if (threadPoolTaskExecutor != null) {
            return threadPoolTaskExecutor.submit(runner);
        }
        return null;
    }

    public static void close() {
        if (threadPoolTaskExecutor != null) {
            threadPoolTaskExecutor.shutdown();
            threadPoolTaskExecutor = null;
        }
    }

}
