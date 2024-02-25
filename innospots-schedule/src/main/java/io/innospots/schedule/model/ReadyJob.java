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

package io.innospots.schedule.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.enums.MessageStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
@Getter
@Setter
public class ReadyJob {

    /**
     * md5(jobKey + params)
     */
    private String jobReadyKey;

    private String jobClass;

    private String originExecutionId;

    private String parentExecutionId;

    private String jobKey;

    private String jobName;

    private String keyType;

    private String scopes;

    private Map<String,Object> context;

    private MessageStatus messageStatus;

    private Integer sequenceNumber;

    private String groupKey;

    private String extExecutionId;

    private String resourceKey;

    private String serverKey;

    private Integer version;

    private String instanceKey;

    private JobType jobType;

    private Long subJobCount;

}
