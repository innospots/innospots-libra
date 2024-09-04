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

package io.innospots.base.connector.http;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/6
 */
@Getter
@Setter
public class HttpData {

    private Map<String, Object> headers = new LinkedHashMap<>();
    private Map<String, Object> params = new LinkedHashMap<>();
    private Object body;
    private int status;
    private String message;

    public void fill(HttpData httpData){
        if(MapUtils.isNotEmpty(httpData.headers)){
            this.headers.putAll(httpData.headers);
        }
        if(MapUtils.isNotEmpty(httpData.params)){
            this.params.putAll(httpData.params);
        }
        if(httpData.body != null){
            this.body = httpData.body;
        }
        this.status = httpData.status;
        this.message = httpData.message;
    }

    public void addHeader(String name, Object value) {
        this.headers.put(name, value);
    }

    public void addParam(String name, Object value) {
        this.params.put(name, value);
    }

    public void addHeaders(Map<String, Object> headers) {
        this.headers.putAll(headers);
    }

    public void addParams(Map<String, Object> params) {
        this.params.putAll(params);
    }

    @Override
    public String toString() {
        return "HttpData{" +
                "headers=" + headers +
                ", params=" + params +
                ", body=" + body +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
