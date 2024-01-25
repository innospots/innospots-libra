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

package io.innospots.app.console;

import io.innospots.app.console.operator.AppDefinitionCategoryOperator;
import io.innospots.app.console.operator.AppTemplateCategoryOperator;
import io.innospots.app.core.ApplicationCoreImporter;
import io.innospots.app.core.config.InnospotsAppProperties;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.app.core.operator.AppTemplateOperator;
import io.innospots.libra.base.model.swagger.SwaggerOpenApiBuilder;
import org.mybatis.spring.annotation.MapperScan;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ApplicationCoreImporter
@Import({ApplicationConsoleImporter.ApplicationConsoleConfiguration.class})
public @interface ApplicationConsoleImporter {


    @ComponentScan(basePackages = {"io.innospots.app.console"})
    @Configuration
    class ApplicationConsoleConfiguration {

        @Bean
        public AppDefinitionCategoryOperator appDefinitionCategoryOperator() {
            return new AppDefinitionCategoryOperator();
        }

        @Bean
        public AppTemplateCategoryOperator appTemplateCategoryOperator(){
            return new AppTemplateCategoryOperator();
        }

        @Bean
        @ConditionalOnProperty(prefix = "innospots.config", name = "enable-swagger", havingValue = "true")
        public GroupedOpenApi AppConsoleGroupedOpenApi() {
            return SwaggerOpenApiBuilder.buildGroupedOpenApi("app-console", "io.innospots.app.console");
        }

    }
}
