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

package io.innospots.base.connector.minder;


import io.innospots.base.data.body.PageBody;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.schema.model.SchemaCatalog;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.connector.schema.reader.ISchemaRegistryReader;
import io.innospots.base.data.operator.IOperator;

import java.util.List;
import java.util.Map;

/**
 * @author Raydian
 * @date 2021/1/31
 */
public interface IDataConnectionMinder {


    void initialize(ISchemaRegistryReader schemaRegistryReader, ConnectionCredential connectionCredential);

    ConnectionCredential connectionCredential();

    /**
     * establish connection
     */
    void open();

    /**
     * release connection
     */
    void close();


    /**
     * fetch database table schema，which include table field
     * registryCode is table name
     * @param registryCode
     * @return
     */
    SchemaRegistry schemaRegistryByCode(String registryCode);


    SchemaRegistry schemaRegistryById(String registryId);

    /**
     * fetch database table schema
     *
     * @param includeField
     * @return
     */
    List<SchemaRegistry> schemaRegistries(boolean includeField);

    List<SchemaCatalog> schemaCatalogs();

    String schemaName();

    <Operator extends IOperator> Operator buildOperator();

    /**
     * test data connection
     *
     * @param connectionCredential
     * @return
     */
    Object testConnect(ConnectionCredential connectionCredential);

    Object fetchSample(ConnectionCredential connectionCredential, String tableName);

    default PageBody<Map<String, Object>> fetchSamples(ConnectionCredential connectionCredential, Map<String,Object> config) {
        return null;
    }

}
