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

package io.innospots.workflow.core.config;

import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.operator.jdbc.JdbcExecutionContextOperator;
import io.innospots.workflow.core.execution.operator.jdbc.JdbcFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.jdbc.JdbcNodeExecutionOperator;
import io.innospots.workflow.core.execution.operator.jdbc.ScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.reader.FlowExecutionReader;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.innospots.workflow.core.flow.loader.WorkflowDBLoader;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceCacheDao;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceDao;
import io.innospots.workflow.core.instance.dao.WorkflowRevisionDao;
import io.innospots.workflow.core.instance.operator.EdgeOperator;
import io.innospots.workflow.core.instance.operator.NodeInstanceOperator;
import io.innospots.workflow.core.instance.operator.WorkflowBodyOperator;
import io.innospots.workflow.core.instance.operator.WorkflowDraftOperator;
import io.innospots.workflow.core.node.definition.dao.FlowNodeDefinitionDao;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties({InnospotsWorkflowProperties.class})
@MapperScan(basePackages = {
        "io.innospots.workflow.core.execution.dao",
        "io.innospots.workflow.core.node.definition.dao",
        "io.innospots.workflow.core.instance.dao"})
@EntityScan(basePackages = {
        "io.innospots.workflow.core.execution.entity",
        "io.innospots.workflow.core.node.definition.entity",
        "io.innospots.workflow.core.instance.entity"})
public class WorkflowCoreConfiguration {


    @Bean
    public IWorkflowLoader workflowInstanceLoader(WorkflowBodyOperator workflowBodyOperator){
        return new WorkflowDBLoader(workflowBodyOperator);
    }

    @Bean
    public IExecutionContextOperator executionContextOperator(IDataOperator dataOperator, InnospotsWorkflowProperties workflowProperties) {
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
    public WorkflowBodyOperator workflowBodyOperator(WorkflowRevisionDao workflowRevisionDao,
                                                     WorkflowInstanceDao workflowInstanceDao,
                                                     NodeInstanceOperator nodeInstanceOperator,
                                                     EdgeOperator edgeOperator) {
        return new WorkflowBodyOperator(workflowRevisionDao,workflowInstanceDao,nodeInstanceOperator,edgeOperator);
    }

    @Bean
    public WorkflowDraftOperator workflowDraftDbOperator(
            WorkflowInstanceCacheDao workflowInstanceCacheDao,
            WorkflowInstanceDao workflowInstanceDao,
            WorkflowRevisionDao workflowRevisionDao,
            NodeInstanceOperator nodeInstanceOperator, EdgeOperator edgeOperator) {
        return new WorkflowDraftOperator(workflowInstanceCacheDao, workflowInstanceDao, workflowRevisionDao,nodeInstanceOperator, edgeOperator);
    }

    @Bean
    public NodeInstanceOperator nodeInstanceOperator(FlowNodeDefinitionDao flowNodeDefinitionDao) {
        return new NodeInstanceOperator(flowNodeDefinitionDao);
    }

    @Bean
    public EdgeOperator edgeOperator() {
        return new EdgeOperator();
    }

    @Bean
    public FlowExecutionReader flowExecutionReader(
            IFlowExecutionOperator flowExecutionOperator,
            INodeExecutionOperator nodeExecutionOperator) {
        return new FlowExecutionReader(flowExecutionOperator, nodeExecutionOperator);
    }

}
