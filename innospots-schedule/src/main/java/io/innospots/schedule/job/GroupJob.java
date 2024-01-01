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

package io.innospots.schedule.job;

import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * group job include multi sub job, which parallel to execute
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/1
 */
@Slf4j
public class GroupJob extends BaseJob {

    public static final String PARAM_EXECUTE_JOB_KEYS = "job.execute.job_keys";

    public static final String PARAM_EXECUTE_JOB_PARAMS = "job.execute.job_params";

    //job keys split by ','
    private List<String> groupJobKeys;

    private ReadJobDispatcher readJobDispatcher;

    @Override
    public void prepare() {
        String groupJobKeysStr = this.validParamString(PARAM_EXECUTE_JOB_KEYS);
        groupJobKeys = Arrays.stream(groupJobKeysStr.split(",")).collect(Collectors.toList());
        readJobDispatcher = BeanContextAwareUtils.getBean(ReadJobDispatcher.class);
    }

    @Override
    public void execute() {
        Map prm = getParamMap(PARAM_EXECUTE_JOB_PARAMS);
        this.jobExecution.setSubJobCount((long) groupJobKeys.size());
        for (int i = 0; i < groupJobKeys.size(); i++) {
            log.info("dispatch sub job:{} , parentExecutionId:{}", groupJobKeys.get(i), jobExecution.getExecutionId());
            readJobDispatcher.execute(jobExecution.getExecutionId(), i + 1, groupJobKeys.get(i), prm);
        }
    }
}
