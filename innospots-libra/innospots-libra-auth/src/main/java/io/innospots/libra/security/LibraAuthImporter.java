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

package io.innospots.libra.security;

import io.innospots.base.constant.PathConstant;
import io.innospots.libra.base.model.swagger.SwaggerOpenApiBuilder;
import io.innospots.libra.base.terminal.TerminalInfoInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.*;


/**
 * @author castor_ling
 * @date 2021/3/12
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(LibraAuthImporter.InnospotsAuthConfiguration.class)
public @interface LibraAuthImporter {

    @ConditionalOnProperty(prefix = "innospots.security", name = "enabled", havingValue = "true")
    @Configuration
    @ComponentScan(basePackages = "io.innospots.libra.security")
    class InnospotsAuthConfiguration implements WebMvcConfigurer {

        @Bean
        @ConditionalOnProperty(prefix = "innospots.config", name = "enable-swagger", havingValue = "true")
        public GroupedOpenApi libraAuthGroupedOpenApi() {
            return SwaggerOpenApiBuilder.buildGroupedOpenApi("libra-auth", "io.innospots.libra.security");
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new TerminalInfoInterceptor()).addPathPatterns(PathConstant.ROOT_PATH + "**");
        }
    }
}