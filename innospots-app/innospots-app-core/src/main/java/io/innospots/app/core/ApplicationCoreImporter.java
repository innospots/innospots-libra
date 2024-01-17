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

package io.innospots.app.core;

import io.innospots.app.core.config.InnospotsAppProperties;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.app.core.operator.AppTemplateOperator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * import application core
 *
 * @author Smars
 * @date 2024/01/17
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ApplicationCoreImporter.ApplicationCoreConfiguration.class})
public @interface ApplicationCoreImporter {


    @EnableConfigurationProperties({InnospotsAppProperties.class})
    @MapperScan(basePackages = {"io.innospots.app.core.dao"})
    @EntityScan(basePackages = {"io.innospots.app.core.entity"})
    @ComponentScan(basePackages = {"io.innospots.app.core"})
    @Configuration
    class ApplicationCoreConfiguration {

        @Bean
        public AppDefinitionOperator appDefinitionOperator(){
            return new AppDefinitionOperator();
        }

        @Bean
        public AppTemplateOperator appTemplateOperator(){
            return new AppTemplateOperator();
        }

    }
}
