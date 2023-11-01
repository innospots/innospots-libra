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

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author Jegy
 */
@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springShopOpenAPI() {
//        Components components = new Components();
//
//        // 添加auth认证header
//        components.addSecuritySchemes("Authorization", new SecurityScheme().type(SecurityScheme.Type.HTTP).name("Authorization").in(SecurityScheme.In.HEADER).bearerFormat("JWT").scheme("bearer"));
//
//        // 添加全局header
//        components.addParameters("Authorization", new Parameter().in(SecurityScheme.In.HEADER.toString()).schema(new StringSchema()).name("Authorization"));
//        components.addHeaders("Authorization", new Header().description("Authorization").schema(new StringSchema()));

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Authorization",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY).scheme("bearer").bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER).name("Authorization")))
                .info(new Info().title("Innospots Api Doc")
                        .description("Innospots Api")
                        .version("Application Version: 1.0.0\n Spring Boot Version: " + SpringBootVersion.getVersion())
                        .license(new License().name("Apache 2.0").url("http://www.innospots.com")))
                .addSecurityItem(
                        new SecurityRequirement().addList("Authorization", Arrays.asList("read", "write")))
                .externalDocs(new ExternalDocumentation()
                        .description("Innospots Wiki Documentation")
                        .url("https://innospots.github.com/docs"));
    }
}