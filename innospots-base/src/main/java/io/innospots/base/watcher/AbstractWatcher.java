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

import io.innospots.base.utils.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Raydian
 * @date 2020/12/14
 */
public abstract class AbstractWatcher implements IWatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWatcher.class);

    protected int checkIntervalMills = 5000;
    protected boolean running;
    protected String name;
    protected long startTime;

    @Override
    public boolean check() {
        return true;
    }

    protected void prepare() {
        name = this.getClass().getSimpleName();
    }

    @Override
    public void run() {
        running = true;
        prepare();
        logger.info("Started watcher: {}", name);
        startTime = System.currentTimeMillis();
        int interval = checkIntervalMills;
        while (running) {
            try {
                if (check()) {
                    interval = execute();
                }
                if (interval <= 0) {
                    interval = checkIntervalMills;
                }
            } catch (Exception e) {
                logger.error("watcher execution error: {}：", e.getMessage(), e);
            } finally {
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
        String timeDuration = DateTimeUtils.consume(startTime);
        logger.info("Watcher {} is stopped, total running time:{}", name, timeDuration);
    }

    @Override
    public void stop() {
        this.running = false;
    }

}
