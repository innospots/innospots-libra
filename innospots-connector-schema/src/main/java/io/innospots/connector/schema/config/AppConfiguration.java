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

package io.innospots.connector.schema.config;

import io.innospots.base.configuration.InnospotConfigProperties;
import io.innospots.base.data.ap.ISqlOperatorPoint;
import io.innospots.base.data.minder.DataConnectionMinderManager;
import io.innospots.base.data.schema.reader.CachedConnectionCredentialReader;
import io.innospots.base.data.schema.reader.CachedSchemaRegistryReader;
import io.innospots.base.data.schema.reader.IConnectionCredentialReader;
import io.innospots.connector.schema.operator.*;
import io.innospots.connector.schema.reader.ConnectionCredentialReader;
import io.innospots.connector.schema.reader.SchemaRegistryReader;
import io.innospots.connector.schema.service.Oauth2CallbackService;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.base.operator.SystemTempCacheOperator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/4
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties({AuthProperties.class, InnospotConfigProperties.class})
@MapperScan(basePackages = {"io.innospots.connector.schema.dao"})
@EntityScan(basePackages = {"io.innospots.connector.schema.entity"})
public class AppConfiguration {

    @Bean
    public Oauth2CallbackService oauth2CallbackService(ConnectionCredentialReader connectionCredentialReader){
        return new Oauth2CallbackService(connectionCredentialReader);
    }

    @Bean
    public AppCredentialOperator appCredentialOperator(
            AuthProperties authProperties,
            SchemaRegistryOperator schemaRegistryOperator
    ) {
        return new AppCredentialOperator(authProperties, schemaRegistryOperator);
    }

    @Bean
    public SchemaRegistryOperator schemaRegistryOperator(SchemaFieldOperator schemaFieldOperator) {
        return new SchemaRegistryOperator(schemaFieldOperator);
    }

    @Bean
    public SchemaFieldOperator dataFieldOperator() {
        return new SchemaFieldOperator();
    }

    @Bean
    public DatasetOperator datasetOperator(SchemaRegistryOperator schemaRegistryOperator, AppCredentialOperator appCredentialOperator) {
        return new DatasetOperator(schemaRegistryOperator, appCredentialOperator);
    }

    @Bean
    public SchemaCategoryOperator schemaCategoryOperator(DatasetOperator datasetOperator) {
        return new SchemaCategoryOperator(datasetOperator);
    }

    /**
    @Bean
    public HttpApiOperator httpApiOperator(SchemaRegistryOperator schemaRegistryOperator) {
        return new HttpApiOperator(schemaRegistryOperator);
    }
     */

    @Bean
    public SchemaRegistryReader schemaRegistryReader(
            SchemaRegistryOperator schemaRegistryOperator,
            SchemaFieldOperator schemaFieldOperator
    ) {
        return new SchemaRegistryReader(schemaRegistryOperator, schemaFieldOperator);
    }

    @Bean
    public CachedSchemaRegistryReader cachedSchemaRegistryReader(
            SchemaRegistryReader schemaRegistryReader,
            InnospotConfigProperties innospotConfigProperties) {
        return new CachedSchemaRegistryReader(schemaRegistryReader, innospotConfigProperties.getSchemaCacheTimeoutSecond());
    }

    @Bean
    public IConnectionCredentialReader connectionCredentialReader(
            AppCredentialOperator appCredentialOperator,
            AuthProperties authProperties
    ) {
        return new ConnectionCredentialReader(appCredentialOperator, authProperties);
    }

    @Bean
    public CachedConnectionCredentialReader cachedConnectionCredentialReader(
            IConnectionCredentialReader connectionCredentialReader,
            InnospotConfigProperties connectionProperties) {
        return new CachedConnectionCredentialReader(connectionCredentialReader, connectionProperties.getSchemaCacheTimeoutSecond());
    }

    @Bean
    public DataConnectionMinderManager dataConnectionMinderManager(
            IConnectionCredentialReader connectionCredentialReader,
            SchemaRegistryReader schemaRegistryReader,
            InnospotConfigProperties connectionProperties) {

        return new DataConnectionMinderManager(connectionCredentialReader, schemaRegistryReader, connectionProperties.getSchemaCacheTimeoutSecond());
    }

    @Bean
    public DataOperatorManager dataOperatorManager(DataConnectionMinderManager dataConnectionMinderManager) {
        return new DataOperatorManager(dataConnectionMinderManager);
    }

    @Bean
    public DataframeExecutor dataframeExecutor(IConnectionCredentialReader connectionCredentialReader,
                                               ISqlOperatorPoint sqlOperator,
                                               DatasetOperator datasetOperator) {
        return new DataframeExecutor(connectionCredentialReader, sqlOperator, datasetOperator);
    }
}
