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
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.kernel.LibraKernelImporter;
import io.innospots.libra.security.LibraAuthImporter;
import io.innospots.server.base.ServerConfigImporter;
import io.innospots.server.base.registry.ServiceRegistryHolder;
import io.innospots.server.base.registry.enums.ServiceType;
import io.innospots.workflow.console.WorkflowConsoleImporter;
import io.innospots.workflow.runtime.WorkflowRuntimeImporter;
import io.innospots.workflow.server.configuration.WorkflowServerImporter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

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
//@WorkflowServerImporter
@EnableScheduling
public class InnospotsAdministrationServer {

    public static void main(String[] args) {
        ServiceRegistryHolder.serverType(ServiceType.ADMINISTRATION);
        SpringApplication.run(InnospotsAdministrationServer.class, args);
    }

}
