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

package io.innospots.workflow.runtime.server;


import io.innospots.base.registry.ServiceRegistryHolder;
import io.innospots.base.registry.enums.ServiceType;
import io.innospots.connector.schema.AppSchemaImporter;
import io.innospots.workflow.console.WorkflowOperatorImporter;
import io.innospots.workflow.runtime.WorkflowRuntimeImporter;
import io.undertow.UndertowOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Raydian
 * @date 2020/12/14
 */
@SpringBootApplication(exclude = {QuartzAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FreeMarkerAutoConfiguration.class})
@WorkflowOperatorImporter
@WorkflowRuntimeImporter
@AppSchemaImporter
@EnableScheduling
public class InnospotsWorkflowServer {

    public static void main(String[] args) {
        ServiceRegistryHolder.serverType(ServiceType.WORKFLOW);
        SpringApplication.run(InnospotsWorkflowServer.class, args);
    }

    public UndertowServletWebServerFactory webServerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        //factory.addDeploymentInfoCustomizers(deploymentInfo -> deploymentI);

        factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_STATISTICS, true));
        return factory;
    }
}
