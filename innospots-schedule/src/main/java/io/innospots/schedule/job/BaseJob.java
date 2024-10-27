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

import io.innospots.base.model.response.R;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.exception.JobExecutionException;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.utils.ParamParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Getter
@Setter
public abstract class BaseJob {

    public static final String PARAM_EXECUTE_JOB_PARAMS = "job.execute.job_params";

    protected JobExecution jobExecution;

    public BaseJob(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    /**
     * read job parameter and connect to resource, service
     */
    public void prepare() {
    }

    public abstract JobType jobType();

    /**
     * execute the job
     * @return
     */
    public abstract R<Map<String,Object>> execute();


    protected Integer getParamInteger(String key) {
        return this.jobExecution.getInteger(key);
    }

    protected String getParamString(String key) {
        return this.jobExecution.getString(key);
    }

    protected String validParamString(String key) {
        String value = jobExecution.getString(key);
        if (value == null) {
            throw new JobExecutionException(this.getClass(), ResponseCode.PARAM_NULL, key + " is null");
        }
        return value;
    }

    protected Integer validParamInteger(String key) {
        Integer value = jobExecution.getInteger(key);
        if (value == null) {
            throw new JobExecutionException(this.getClass(), ResponseCode.PARAM_NULL, key + " is null");
        }
        return value;
    }

    protected Map getParamMap(String key) {
        return ParamParser.getParamMap(jobExecution, key);
    }

}
