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

    private Integer scanSlotTimeMills = 200;

    private Integer executorSize = 8;

    private Integer assignedJobTimeoutSecond = 60;

    /**
     * default group keys using select ready job
     */
    private List<String> groupKeys;

}
