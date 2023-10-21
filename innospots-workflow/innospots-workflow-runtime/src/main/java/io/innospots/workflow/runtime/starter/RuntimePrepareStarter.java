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

import io.innospots.base.utils.ApplicationContextUtils;
import io.innospots.workflow.core.config.InnospotWorkflowProperties;
import io.innospots.workflow.core.context.WorkflowRuntimeContext;
import io.innospots.workflow.core.lisenter.WorkflowRuntimeListener;
import io.innospots.workflow.core.utils.WorkflowUtils;
import io.innospots.workflow.runtime.container.RunTimeContainerManager;
import io.innospots.workflow.runtime.server.WorkflowWebhookServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.availability.ApplicationAvailability;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/24
 */
public class RuntimePrepareStarter implements ApplicationRunner {


    private static final Logger logger = LoggerFactory.getLogger(RuntimePrepareStarter.class);


    private RunTimeContainerManager runTimeContainerManager;
    private WorkflowWebhookServer workflowWebhookServer;
    private InnospotWorkflowProperties workflowProperties;
    private ApplicationAvailability applicationAvailability;

    public RuntimePrepareStarter(
            WorkflowWebhookServer workflowWebhookServer
            , RunTimeContainerManager runTimeContainerManager,
            InnospotWorkflowProperties workflowProperties,
            ApplicationAvailability applicationAvailability
    ) {
        this.runTimeContainerManager = runTimeContainerManager;
        this.workflowWebhookServer = workflowWebhookServer;
        this.workflowProperties = workflowProperties;
        this.applicationAvailability = applicationAvailability;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        WorkflowRuntimeContext.contextResourcePath = new File(workflowProperties.getExecutionStorePath()).getAbsolutePath();
        Map<String, WorkflowRuntimeListener> listeners =
                ApplicationContextUtils.getBeansOfType(WorkflowRuntimeListener.class);
        runTimeContainerManager.addListener(listeners.values());
        WorkflowUtils.initialize(workflowProperties);
        logger.info("server liveness state:{}", applicationAvailability.getLivenessState());
        logger.info("server readiness state:{}", applicationAvailability.getReadinessState());
        logger.info("workflow context file store path:{}", WorkflowRuntimeContext.contextResourcePath);
        logger.info("bind runtime context listener:{}", listeners.keySet());
        //workflowWebhookServer.start();
    }


    @PreDestroy
    public void close() {
        //workflowWebhookServer.stop();
    }

}
