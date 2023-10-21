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

package io.innospots.workflow.runtime;


import io.innospots.workflow.runtime.config.WorkflowRuntimeConfiguration;
import io.innospots.workflow.runtime.flow.FlowManager;
import io.innospots.workflow.runtime.starter.WorkflowStarter;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//@WorkflowOperatorImporter
@Import({WorkflowRuntimeImporter.StreamFlowEngineConfiguration.class, WorkflowRuntimeConfiguration.class})
public @interface WorkflowRuntimeImporter {


    @Configuration
    class StreamFlowEngineConfiguration {

        @Bean
        public WorkflowStarter workflowStarter(FlowManager flowManager) {
            return new WorkflowStarter(flowManager);
        }

        /*
        @Bean
        public IWorkflowLoader workflowLoader(WorkflowBuilderOperator workFlowBuilderOperator) {
            return new DbWorkflowLoader(workFlowBuilderOperator);
        }
         */


        @Bean
        @ConditionalOnProperty(prefix = "innospots.config", name = "enable-swagger", havingValue = "true")
        public GroupedOpenApi workflowRuntimeOpenApi() {
            return GroupedOpenApi.builder().group("workflow-runtime")
                    .packagesToScan("io.innospots.workflow.runtime")
                    .pathsToMatch("/**")
                    .build();
        }

    }
}
