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

package io.innospots.workflow.runtime.config;


import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.base.quartz.QuartzScheduleManager;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.config.WorkflowCoreConfiguration;
import io.innospots.workflow.core.debug.FlowNodeDebugger;
import io.innospots.workflow.core.execution.listener.IFlowExecutionListener;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.reader.FlowExecutionReader;
import io.innospots.workflow.core.execution.reader.NodeExecutionReader;
import io.innospots.workflow.core.flow.manage.FlowManager;
import io.innospots.workflow.core.instance.operator.WorkflowDraftOperator;
import io.innospots.workflow.core.runtime.webhook.AppFormResponseBuilder;
import io.innospots.workflow.core.runtime.webhook.CompositeResponseBuilder;
import io.innospots.workflow.runtime.container.*;
import io.innospots.workflow.runtime.container.listener.WorkflowRuntimeEventListener;
import io.innospots.workflow.runtime.debugger.FlowNodeSimpleDebugger;
import io.innospots.workflow.runtime.endpoint.*;
import io.innospots.workflow.runtime.engine.CarrierFlowEngine;
import io.innospots.workflow.runtime.engine.ParallelStreamFlowEngine;
import io.innospots.workflow.core.engine.StreamFlowEngine;
import io.innospots.workflow.runtime.response.StreamResponseEmitter;
import io.innospots.workflow.runtime.scheduled.NodeExecutionEventListener;
import io.innospots.workflow.runtime.server.WorkflowWebhookServer;
import io.innospots.workflow.runtime.starter.RuntimePrepareStarter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author Smars
 * @date 2021/3/15
 */
@Configuration
@Import({WorkflowCoreConfiguration.class})
@EnableConfigurationProperties(WorkflowRuntimeProperties.class)
public class WorkflowRuntimeConfiguration {


    @Bean
    public ResponseMockEndpoint mockEndpoint(){
        return new ResponseMockEndpoint();
    }

    @Bean
    public SseEventEndpoint sseEventEndpoint() {
        return new SseEventEndpoint();
    }

    @Bean
    public WebhookRuntimeEndpoint webhookRuntimeEndpoint(WebhookRuntimeContainer webhookRuntimeContainer) {
        return new WebhookRuntimeEndpoint(webhookRuntimeContainer);
    }

    @Bean
    public WebhookTestEndpoint webhookTestEndpoint(FlowNodeDebugger flowNodeDebugger) {

        return new WebhookTestEndpoint(flowNodeDebugger);
    }

    @Bean
    public WorkflowManagementEndpoint managementEndpoint(FlowManager flowManager,
                                                         RunTimeContainerManager runTimeContainerManager,
                                                         QuartzScheduleManager quartzScheduleManager,
                                                         WorkflowRuntimeProperties serverProperties
    ) {
        return new WorkflowManagementEndpoint(flowManager, runTimeContainerManager, quartzScheduleManager, serverProperties);
    }

    @Bean
    public WorkflowStreamEndpoint workflowStreamEndpoint(StreamResponseEmitter streamResponseEmitter){
        return new WorkflowStreamEndpoint(streamResponseEmitter);
    }

    @Bean
    public NodeExecutionEventListener nodeExecutionEventListener(
            IFlowExecutionOperator flowExecutionOperator,
            INodeExecutionOperator nodeExecutionOperator,
            IScheduledNodeExecutionOperator scheduledNodeExecutionOperator
    ) {
        return new NodeExecutionEventListener(flowExecutionOperator, nodeExecutionOperator, scheduledNodeExecutionOperator);
    }

    @Bean("EVENTS_FlowEngine")
    public CarrierFlowEngine carrierFlowEngine(FlowManager flowManager, List<IFlowExecutionListener> flowExecutionListeners) {
        return new CarrierFlowEngine(flowExecutionListeners, flowManager);
    }

    @Bean
    public StreamFlowEngine streamFlowEngine(FlowManager flowManager, List<IFlowExecutionListener> flowExecutionListeners) {

        return new StreamFlowEngine(flowExecutionListeners, flowManager);
    }

    @Bean
    public ParallelStreamFlowEngine parallelStreamFlowEngine(List<IFlowExecutionListener> flowExecutionListeners, FlowManager flowManager) {

        return new ParallelStreamFlowEngine(flowExecutionListeners, flowManager);
    }

