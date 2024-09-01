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

package io.innospots.administration.server;

import io.innospots.administration.schedule.LoggerClearScheduler;
import io.innospots.app.console.ApplicationConsoleImporter;
import io.innospots.app.visitor.ApplicationVisitorImporter;
import io.innospots.libra.kernel.LibraKernelImporter;
import io.innospots.libra.security.LibraAuthImporter;
import io.innospots.schedule.ScheduleConsoleImporter;
import io.innospots.server.base.ServerConfigImporter;
import io.innospots.server.base.registry.ServiceRegistryHolder;
import io.innospots.server.base.registry.enums.ServiceType;
import io.innospots.workflow.console.WorkflowConsoleImporter;
import io.innospots.workflow.server.configuration.WorkflowServerImporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Raydian
 * @date 2020/12/14
 */
//@EnableDiscoveryClient
@SpringBootApplication(exclude = {QuartzAutoConfiguration.class,
//        HibernateJpaAutoConfiguration.class,
        FreeMarkerAutoConfiguration.class})
@Import(LoggerClearScheduler.class)
@LibraAuthImporter
@LibraKernelImporter
@ServerConfigImporter
@WorkflowConsoleImporter
@ApplicationConsoleImporter
@ApplicationVisitorImporter
@ScheduleConsoleImporter
@WorkflowServerImporter
@EnableScheduling
public class InnospotsAdministrationServer {

    public static void main(String[] args) {
        ServiceRegistryHolder.serviceType(ServiceType.ADMINISTRATION.name());
        SpringApplication.run(InnospotsAdministrationServer.class, args);
    }

}
