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

package io.innospots.connector.kafka.sender;

import io.innospots.base.data.operator.IQueueSender;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.time.DateTimeUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Alfred
 * @date 2021-08-07
 */
public class KafkaQueueSender implements IQueueSender {

    private Properties properties;

    private KafkaProducer<String, String> producer;

    public static final String KEY_FIELD = "key.field";

    private String keyField;

    public KafkaQueueSender(Properties props) {
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.properties = props;
        this.keyField = properties.getProperty(KEY_FIELD);
    }

    @Override
    public void openSender() {
        if (producer == null) {
            producer = new KafkaProducer<>(properties);
        }
    }

    @Override
    public Map<String, Object> send(String topic, Map<String, Object> body) {
        if (producer == null) {
            openSender();
        }
        Map<String, Object> resp = new HashMap<>(5);
        resp.put("topic", topic);
        long start = System.currentTimeMillis();
        if (keyField == null) {
            producer.send(new ProducerRecord<>(topic, null, JSONUtils.toJsonString(body)));
        } else {
            Object v = body.get(keyField);
            resp.put("key", v);
            if (v != null) {
                producer.send(new ProducerRecord<>(topic, String.valueOf(v), JSONUtils.toJsonString(body)));
            } else {
                producer.send(new ProducerRecord<>(topic, null, JSONUtils.toJsonString(body)));
            }
        }
        resp.put("consume", DateTimeUtils.consume(start));
        return resp;
    }

    @Override
    public Map<String, Object> send(String topic, List<Map<String, Object>> bodies) {
        Map<String, Object> resp = new HashMap<>(5);
        resp.put("topic", topic);
        long start = System.currentTimeMillis();
        for (Map<String, Object> body : bodies) {
            send(topic, body);
        }
        resp.put("consume", DateTimeUtils.consume(start));
        return resp;
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
