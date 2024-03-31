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

package io.innospots.schedule.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/4
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "innospots.schedule")
public class InnospotsScheduleProperties {

    /**
     * ready queue watcher scan interval time,
     * the smaller the value, the shorter the wait time of the pull queue.
     *
     * The default value is 2 seconds, which indicates that the execution tasks
     * in the message queue are rotated at least once every two seconds
     * and assigned to the current execution node.
     */
    private Integer scanSlotTimeMills = 2000;

    /**
     * The maximum number of assigned jobs that can be executed in the executor
     */
    private Integer executorSize = 8;

    /**
     * the timeout second when the job state is assigned in the queue
     *
     */
    private Integer assignedJobTimeoutSecond = 60;

    /**
     * default group keys using select ready job
     * If group keys is used, you can specify that the execution node executes only the jobs of a certain queue group.
     * If the value is empty, all the jobs in the queue will be detected and executed
     */
    private List<String> groupKeys;

}
