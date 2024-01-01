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

package io.innospots.base.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.quartz.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
@Getter
@Setter
public class ExecutionBase {

    /**
     * primary key
     */
    protected String executionId;

    /**
     * resource, task, node key
     */
    protected String key;

    /**
     * flow, node, task
     */
    protected String keyType;

    /**
     * resource ,task, node name
     */
    protected String name;

    /**
     * md5(context+key)
     */
    protected String instanceKey;


    protected String scopes;

    /**
     * execution status
     */
    protected ExecutionStatus status;

    protected LocalDateTime startTime;

    protected LocalDateTime endTime;

    protected LocalDateTime selfEndTime;

    protected String detailUri;

    protected boolean next;

    /**
     * execution message
     */
    protected String message;

    /**
     * execution order sequence number
     */
    protected Integer sequenceNumber;

    /**
     * execution store mode when be executed
     */
    protected ExecMode execMode;

    /**
     * params and execute data share in the flow
     */
    protected Map<String,Object> context;


    protected String createdBy;

    public String getString(String key){
        return context!=null? (String) context.get(key):null;
    }

    public Integer getInteger(String key){
        return context!=null? (Integer) context.getOrDefault(key,0) :null;
    }

    public Date getDate(String key){
        return context!=null? (Date) context.getOrDefault(key,null) :null;
    }

    public Object get(String key){
        return context!=null? context.getOrDefault(key,null) :null;
    }

    public LocalDateTime getLocalDateTime(String key){
        return context!=null? (LocalDateTime) context.getOrDefault(key,null) :null;
    }

    public LocalDate getLocalDate(String key){
        return context!=null? (LocalDate) context.getOrDefault(key,null) :null;
    }

    public Boolean getBoolean(String key){
        return context!=null? (Boolean) context.getOrDefault(key,null) :null;
    }

    public String[] getStringArray(String key){
        return context!=null? (String[]) context.getOrDefault(key,null) :null;
    }

}
