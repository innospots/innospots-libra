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

import io.innospots.base.exception.ResourceException;
import io.innospots.schedule.model.JobExecution;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/5
 */
public class JobBuilder {

    public static BaseJob build(JobExecution jobExecution) {
        BaseJob baseJob;
        try {
            Class<?> jobClass = Class.forName(jobExecution.getJobClass());
            baseJob = (BaseJob) jobClass.getConstructor().newInstance();
            baseJob.jobExecution = jobExecution;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw ResourceException.buildAbandonException(JobBuilder.class, "jobClass not found," + jobExecution.getJobClass(), e);
        }
        return baseJob;
    }
}
