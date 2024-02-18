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

package io.innospots.schedule.queue;

import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.model.ReadyJob;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/8
 */
public interface IReadyJobQueue {

    /**
     * poll read job from queue
     * @param fetchSize max fetch item size
     * @param groupKeys job group key
     * @return
     */
    List<ReadyJobEntity> poll(int fetchSize, List<String> groupKeys);

    /**
     * ack read job
     * @param jobReadyKey
     */
    void ackRead(String jobReadyKey);

    /**
     * push ready job
     * @param jobKey
     */
    int push(String jobKey, Map<String,Object> params);

    int push(String parentExecutionId,Integer sequenceNumber, String jobKey, Map<String,Object> params);

    /**
     * push ready job using job key
     * @param jobKey
     */
    int push(String jobKey);

    int push(ReadyJob readyJob);

    /**
     * reset assign job to unread, if job's update time is greater than the set timeout
     */
    int resetAssignTimeoutJob(int timeoutSecond);

    int cancelJob(String jobKey);

    int cancelJobByParentExecutionId(String parentExecutionId);
}
