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
import io.innospots.base.execution.ExecutionBase;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.schedule.enums.JobType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/6
 */
@Getter
@Setter
public class JobExecution extends ExecutionBase {

    private JobType jobType;

    private Integer percent;

    private Long subJobCount;

    private Long successCount;

    private Long failCount;

    private String extExecutionId;

    private String resourceKey;

    /**
     * retry execute the first origin executionId
     * is null if the execution is the first time executed
     */
    private String originExecutionId;

    private String timeConsume;

    private String parentExecutionId;

    private String serverKey;

    private String jobClass;

}
