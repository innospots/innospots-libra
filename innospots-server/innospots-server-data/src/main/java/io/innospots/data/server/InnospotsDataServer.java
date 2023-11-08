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

package io.innospots.data.server;


import io.innospots.server.base.configuration.BaseServiceConfiguration;
import io.innospots.server.base.configuration.DatasourceConfiguration;
import io.innospots.server.base.registry.ServiceRegistryHolder;
import io.innospots.server.base.registry.enums.ServiceType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Raydian
 * @date 2021/1/30
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, QuartzAutoConfiguration.class})
@Import({DatasourceConfiguration.class, BaseServiceConfiguration.class})
public class InnospotsDataServer {

    public static void main(String[] args) {
        ServiceRegistryHolder.serverType(ServiceType.DATA);
        SpringApplication.run(InnospotsDataServer.class, args);
//        DataConnectionMinderManager.getMinderClasses();
    }

}