    @Bean
    public QueueRuntimeContainer queueRuntimeContainer(
            DataConnectionMinderManager dataConnectionMinderManager,
            WorkflowRuntimeProperties workflowRuntimeProperties
    ) {
        return new QueueRuntimeContainer(dataConnectionMinderManager, workflowRuntimeProperties.getQueueThreadCapacity());
    }


    @Bean
    public ScheduleRuntimeContainer scheduleRuntimeContainer(QuartzScheduleManager quartzScheduleManager) {
        return new ScheduleRuntimeContainer(quartzScheduleManager);
    }

    public CompositeResponseBuilder responseBuilder() {
        return new CompositeResponseBuilder(new AppFormResponseBuilder());
    }

    @Bean
    public WebhookRuntimeContainer webhookRuntimeContainer() {
        return new WebhookRuntimeContainer(responseBuilder());
    }

    @Bean
    public CycleTimerRuntimeContainer cycleTimerRuntimeContainer(WorkflowRuntimeProperties workflowRuntimeProperties) {
        return new CycleTimerRuntimeContainer(workflowRuntimeProperties.getMaxCycleFlow());
    }

    @Bean
    public DummyRuntimeContainer dummyRuntimeContainer(){
        return new DummyRuntimeContainer();
    }

    @Bean
    @ConditionalOnMissingBean
    public QuartzScheduleManager quartzScheduleManager() {
        QuartzScheduleManager quartzScheduleManager = new QuartzScheduleManager();
        quartzScheduleManager.startup();
        return quartzScheduleManager;
    }


    @Bean
    public FlowNodeDebugger nodeDebugger(NodeExecutionReader nodeExecutionReader,
                                         IFlowExecutionOperator flowExecutionOperator,
                                         WorkflowDraftOperator workflowDraftOperator
    ) {
        return new FlowNodeSimpleDebugger(workflowDraftOperator, nodeExecutionReader, flowExecutionOperator);
    }

    @Bean
    public WorkflowWebhookServer webhookServer(WorkflowRuntimeProperties eventProperties,
                                               ServerProperties serverProperties,
                                               WebhookRuntimeContainer webhookRuntimeContainer) {
        Integer port = eventProperties.getPort();
        if (port == null) {
            port = 10000 + serverProperties.getPort();
        }
        return new WorkflowWebhookServer(port, eventProperties.getHost(), webhookRuntimeContainer);
    }

    @Bean
    public RunTimeContainerManager runTimeContainerManager(
            WebhookRuntimeContainer webhookRuntimeContainer,
            QueueRuntimeContainer queueRuntimeContainer,
            ScheduleRuntimeContainer scheduleRuntimeContainer,
            DummyRuntimeContainer dummyRuntimeContainer,
            CycleTimerRuntimeContainer cycleTimerRuntimeContainer
    ) {
        return new RunTimeContainerManager(
                webhookRuntimeContainer, cycleTimerRuntimeContainer, queueRuntimeContainer,dummyRuntimeContainer, scheduleRuntimeContainer);
    }


    @Bean
    public WorkflowRuntimeEventListener workflowRuntimeEventListener(DummyRuntimeContainer dummyRuntimeContainer,WebhookRuntimeContainer webhookRuntimeContainer){
        return new WorkflowRuntimeEventListener(dummyRuntimeContainer,webhookRuntimeContainer);
    }

    @Bean
    public RuntimePrepareStarter runtimePrepareStarter(WorkflowWebhookServer workflowWebhookServer,
                                                       InnospotsWorkflowProperties workflowProperties,
                                                       RunTimeContainerManager runTimeContainerManager,
                                                       ApplicationAvailability applicationAvailability
    ) {

        return new RuntimePrepareStarter(
                workflowWebhookServer, runTimeContainerManager, workflowProperties, applicationAvailability);
    }

    @Bean
    public StreamResponseEmitter streamResponseEmitter(NodeExecutionReader nodeExecutionReader, FlowExecutionReader flowExecutionReader){
        return new StreamResponseEmitter(nodeExecutionReader,flowExecutionReader);
    }


}
