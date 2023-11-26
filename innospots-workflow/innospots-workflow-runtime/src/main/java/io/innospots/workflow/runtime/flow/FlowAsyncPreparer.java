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

import io.innospots.base.utils.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * @author Smars
 * @date 2021/3/23
 */
public class FlowAsyncPreparer implements Runnable, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(FlowAsyncPreparer.class);
    private final Flow flow;

    private final FlowPrepareExecutor loadExecutor;


    public FlowAsyncPreparer(Flow flow, FlowPrepareExecutor flowPrepareExecutor) {
        this.flow = flow;
        this.loadExecutor = flowPrepareExecutor;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            flow.prepare();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            loadExecutor.done(flow);
            logger.info("build stat:{}", flow.getBuildProcessInfo());
            //flow.clear();
        }
        logger.debug("flow loaded,key:{}, flow status:{}, consume: {}", flow.key(), flow.getStatus(), DateTimeUtils.consume(startTime));

    }

    @Override
    public void close() {
    }

    public Flow flow() {
        return flow;
    }
}
