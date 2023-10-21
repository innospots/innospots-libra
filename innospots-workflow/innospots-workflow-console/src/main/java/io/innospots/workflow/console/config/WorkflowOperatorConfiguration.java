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

package io.innospots.workflow.console.config;

import io.innospots.base.configuration.DatasourceConfiguration;
import io.innospots.base.configuration.InnospotConfigProperties;
import io.innospots.base.data.minder.DataConnectionMinderManager;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.re.GenericExpressionEngine;
import io.innospots.libra.base.configuration.LibraBaseConfiguration;
import io.innospots.workflow.console.dao.apps.AppNodeDefinitionDao;
import io.innospots.workflow.console.dao.apps.AppNodeGroupDao;
import io.innospots.workflow.console.dao.apps.AppNodeGroupNodeDao;
import io.innospots.workflow.console.dao.execution.ExecutionContextDao;
import io.innospots.workflow.console.dao.execution.FlowExecutionDao;
import io.innospots.workflow.console.dao.execution.NodeExecutionDao;
import io.innospots.workflow.console.dao.execution.ScheduledNodeExecutionDao;
import io.innospots.workflow.console.dao.instance.WorkflowInstanceCacheDao;
import io.innospots.workflow.console.dao.instance.WorkflowRevisionDao;
import io.innospots.workflow.console.loader.WorkflowDBLoader;
import io.innospots.workflow.console.operator.WorkflowCategoryOperator;
import io.innospots.workflow.console.operator.apps.AppCategoryOperator;
import io.innospots.workflow.console.operator.apps.AppFlowTemplateOperator;
import io.innospots.workflow.console.operator.apps.AppNodeDefinitionOperator;
import io.innospots.workflow.console.operator.apps.AppNodeGroupOperator;
import io.innospots.workflow.console.operator.execution.*;
import io.innospots.workflow.console.operator.instance.EdgeOperator;
import io.innospots.workflow.console.operator.instance.NodeInstanceOperator;
import io.innospots.workflow.console.operator.instance.WorkflowBuilderOperator;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.workflow.core.config.InnospotWorkflowProperties;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.reader.FlowExecutionReader;
import io.innospots.workflow.core.execution.reader.NodeExecutionReader;
import io.innospots.workflow.core.execution.store.FlowExecutionStoreListener;
import io.innospots.workflow.core.execution.store.NodeExecutionStoreListener;
import io.innospots.workflow.core.flow.instance.IWorkflowCacheDraftOperator;
import io.innospots.workflow.core.loader.IWorkflowLoader;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Configuration
@ComponentScan(basePackages = {"io.innospots.workflow.console.listener","io.innospots.workflow.console.task"})
@EnableCaching
@EnableConfigurationProperties({InnospotConfigProperties.class, InnospotWorkflowProperties.class})
@Import({DatasourceConfiguration.class, LibraBaseConfiguration.class})
@MapperScan(basePackages = {
        "io.innospots.workflow.console.dao.apps",
        "io.innospots.workflow.console.dao.execution",
        "io.innospots.workflow.console.dao.instance"})
@EntityScan(basePackages = {
        "io.innospots.workflow.console.entity.apps",
        "io.innospots.workflow.console.entity.execution",
        "io.innospots.workflow.console.entity.instance"})
public class WorkflowOperatorConfiguration {


    private final InnospotConfigProperties configProperties;

