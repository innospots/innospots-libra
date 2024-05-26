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

import io.innospots.workflow.console.operator.WorkflowCategoryOperator;
import io.innospots.workflow.console.operator.execution.ExecutionManagerOperator;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator;
import io.innospots.workflow.console.operator.node.FlowNodeGroupOperator;
import io.innospots.workflow.console.operator.node.FlowTemplateOperator;
import io.innospots.workflow.core.config.WorkflowCoreConfiguration;
import io.innospots.workflow.core.execution.dao.ExecutionContextDao;
import io.innospots.workflow.core.execution.dao.FlowExecutionDao;
import io.innospots.workflow.core.execution.dao.NodeExecutionDao;
import io.innospots.workflow.core.execution.dao.ScheduledNodeExecutionDao;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import io.innospots.workflow.core.execution.reader.NodeExecutionReader;
import io.innospots.workflow.core.node.definition.dao.FlowNodeDefinitionDao;
import io.innospots.workflow.core.node.definition.dao.FlowNodeGroupDao;
import io.innospots.workflow.core.node.definition.dao.FlowNodeGroupNodeDao;
import io.innospots.workflow.core.node.definition.dao.FlowTemplateDao;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Configuration
@ComponentScan(basePackages = {"io.innospots.workflow.console.task"})
@EnableCaching
@Import({WorkflowCoreConfiguration.class})
public class WorkflowOperatorConfiguration {

    @Bean
    public FlowNodeGroupOperator nodeGroupOperator(FlowTemplateDao flowTemplateDao,FlowNodeGroupDao flowNodeGroupDao, FlowNodeGroupNodeDao flowNodeGroupNodeDao,
                                                   FlowNodeDefinitionDao flowNodeDefinitionDao) {
        return new FlowNodeGroupOperator(flowTemplateDao,flowNodeGroupDao, flowNodeGroupNodeDao, flowNodeDefinitionDao);
    }

    @Bean
    public ExecutionManagerOperator executionManagerOperator(ExecutionContextDao executionContextDao, FlowExecutionDao flowExecutionDao,
                                                             NodeExecutionDao nodeExecutionDao, ScheduledNodeExecutionDao scheduledNodeExecutionDao) {
        return new ExecutionManagerOperator(executionContextDao, flowExecutionDao, nodeExecutionDao, scheduledNodeExecutionDao);
    }


    @Bean
    public FlowNodeDefinitionOperator nodeDefinitionOperator(FlowNodeGroupDao flowNodeGroupDao, FlowNodeGroupNodeDao flowNodeGroupNodeDao) {
        return new FlowNodeDefinitionOperator(flowNodeGroupDao,flowNodeGroupNodeDao);
    }

    @Bean
    public FlowTemplateOperator workFlowTemplateOperator(FlowNodeGroupOperator flowNodeGroupOperator) {
        return new FlowTemplateOperator(flowNodeGroupOperator);
    }


    @Bean
    public WorkflowCategoryOperator workflowCategoryOperator(WorkflowInstanceOperator workflowInstanceOperator) {
        return new WorkflowCategoryOperator(workflowInstanceOperator);
    }

    @Bean
    public WorkflowInstanceOperator workflowInstanceOperator(FlowTemplateOperator flowTemplateOperator) {
        return new WorkflowInstanceOperator(flowTemplateOperator);
    }

    @Bean
    public NodeExecutionReader nodeExecutionDisplayReader(
            IWorkflowLoader workflowLoader,
            INodeExecutionOperator nodeExecutionOperator,
            IFlowExecutionOperator flowExecutionOperator
    ) {
        return new NodeExecutionReader(workflowLoader,nodeExecutionOperator, flowExecutionOperator);
    }


}
