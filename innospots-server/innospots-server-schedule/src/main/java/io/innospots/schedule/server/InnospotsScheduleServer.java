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

package io.innospots.schedule.server;


import io.innospots.schedule.ScheduleExecutorImporter;
import io.innospots.schedule.utils.ScheduleUtils;
import io.innospots.server.base.ServerConfigImporter;
import io.innospots.server.base.registry.ServiceRegistryHolder;
import io.innospots.server.base.registry.enums.ServiceType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;

/**
 * @author Raydian
 * @date 2020/12/14
 */
//@EnableDiscoveryClient
@SpringBootApplication(exclude = {QuartzAutoConfiguration.class,
//        HibernateJpaAutoConfiguration.class,
        FreeMarkerAutoConfiguration.class})
@ServerConfigImporter
@ScheduleExecutorImporter
public class InnospotsScheduleServer {

    public static void main(String[] args) {
        ServiceRegistryHolder.serviceType(ScheduleUtils.SCHEDULE_SERVICE_EXECUTOR);
        SpringApplication.run(InnospotsScheduleServer.class, args);
    }


}