    public WorkflowOperatorConfiguration(InnospotConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @PostConstruct
    public void buildPath() {
        GenericExpressionEngine.setPath(configProperties.getScriptBuildPath() + File.separator + "src", configProperties.getScriptBuildPath());
    }


    @Bean
    public AppNodeGroupOperator nodeGroupOperator(AppNodeGroupDao appNodeGroupDao, AppNodeGroupNodeDao appNodeGroupNodeDao,
                                                  AppNodeDefinitionDao appNodeDefinitionDao) {
        return new AppNodeGroupOperator(appNodeGroupDao, appNodeGroupNodeDao, appNodeDefinitionDao);
    }

    @Bean
    public ExecutionManagerOperator executionManagerOperator(ExecutionContextDao executionContextDao, FlowExecutionDao flowExecutionDao,
                                                             NodeExecutionDao nodeExecutionDao, ScheduledNodeExecutionDao scheduledNodeExecutionDao) {
        return new ExecutionManagerOperator(executionContextDao, flowExecutionDao, nodeExecutionDao, scheduledNodeExecutionDao);
    }

    @Bean
    public AppCategoryOperator appCategoryOperator() {
        return new AppCategoryOperator();
    }


    @Bean
    public AppNodeDefinitionOperator nodeDefinitionOperator() {
        return new AppNodeDefinitionOperator();
    }

    @Bean
    public AppFlowTemplateOperator workFlowTemplateOperator(AppNodeGroupOperator appNodeGroupOperator) {
        return new AppFlowTemplateOperator(appNodeGroupOperator);
    }

    @Bean
    public EdgeOperator edgeOperator() {
        return new EdgeOperator();
    }

    @Bean
    public NodeInstanceOperator nodeInstanceOperator(AppNodeDefinitionOperator appNodeDefinitionOperator) {
        return new NodeInstanceOperator(appNodeDefinitionOperator);
    }

    @Bean
    public WorkflowCategoryOperator workflowCategoryOperator(WorkflowInstanceOperator workflowInstanceOperator) {
        return new WorkflowCategoryOperator(workflowInstanceOperator);
    }

    @Bean
    public WorkflowInstanceOperator workflowInstanceOperator(AppFlowTemplateOperator appFlowTemplateOperator) {
        return new WorkflowInstanceOperator(appFlowTemplateOperator);
    }

    @Bean
    public IWorkflowLoader workflowInstanceLoader(WorkflowBuilderOperator workflowBuilderOperator){
        return new WorkflowDBLoader(workflowBuilderOperator);
    }

    @Bean
    public WorkflowBuilderOperator workflowBuilderOperator(WorkflowRevisionDao workflowRevisionDao,
                                                           WorkflowInstanceCacheDao instanceCacheDao,
                                                           WorkflowInstanceOperator workflowInstanceOperator,
                                                           NodeInstanceOperator nodeInstanceOperator, EdgeOperator edgeOperator,
                                                           InnospotWorkflowProperties innospotWorkflowProperties) {
        return new WorkflowBuilderOperator(workflowRevisionDao, instanceCacheDao, workflowInstanceOperator, nodeInstanceOperator, edgeOperator, innospotWorkflowProperties);
    }

    @Bean
    public IExecutionContextOperator executionContextOperator(IDataOperator dataOperator, InnospotWorkflowProperties workflowProperties) {
        return new JdbcExecutionContextOperator(dataOperator, workflowProperties.getExecutionStorePath());
    }

    @Bean
    public IFlowExecutionOperator flowExecutionOperator(IDataOperator dataOperator, IExecutionContextOperator executionContextOperator) {
        return new JdbcFlowExecutionOperator(dataOperator, executionContextOperator);
    }

    @Bean
    public INodeExecutionOperator nodeExecutionOperator(IDataOperator dataOperator, IExecutionContextOperator executionContextOperator) {
        return new JdbcNodeExecutionOperator(dataOperator, executionContextOperator);
    }

    @Bean
    public IScheduledNodeExecutionOperator ScheduledNodeExecutionOperator(DataConnectionMinderManager dataConnectionMinderManager,
                                                                          IDataOperator defaultDataOperator) {
        return new ScheduledNodeExecutionOperator(dataConnectionMinderManager, defaultDataOperator);
    }


    @Bean
    public NodeExecutionReader nodeExecutionDisplayReader(
            IWorkflowCacheDraftOperator workflowCacheDraftOperator,
            INodeExecutionOperator nodeExecutionOperator,
            IFlowExecutionOperator flowExecutionOperator
    ) {
        return new NodeExecutionReader(workflowCacheDraftOperator,nodeExecutionOperator, flowExecutionOperator);
    }

    @Bean
    public FlowExecutionReader flowExecutionReader(IFlowExecutionOperator IFlowExecutionOperator, INodeExecutionOperator INodeExecutionOperator) {
        return new FlowExecutionReader(IFlowExecutionOperator, INodeExecutionOperator);
    }

    @Bean
    public FlowExecutionStoreListener flowExecutionStoreListener(IFlowExecutionOperator flowExecutionOperator) {
        return new FlowExecutionStoreListener(flowExecutionOperator);
    }

    @Bean
    public NodeExecutionStoreListener nodeExecutionStoreListener(INodeExecutionOperator nodeExecutionOperator) {
        return new NodeExecutionStoreListener(nodeExecutionOperator);
    }

}
