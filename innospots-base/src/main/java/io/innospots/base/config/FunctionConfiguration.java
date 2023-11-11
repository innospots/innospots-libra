/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.base.config;

import io.innospots.base.function.definition.loader.FunctionDbLoader;
import io.innospots.base.function.definition.operator.FunctionDefinitionOperator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/11
 */
@Component
@Configuration
@EntityScan(basePackages = "io.innospots.base.function.definition.entity")
@MapperScan(basePackages = "io.innospots.base.function.definition.dao")
public class FunctionConfiguration {

    @Bean
    public FunctionDefinitionOperator functionDefinitionOperator() {
        return new FunctionDefinitionOperator();
    }

    @Bean
    public FunctionDbLoader functionDbLoader(FunctionDefinitionOperator functionDefinitionOperator) {
        return new FunctionDbLoader(functionDefinitionOperator);
    }
}
