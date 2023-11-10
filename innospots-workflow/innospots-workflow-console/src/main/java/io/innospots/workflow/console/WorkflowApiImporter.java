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

package io.innospots.workflow.console;

import io.innospots.libra.base.model.swagger.SwaggerOpenApiBuilder;
import io.innospots.workflow.console.listener.NodeReferenceListener;
import io.innospots.workflow.console.listener.WorkflowPageListener;
import io.innospots.workflow.console.operator.instance.NodeInstanceOperator;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * import workflow controller api import
 *
 * @author Raydian
 * @date 2020/11/28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@WorkflowOperatorImporter
@Import({WorkflowApiImporter.WorkflowApiConfiguration.class})
public @interface WorkflowApiImporter {


    @ComponentScan(basePackages = {"io.innospots.workflow.console"})
    @Configuration
    class WorkflowApiConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "innospots.config", name = "enable-swagger", havingValue = "true")
        public GroupedOpenApi workflowConsoleOpenApi() {
            return SwaggerOpenApiBuilder.buildGroupedOpenApi("workflow-console", "io.innospots.workflow.console");
        }

        @Bean
        public NodeReferenceListener nodeReferenceListener(NodeInstanceOperator nodeInstanceOperator, FlowNodeDefinitionOperator nodeDefinitionOperator) {
            return new NodeReferenceListener(nodeInstanceOperator, nodeDefinitionOperator);
        }

        @Bean
        public WorkflowPageListener workflowPageListener(WorkflowInstanceOperator workflowInstanceOperator) {
            return new WorkflowPageListener(workflowInstanceOperator);
        }

    }
}
