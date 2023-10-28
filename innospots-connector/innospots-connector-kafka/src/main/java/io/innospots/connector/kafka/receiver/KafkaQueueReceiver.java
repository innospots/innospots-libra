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

package io.innospots.connector.kafka.receiver;

import io.innospots.base.data.operator.IQueueReceiver;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/3/23
 */
@Slf4j
public class KafkaQueueReceiver implements IQueueReceiver {


    private Properties properties;

    private KafkaConsumer<String, String> kafkaConsumer;

    private String topic;

    private boolean autoCommit = true;

    private Queue<Map<String, Object>> cacheQueue = new LinkedBlockingQueue<>();

    private MessageFormat messageFormat = MessageFormat.JSON;

    private Long pollTimeOut = 1000L;

    private String group;

    private String offset;


    public KafkaQueueReceiver(Properties props,
                              String topic,
                              String group,
                              String offset,
                              String format,
                              int maxPollSize) {
        this(props, maxPollSize);
        this.topic = topic;
        this.group = group;
        this.offset = offset;
        if (format != null) {
            messageFormat = MessageFormat.valueOf(format);
        }
    }

    public KafkaQueueReceiver(Properties props,
                              String topic,
                              String group,
                              String offset,
                              String format,
                              int maxPollSize, long pollTimeOut) {
        this(props, topic, group, offset, format, maxPollSize);
        this.pollTimeOut = pollTimeOut;
    }

    public KafkaQueueReceiver(Properties props, int maxPollSize) {
        this.properties = new Properties();
        this.properties.putAll(props);

        if (!properties.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group_receiver");
        }
//        if (!properties.containsKey(ConsumerConfig.CLIENT_ID_CONFIG)) {
//            properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "client_receiver");
//        }
        if (!properties.containsKey(ConsumerConfig.MAX_POLL_RECORDS_CONFIG) && maxPollSize > 0) {
            properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "" + maxPollSize);
        }
        if (!properties.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)) {
            properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        } else {
            autoCommit = Boolean.parseBoolean(properties.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        }
        if (!properties.containsKey(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG)) {
            properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "120000");
        }
        if (!properties.containsKey(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)) {
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        }

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    }

    @Override
    public void openSubscribe() {
        openSubscribe(topic, group, pollTimeOut);
    }

    @Override
    public void openSubscribe(String group) {
        openSubscribe(topic, group, pollTimeOut);
    }


    @Override
    public void openSubscribe(String topic, String group, Long pollTimeOut) {
        if (kafkaConsumer != null) {
            return;
        }
        this.pollTimeOut = pollTimeOut;
        this.topic = topic;
        if (group != null) {
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        }
        if (offset != null) {
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        }
        log.info("open subscribe, topic:{}, : {}", topic, properties);
        kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public void assign(String topic, String group, Long pollTimeOut, Long seekOffset) {
        if (kafkaConsumer == null) {
            this.pollTimeOut = pollTimeOut;
            this.topic = topic;
            if (group != null) {
                properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
            }
            if (offset != null) {
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
            }
            kafkaConsumer = new KafkaConsumer<>(properties);
        }

        List<TopicPartition> topicPartitions = new ArrayList<>();
        List<PartitionInfo> partition = kafkaConsumer.partitionsFor(topic);

        // 遍历得到所有分区
        for (PartitionInfo partitionInfo : partition) {
            TopicPartition topicPartition = new TopicPartition(topic, partitionInfo.partition());
            topicPartitions.add(topicPartition);
        }
        // 手动分配topic的所有分区给当前消费者
        kafkaConsumer.assign(topicPartitions);
        log.info("assign topic:{}, : {}", topic, properties);

        // 循环获取每个分区的latest,单独设置每个分区的seek
        for (TopicPartition topicPartition : topicPartitions) {
            // latest 代表当前groupId最后一次消费当前分区的offset值
            long latest = kafkaConsumer.position(topicPartition);
            long offset;
            if (seekOffset != null && seekOffset <= latest) {
                offset = seekOffset;
            } else {
                // read the latest - 1
                offset = latest != 0L ? latest - 1 : 0;
            }
            kafkaConsumer.seek(topicPartition, offset);
        }
    }

    @Override
    public DataBody<Map<String, Object>> receive() {
        Map<String, Object> body = pollData();
        DataBody<Map<String, Object>> dataBody = new DataBody<>(body);
        return dataBody;
    }

    private Map<String, Object> pollData() {
        Map<String, Object> body = cacheQueue.poll();
        if (body != null) {
            return body;
        }

        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(pollTimeOut));
        if (records == null) {
            return null;
        }

        for (ConsumerRecord<String, String> record : records) {
            switch (messageFormat) {
                case JSON:
                    body = JSONUtils.toMap(record.value(), String.class, Object.class);
                    break;
                case JSON_ARRAY:
                    List<Map> l = JSONUtils.toList(record.value(), Map.class);
                    body = new HashMap<>();
                    body.put(record.key(), l);
                    break;
                case STRING:
                default:
            }

            if (body == null) {
                body = new HashMap<>();
                body.put(record.key(), record.value());
            }
            cacheQueue.offer(body);
        }

        if (!autoCommit && !records.isEmpty()) {
            kafkaConsumer.commitAsync();
        }
        return cacheQueue.poll();
    }

    private Map<String, Object> pollLastData() {
        Map<String, Object> body = null;
        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(pollTimeOut));
        if (records == null) {
            return null;
        }

        for (ConsumerRecord<String, String> record : records) {
            switch (messageFormat) {
                case JSON:
                    body = JSONUtils.toMap(record.value(), String.class, Object.class);
                    break;
                case JSON_ARRAY:
                    List<Map> l = JSONUtils.toList(record.value(), Map.class);
                    body = new HashMap<>();
                    body.put(record.key(), l);
                    break;
                case STRING:
                default:
            }

            if (body == null) {
                body = new HashMap<>();
                body.put(record.key(), record.value());
            }
        }

        if (!autoCommit && !records.isEmpty()) {
            kafkaConsumer.commitAsync();
        }
        return body;
    }


    @Override
    public PageBody<Map<String, Object>> receive(int size) {
        PageBody<Map<String, Object>> pageBody = new PageBody<>();
        List<Map<String, Object>> list = new ArrayList<>();
        pageBody.setList(list);
        Map<String, Object> item = pollData();
        if (item == null) {
            return pageBody;
        }
        list.add(item);
        while (list.size() < size) {
            item = pollData();
            if (item == null) {
                break;
            }
            list.add(item);
        }

        return pageBody;
    }

    @Override
    public DataBody<Map<String, Object>> receiveLastData() {
        Map<String, Object> body = pollLastData();
        DataBody<Map<String, Object>> dataBody = new DataBody<>(body);
        return dataBody;
    }

    @Override
    public void close() {
        if (kafkaConsumer != null) {
            try {
                kafkaConsumer.close(Duration.ofSeconds(2));
                kafkaConsumer = null;
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }
    }

    @Override
    public boolean hasCache() {
        return !cacheQueue.isEmpty();
    }

    @Override
    public String key() {
        return String.join("_", topic, group, offset);
    }

}
