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

package io.innospots.libra.kernel.configuration;


import io.innospots.base.constant.PathConstant;
import io.innospots.base.crypto.BCryptPasswordEncoder;
import io.innospots.base.crypto.PasswordEncoder;
import io.innospots.libra.base.configuration.LibraBaseConfiguration;
import io.innospots.libra.base.configuration.WebConfiguration;
import io.innospots.libra.base.model.swagger.SwaggerOpenApiBuilder;
import io.innospots.libra.kernel.interceptor.AccessPermissionInterceptor;
import io.innospots.libra.kernel.interceptor.PageAccessCheckInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Raydian
 * @date 2021/1/10
 */
@Configuration
@EntityScan(basePackages = {"io.innospots.libra.kernel.module.**.entity"})
@MapperScan(basePackages = "io.innospots.libra.kernel.module.**.dao")
@Import({LibraBaseConfiguration.class, WebConfiguration.class})
@EnableConfigurationProperties({InnospotsConsoleProperties.class})
public class LibraKernelConfiguration implements WebMvcConfigurer {


    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    @ConditionalOnProperty(prefix = "innospots.config", name = "enable-swagger", havingValue = "true")
    public GroupedOpenApi libraKernelOpenApi() {
        return SwaggerOpenApiBuilder.buildGroupedOpenApi("libra-kernel", "io.innospots.libra.kernel");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AccessPermissionInterceptor accessPermissionInterceptor = new AccessPermissionInterceptor();
        PageAccessCheckInterceptor pageAccessCheckInterceptor = new PageAccessCheckInterceptor(accessPermissionInterceptor);
        registry.addInterceptor(accessPermissionInterceptor).addPathPatterns(PathConstant.ROOT_PATH + "**");
        registry.addInterceptor(pageAccessCheckInterceptor).addPathPatterns(PathConstant.ROOT_PATH + "access/check");
    }
}