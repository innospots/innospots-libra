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

package io.innospots.base.data.minder;


import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.data.schema.SchemaCatalog;
import io.innospots.base.data.schema.SchemaField;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.data.schema.reader.ISchemaRegistryReader;
import io.innospots.base.model.PageBody;

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
     * test data connection
     *
     * @param connectionCredential
     * @return
     */
    Object test(ConnectionCredential connectionCredential);

    /**
     * release connection
     */
    void close();


    /**
     * fetch database table schema，which include table field
     *
     * @param registryCode
     * @return
     */
    SchemaRegistry schemaRegistry(String registryCode);

    SchemaRegistry schemaRegistry(Integer registryId);

    List<SchemaField> schemaRegistryFields(String tableName);

    /**
     * fetch database table schema
     *
     * @param includeField
     * @return
     */
    List<SchemaRegistry> schemaRegistries(boolean includeField);

    List<SchemaCatalog> schemaCatalogs();

    /**
     * connector name
     *
     * @return
     */
    String connector();

    default IOperator buildOperator() {
        return null;
    }

    default IOperator buildOperator(Map<String, Object> params) {
        return null;
    }

    default IOperator buildOperator(String... params) {
        return null;
    }

    /*
    default IDataOperator dataOperator() {
        return null;
    }

    default ISqlOperator sqlOperator() {
        return null;
    }

    default IQueueReceiver dataReceiver(String topic, String group, String offset, String format) {
        return dataReceiver(topic, group, offset, format, 0, 0);
    }

    default IQueueReceiver dataReceiver(String topic, String group, String offset, String format, long pollTimeout, int pollSize) {
        return null;
    }

    default IQueueSender dataSender() {
        return null;
    }

     */

    Object fetchSample(ConnectionCredential connectionCredential, String tableName);

    default PageBody<Map<String, Object>> fetchSamples(ConnectionCredential connectionCredential, SchemaRegistry schemaRegistry, int page, int size) {
        return null;
    }

}
