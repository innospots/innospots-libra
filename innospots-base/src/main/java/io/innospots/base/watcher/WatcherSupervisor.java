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

package io.innospots.base.watcher;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

/**
 * @author Raydian
 * @date 2020/12/14
 */
public class WatcherSupervisor {

    private static final Logger logger = LoggerFactory.getLogger(WatcherSupervisor.class);

    private ExecutorService executorService;

    private List<IWatcher> watchers;
    private int maxSize;

    public WatcherSupervisor(int maxSize) {
        this.maxSize = maxSize;
        watchers = new ArrayList<>(maxSize);
        start();
    }

    public void start() {
        if (executorService == null) {
            ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("watcher-supervisor-%d").build();
            executorService = new ThreadPoolExecutor(maxSize, maxSize,
                    300, TimeUnit.SECONDS,
                    new SynchronousQueue<>(), threadFactory);
        }
    }

    public void registry(IWatcher watcher) {
        logger.info("registry watcher:{}", watcher.getClass().getSimpleName());
        if (executorService != null) {
            executorService.submit(watcher);
            watchers.add(watcher);
        } else {
            logger.error("registry monitor fail，executor poll is null,please call start method to initialize.");
        }
    }


    public void close() {
        try {
            for (IWatcher monitor : watchers) {
                monitor.stop();
            }
            if (executorService != null) {
                TimeUnit.SECONDS.sleep(5);
                executorService.shutdown();
                executorService = null;
                watchers.clear();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
