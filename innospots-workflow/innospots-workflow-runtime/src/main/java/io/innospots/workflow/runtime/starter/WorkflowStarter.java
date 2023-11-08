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

package io.innospots.workflow.runtime.starter;

import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.workflow.core.execution.AsyncExecutors;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.runtime.flow.FlowManager;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/18
 */
public class WorkflowStarter implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowStarter.class);

    private FlowManager flowManager;

    public WorkflowStarter(FlowManager flowManager) {
        this.flowManager = flowManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //register node execution listener to nodes
        Map<String, INodeExecutionListener> listenerMap = BeanContextAwareUtils.getBeansOfType(INodeExecutionListener.class);
        if (MapUtils.isNotEmpty(listenerMap)) {
            flowManager.setNodeExecutionListeners(new ArrayList<>(listenerMap.values()));
        }
        logger.info("bind node execution listener: {}", listenerMap);

        AsyncExecutors.initialize(Runtime.getRuntime().availableProcessors() * 2 + 2, 50000);
    }
}
