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


import io.innospots.base.watcher.WatcherSupervisor;
//import io.innospots.workflow.console.operator.execution.ScheduledNodeExecutionOperator;
//import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.loader.IWorkflowLoader;
import io.innospots.workflow.runtime.container.RunTimeContainerManager;
import io.innospots.workflow.runtime.flow.FlowManager;
import io.innospots.workflow.runtime.scheduled.ScheduledNodeExecutionWatcher;
import io.innospots.workflow.runtime.watcher.TriggerRegistryWatcher;
import io.innospots.workflow.runtime.watcher.WorkflowStatusWatcher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;

/**
 * @author Smars
 * @date 2021/6/11
 */
public class WatcherStarter implements ApplicationRunner {


    private ApplicationContext applicationContext;

    public WatcherStarter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        WatcherSupervisor watcherSupervisor = applicationContext.getBean(WatcherSupervisor.class);

        watcherSupervisor.registry(triggerRegistryWatcher());
        watcherSupervisor.registry(workflowStatusWatcher());
        watcherSupervisor.registry(scheduledNodeExecutionWatcher());

    }

    private TriggerRegistryWatcher triggerRegistryWatcher() {
        FlowManager flowManager = applicationContext.getBean(FlowManager.class);
        RunTimeContainerManager runTimeContainerManager = applicationContext.getBean(RunTimeContainerManager.class);
        return new TriggerRegistryWatcher(runTimeContainerManager, flowManager);
    }


    private WorkflowStatusWatcher workflowStatusWatcher() {
        IWorkflowLoader workflowLoader = applicationContext.getBean(IWorkflowLoader.class);
        FlowManager flowManager = applicationContext.getBean(FlowManager.class);
        return new WorkflowStatusWatcher(workflowLoader, flowManager);
    }

    private ScheduledNodeExecutionWatcher scheduledNodeExecutionWatcher() {
        IScheduledNodeExecutionOperator scheduledNodeExecutionOperator = applicationContext.getBean(IScheduledNodeExecutionOperator.class);
        return new ScheduledNodeExecutionWatcher(scheduledNodeExecutionOperator);
    }
}
