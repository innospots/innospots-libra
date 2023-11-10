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

package io.innospots.libra.base.schema.config;


import io.innospots.base.connector.credential.IConnectionCredentialReader;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.schema.reader.CachedSchemaRegistryReader;
import io.innospots.base.data.operator.DataOperatorManager;
import io.innospots.libra.base.configuration.InnospotsConsoleProperties;
import io.innospots.libra.base.schema.operator.SchemaCategoryOperator;
import io.innospots.libra.base.schema.operator.SchemaFieldOperator;
import io.innospots.libra.base.schema.operator.SchemaRegistryOperator;
import io.innospots.libra.base.schema.reader.SchemaRegistryReader;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/4
 */
@Configuration
@EntityScan(basePackages = "io.innospots.libra.base.schema.entity")
@MapperScan(basePackages = "io.innospots.libra.base.schema.dao")
public class SchemaConfiguration {

    @Bean
    public CachedSchemaRegistryReader cachedSchemaRegistryReader(
            SchemaRegistryReader schemaRegistryReader,
            InnospotsConsoleProperties innospotsConsoleProperties) {
        return new CachedSchemaRegistryReader(schemaRegistryReader, innospotsConsoleProperties.getSchemaCacheTimeoutSecond());
    }

    @Bean
    public DataConnectionMinderManager dataConnectionMinderManager(
            IConnectionCredentialReader connectionCredentialReader,
            SchemaRegistryReader schemaRegistryReader,
            InnospotsConsoleProperties innospotsConsoleProperties) {
        return new DataConnectionMinderManager(connectionCredentialReader, schemaRegistryReader, innospotsConsoleProperties.getSchemaCacheTimeoutSecond());
    }

    @Bean
    public DataOperatorManager dataOperatorManager(DataConnectionMinderManager dataConnectionMinderManager) {
        return new DataOperatorManager(dataConnectionMinderManager);
    }

    @Bean
    public SchemaRegistryReader schemaRegistryReader(SchemaRegistryOperator schemaRegistryOperator, SchemaFieldOperator schemaFieldOperator) {
        return new SchemaRegistryReader(schemaRegistryOperator, schemaFieldOperator);
    }

    @Bean
    public SchemaRegistryOperator schemaRegistryOperator(SchemaFieldOperator schemaFieldOperator) {
        return new SchemaRegistryOperator(schemaFieldOperator);
    }

    @Bean
    public SchemaFieldOperator schemaFieldOperator() {
        return new SchemaFieldOperator();
    }

    @Bean
    public SchemaCategoryOperator schemaCategoryOperator(SchemaRegistryOperator schemaRegistryOperator) {
        return new SchemaCategoryOperator(schemaRegistryOperator);
    }

}
