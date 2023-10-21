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

package io.innospots.connector.kafka.minder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.innospots.base.data.minder.BaseDataConnectionMinder;
import io.innospots.base.data.minder.IQueueConnectionMinder;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.operator.IQueueReceiver;
import io.innospots.base.data.operator.IQueueSender;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.data.schema.SchemaCatalog;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.exception.data.DataConnectionException;
import io.innospots.base.exception.data.DataSchemaException;
import io.innospots.base.model.DataBody;
import io.innospots.base.model.PageBody;
import io.innospots.connector.kafka.receiver.KafkaQueueReceiver;
import io.innospots.connector.kafka.sender.KafkaQueueSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.time.Duration;
import java.util.*;

/**
 * @author Alfred
 * @date 2021-07-06
 */
@Slf4j
public class KafkaDataConnectionMinder extends BaseDataConnectionMinder implements IQueueConnectionMinder {

    private AdminClient adminClient;

    private Properties properties;

    private KafkaQueueSender dataSender;

    private Cache<String, IQueueReceiver> receiverCache = Caffeine.newBuilder()
            .weakValues()
            .removalListener((RemovalListener<String, IQueueReceiver>) (s, dataReceiver, removalCause) -> {
                log.warn("receiver is expired, close the data receiver,key:{}", s);
                if (dataReceiver != null) {
                    dataReceiver.close();
                }
            })
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    @Override
    public void open() {
        if (this.properties != null) {
            return;
        }
        Properties properties = toProperties(connectionCredential);

        try {
            //this.adminClient = AdminClient.create(properties);
        } catch (Exception e) {
            throw DataConnectionException.buildException(this.getClass(), e, "Connection open failure");
        }
        this.properties = properties;
    }

    private Properties toProperties(ConnectionCredential connectionCredential) {
        String bootstrapServers = String.valueOf(connectionCredential.getConfig().get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));
        Properties properties = new Properties();
        properties.putAll(connectionCredential.getConfig());
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return properties;
    }

    @Override
    public Object test(ConnectionCredential connectionCredential) {
        String bootstrapServers = connectionCredential.v(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG);
        Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(properties)) {
            return adminClient != null;
        } catch (Exception e) {
            throw DataConnectionException.buildException(this.getClass(), e, "Connection open failure");
        }
    }

    @Override
    public void close() {
        try {
            if (this.dataSender != null) {
                dataSender.close();
                dataSender = null;
            }
            this.receiverCache.invalidateAll();
            if (this.adminClient == null) {
                return;
            }
            this.adminClient.close();
            this.adminClient = null;
        } catch (Exception e) {
            throw DataConnectionException.buildException(this.getClass(), e, "Connection open failure");
        }
    }


    @Override
    public List<SchemaCatalog> schemaCatalogs() {
        List<SchemaCatalog> tableInfos = new ArrayList<>();
        try {
            if (adminClient == null) {
                adminClient = AdminClient.create(properties);
            }
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            Set<String> topics = listTopicsResult.names().get();
            for (String topic : topics) {
                SchemaCatalog schemaCatalog = new SchemaCatalog();
                schemaCatalog.setName(topic);
                schemaCatalog.setCode(topic);
                schemaCatalog.setCredentialId(this.connectionCredential.getCredentialId());
                tableInfos.add(schemaCatalog);
            }
        } catch (Exception e) {
            throw DataSchemaException.buildException(this.getClass(), e, "Get schemaTable failure");
        }
        return tableInfos;
    }


    @Override
    public IOperator buildOperator() {
        return queueSender();
    }

    @Override
    public IQueueSender queueSender() {
        if (dataSender == null) {
            dataSender = new KafkaQueueSender(this.properties);
        }
        return dataSender;
    }

    @Override
    public IOperator buildOperator(String... params) {
        return queueReceiver(params[0], params[1], params[2], params[3], Long.parseLong(params[4]), Integer.parseInt(params[5]));
    }

    @Override
    public IQueueReceiver queueReceiver(String topic, String group, String offset, String format, long pollTimeout, int pollSize) {
        String key = String.join("_", topic, group, offset);
        IQueueReceiver dataReceiver = receiverCache.getIfPresent(key);
        if (dataReceiver == null) {
            dataReceiver = new KafkaQueueReceiver(properties, topic, group, offset, format, pollSize, pollTimeout);
            dataReceiver.openSubscribe();
            receiverCache.put(key, dataReceiver);
        }
        return dataReceiver;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        Properties properties = toProperties(connectionCredential);
        String offset = "latest";
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);

        String group = "test-fetch-sample";
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        String key = String.join("_", tableName, group, offset);

        IQueueReceiver dataReceiver = receiverCache.getIfPresent(key);
        if (dataReceiver == null) {
            dataReceiver = new KafkaQueueReceiver(properties, 100);
            receiverCache.put(key, dataReceiver);
        }

        dataReceiver.assign(tableName, group, 2000L, null);
        DataBody<Map<String, Object>> innospotResponse = dataReceiver.receiveLastData();
        return innospotResponse.getBody();
    }

    @Override
    public PageBody<Map<String, Object>> fetchSamples(ConnectionCredential connectionCredential, SchemaRegistry schemaRegistry, int page, int size) {
        if (MapUtils.isEmpty(schemaRegistry.getConfigs())) {
            throw DataSchemaException.buildException(this.getClass(), "kafka schemaRegistry configs can not be empty");
        }
        String topic = String.valueOf(schemaRegistry.getConfigs().get("topic"));
        Properties properties = toProperties(connectionCredential);
        String group = "test-fetch-sample";
        String offset = "earliest";
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);

        String key = String.join("_", topic, group, offset);
        IQueueReceiver dataReceiver = receiverCache.getIfPresent(key);

        if (dataReceiver == null) {
            dataReceiver = new KafkaQueueReceiver(properties, 100);
            receiverCache.put(key, dataReceiver);
        }
        dataReceiver.assign(topic, group, 2000L, 0L);

        return dataReceiver.receive(size);
    }

    @Override
    public String connector() {
        return "kafka_queue";
    }
}
