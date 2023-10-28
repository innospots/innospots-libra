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

package io.innospots.base.store;

import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Smars
 * @date 2021/3/15
 */
public class AsyncDataStore<E> implements IDataStore<E> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDataStore.class);

    private IDataStore<E> executionStore;

    private LinkedBlockingQueue<E> insertQueue;
    private LinkedBlockingQueue<E> updateQueue;

    private ThreadTaskExecutor threadTaskExecutor;

    private boolean isRunning;

    private int batchSize;

    private String poolName;


    public AsyncDataStore(IDataStore<E> dataStore, int storeThreadSize, int batchSize, String poolName) {
        this.executionStore = dataStore;
        this.poolName = poolName;
        threadTaskExecutor = ThreadPoolBuilder.build(storeThreadSize, poolName);
        insertQueue = new LinkedBlockingQueue<>(10000);

        updateQueue = new LinkedBlockingQueue<>(10000);
        this.batchSize = batchSize;
        this.isRunning = true;

        for (int i = 0; i < batchSize; i++) {
            threadTaskExecutor.execute(new ExecutionStoreThread());
        }
    }

    @Override
    public boolean insert(E execution) {
        return insertQueue.add(execution);
    }

    @Override
    public boolean insert(List<E> executions) {
        return insertQueue.addAll(executions);
    }

    @Override
    public boolean update(E execution) {
        return updateQueue.add(execution);
    }

    @Override
    public boolean update(List<E> executions) {
        return updateQueue.addAll(executions);
    }

    @Override
    public void close() {
        this.isRunning = false;
        //TODO should waiting queue data are been stored completely.
        threadTaskExecutor.shutdown();
    }


    private class ExecutionStoreThread implements Runnable {

        private List<E> insertCache = new ArrayList<>();
        private List<E> updateCache = new ArrayList<>();

        @Override
        public void run() {
            try {
                logger.debug("execution store started, poolName:{}, {}", poolName, Thread.currentThread().getName());
                while (AsyncDataStore.this.isRunning) {
                    boolean ins = insert();
                    boolean upt = update();
                    if (ins && upt) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                        }
                    }
                }//end while

                if (!insertCache.isEmpty()) {
                    executionStore.insert(insertCache);
                    insertCache.clear();
                }

                if (!updateCache.isEmpty()) {
                    executionStore.update(updateCache);
                    updateCache.clear();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        private boolean insert() {
            boolean flag = false;
            E execution = insertQueue.poll();
            if (execution != null) {
                insertCache.add(execution);
            } else {
                if (!insertCache.isEmpty()) {
                    executionStore.insert(insertCache);
                    insertCache.clear();
                } else {
                    flag = true;
                }
            }
            if (insertCache.size() > batchSize) {
                executionStore.insert(insertCache);
                insertCache.clear();
            }
            return flag;
        }

        private boolean update() {
            boolean flag = false;
            E execution = updateQueue.poll();
            if (execution != null) {
                updateCache.add(execution);
            } else {
                if (!updateCache.isEmpty()) {
                    executionStore.update(updateCache);
                    updateCache.clear();
                } else {
                    flag = true;
                }
            }
            if (updateCache.size() > batchSize) {
                executionStore.update(updateCache);
                updateCache.clear();
            }
            return flag;
        }

    }
}
