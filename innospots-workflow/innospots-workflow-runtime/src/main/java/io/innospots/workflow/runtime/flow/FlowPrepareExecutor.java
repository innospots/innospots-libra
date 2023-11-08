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

import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/23
 */
public class FlowPrepareExecutor implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(FlowPrepareExecutor.class);

    private ThreadTaskExecutor threadPoolTaskExecutor;

    private Map<String, FlowAsyncPreparer> loaderCache = new HashMap<>();
    private FlowManager flowManager;

    public FlowPrepareExecutor(FlowManager flowManager) {
        this.flowManager = flowManager;
        threadPoolTaskExecutor = ThreadPoolBuilder.build("flow-load-executor", 2000);
    }


    public void asyncPrepare(@NotNull Flow flow) {
        if (loaderCache.containsKey(flow.key())) {
            return;
        }
        FlowAsyncPreparer flowAsyncPreparer = new FlowAsyncPreparer(flow, this);
        threadPoolTaskExecutor.execute(flowAsyncPreparer);
        loaderCache.put(flow.key(), flowAsyncPreparer);
    }

    void done(@NotNull Flow flow) {
        loaderCache.remove(flow.key());
        flowManager.cacheFlow(flow);
    }

    void clear(String flowKey) {
        FlowAsyncPreparer asyncLoader = loaderCache.remove(flowKey);
        if (asyncLoader != null) {
            asyncLoader.close();
        }
    }

    @Override
    public void close() throws IOException {
        logger.info("prepare shutdown flowLoadExecutor...");
        for (FlowAsyncPreparer loader : loaderCache.values()) {
            loader.close();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        threadPoolTaskExecutor.shutdown();
        logger.info("shutdown flowLoadExecutor completed.");
    }

    /**
     * loading flow in cache
     *
     * @param key
     * @return
     */
    Flow getCachedFlow(String key) {
        FlowAsyncPreparer preparer = this.loaderCache.get(key);
        if (preparer == null) {
            return null;
        }
        return preparer.flow();
    }


}
