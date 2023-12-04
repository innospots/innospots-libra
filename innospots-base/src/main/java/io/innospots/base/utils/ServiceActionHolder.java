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

package io.innospots.base.utils;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/10
 */
public class ServiceActionHolder {

    /**
     * schedule service
     */
    private static boolean scheduleService;

    /**
     * job execute service
     */
    private static boolean jobExecutionService;

    /**
     * execute job group keys
     */
    private static List<String> groupKeys;

    /**
     * clean execution log and cache
     */
    private static boolean cleanTask;

    public static boolean isJobExecutionService() {
        return jobExecutionService;
    }

    public static void setJobExecutionService(boolean jobExecutionService) {
        ServiceActionHolder.jobExecutionService = jobExecutionService;
    }

    public static List<String> getGroupKeys() {
        return groupKeys;
    }

    public static void setGroupKeys(List<String> groupKeys) {
        ServiceActionHolder.groupKeys = groupKeys;
    }

    public static boolean isScheduleService() {
        return scheduleService;
    }

    public static void setScheduleRole(boolean scheduleService) {
        ServiceActionHolder.scheduleService = scheduleService;
    }

    public static boolean isCleanTask() {
        return cleanTask;
    }

    public static void setCleanTask(boolean cleanTask) {
        ServiceActionHolder.cleanTask = cleanTask;
    }
}
