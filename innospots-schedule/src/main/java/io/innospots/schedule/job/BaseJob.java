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

import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.schedule.exception.JobExecutionException;
import io.innospots.schedule.model.JobExecution;
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

    protected JobExecution jobExecution;

    public void prepare(){
    }

    public abstract void execute();

    protected String validParamString(String key){
        String value = jobExecution.getString(key);
        if(value==null){
            throw new JobExecutionException(this.getClass(), ResponseCode.PARAM_NULL, key + " is null");
        }
        return value;
    }

    protected Integer validParamInteger(String key){
        Integer value = jobExecution.getInteger(key);
        if(value==null){
            throw new JobExecutionException(this.getClass(), ResponseCode.PARAM_NULL, key + " is null");
        }
        return value;
    }

    protected Map getParamMap(String key){
        Object executeJobParams = jobExecution.get(key);
        Map prm = null;
        if (executeJobParams instanceof Map) {
            prm = (Map) executeJobParams;
        } else if (executeJobParams instanceof String && ((String) executeJobParams).startsWith("{")) {
            prm = JSONUtils.toMap((String) executeJobParams);
        }
        return prm;
    }

}
