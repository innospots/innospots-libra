/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.base.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/10/31
 */
@Schema(title = "api response wrapper")
public class InnospotsResponse<T> {

    @Schema(title = "response message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @Schema(title = "status code")
    private String code;

    @Schema(title = "response detail message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String detail;

    @Schema(title = "body data")
    private T body;

    @Schema(title = "response timestamp")
    private long ts;

    public static <T> boolean hasData(InnospotsResponse<T> innospotsResponse) {
        return innospotsResponse != null && innospotsResponse.hasData();
    }

    public boolean hasData() {
        boolean has = body != null && ResponseCode.SUCCESS.getCode().equals(code);
        if (!has) {
            return has;
        }
        if (body instanceof PageBody) {
            return CollectionUtils.isNotEmpty(((PageBody<?>) body).getList());
        }
        if (body instanceof DataBody) {
            return ((DataBody<?>) body).getBody() != null;
        }

        return has;
    }

    public void fillResponse(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.detail = responseCode.getInfo();
        this.ts = System.currentTimeMillis();
    }

    public static <T> InnospotsResponse<T> success() {
        return success(null);
    }

    public static <T> InnospotsResponse<T> success(T body) {
        InnospotsResponse<T> response = new InnospotsResponse<>();
        response.setBody(body);
        response.fillResponse(ResponseCode.SUCCESS);
        return response;
    }

    public static <T> InnospotsResponse<T> fail(ResponseCode responseCode, String detail) {
        InnospotsResponse<T> innospotsResponse = new InnospotsResponse<>();
        innospotsResponse.fillResponse(responseCode);
        innospotsResponse.setDetail(detail);
        return innospotsResponse;
    }

    public static <T> InnospotsResponse<T> fail(ResponseCode responseCode) {
        InnospotsResponse<T> innospotsResponse = new InnospotsResponse<>();
        innospotsResponse.fillResponse(responseCode);
        return innospotsResponse;
    }

    public static <T> InnospotsResponse<T> fail(String message, String code, String detail) {
        InnospotsResponse<T> innospotsResponse = new InnospotsResponse<>();
        innospotsResponse.setDetail(detail);
        innospotsResponse.setCode(code);
        innospotsResponse.setMessage(message);
        innospotsResponse.ts = System.currentTimeMillis();
        return innospotsResponse;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("message='").append(message).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", detail='").append(detail).append('\'');
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    public Map<String, Object> info() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", code);
        resp.put("message", message);
        resp.put("detail", detail);
        return resp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public void fillBody(T body){
        this.setBody(body);
        this.fillResponse(ResponseCode.SUCCESS);
    }

    public long endTimestamp() {
        return ts;
    }
}
