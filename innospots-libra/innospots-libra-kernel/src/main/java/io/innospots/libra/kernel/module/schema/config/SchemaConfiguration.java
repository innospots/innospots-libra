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

package io.innospots.libra.kernel.module.schema.config;


import io.innospots.base.connector.credential.IConnectionCredentialReader;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.schema.reader.CachedSchemaRegistryReader;
import io.innospots.base.data.operator.DataOperatorManager;
import io.innospots.libra.base.configuration.InnospotsConfigProperties;
import io.innospots.libra.kernel.module.schema.dataset.DataframeExecutor;
import io.innospots.libra.kernel.module.schema.dataset.DatasetOperator;
import io.innospots.libra.kernel.module.schema.operator.SchemaRegistryOperator;
import io.innospots.libra.kernel.module.schema.reader.SchemaRegistryReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/4
 */
@Configuration
public class SchemaConfiguration {


    @Bean
    public DatasetOperator datasetOperator(SchemaRegistryOperator schemaRegistryOperator,
                                           IConnectionCredentialReader connectionCredentialReader) {
        return new DatasetOperator(schemaRegistryOperator, connectionCredentialReader);
    }

    /**
     * @Bean public HttpApiOperator httpApiOperator(SchemaRegistryOperator schemaRegistryOperator) {
     * return new HttpApiOperator(schemaRegistryOperator);
     * }
     */


    @Bean
    public CachedSchemaRegistryReader cachedSchemaRegistryReader(
            SchemaRegistryReader schemaRegistryReader,
            InnospotsConfigProperties innospotsConfigProperties) {
        return new CachedSchemaRegistryReader(schemaRegistryReader, innospotsConfigProperties.getSchemaCacheTimeoutSecond());
    }


    @Bean
    public DataConnectionMinderManager dataConnectionMinderManager(
            IConnectionCredentialReader connectionCredentialReader,
            SchemaRegistryReader schemaRegistryReader,
            InnospotsConfigProperties configProperties) {
        return new DataConnectionMinderManager(connectionCredentialReader, schemaRegistryReader, configProperties.getSchemaCacheTimeoutSecond());
    }

    @Bean
    public DataOperatorManager dataOperatorManager(DataConnectionMinderManager dataConnectionMinderManager) {
        return new DataOperatorManager(dataConnectionMinderManager);
    }

    @Bean
    public DataframeExecutor dataframeExecutor(IConnectionCredentialReader connectionCredentialReader,
                                               DataOperatorManager dataOperatorManager,
                                               DatasetOperator datasetOperator) {
        return new DataframeExecutor(connectionCredentialReader, dataOperatorManager, datasetOperator);
    }
}
