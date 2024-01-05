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

package io.innospots.schedule.utils;

import io.innospots.base.utils.ServiceActionHolder;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
public class ScheduleUtils {

    public static final String SERVICE_TYPE_SCHEDULE = "SCHEDULER_SERVICE";
    public static final String SERVICE_TYPE_EXECUTOR = "EXECUTOR_SERVICE";
    public static final String PARAM_TIMEOUT_SECOND = "exec.timeout";


    public static boolean isScheduler() {
        return SERVICE_TYPE_SCHEDULE.equals(ServiceActionHolder.getServiceType());
    }

    public static boolean isExecutor() {
        return SERVICE_TYPE_EXECUTOR.equals(ServiceActionHolder.getServiceType());
    }

}
