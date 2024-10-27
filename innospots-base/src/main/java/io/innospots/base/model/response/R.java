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
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/10/31
 */
@Schema(title = "api response wrapper")
public class R<T> {

    @Setter
    @Getter
    @Schema(title = "response message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String message;

    @Setter
    @Getter
    @Schema(title = "status code")
    protected String code;

    @Setter
    @Getter
    @Schema(title = "response detail message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String detail;

    @Setter
    @Getter
    @Schema(title = "body data")
    protected T body;

    @Schema(title = "response timestamp")
    protected long ts;

    public static <T> boolean hasData(R<T> innospotsResponse) {
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

    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> success(T body) {
        R<T> response = new R<>();
        response.setBody(body);
        response.fillResponse(ResponseCode.SUCCESS);
        return response;
    }

    public static <T> R<T> fail(ResponseCode responseCode, String detail) {
        R<T> innospotsResponse = new R<>();
        innospotsResponse.fillResponse(responseCode);
        innospotsResponse.setDetail(detail);
        return innospotsResponse;
    }

    public static <T> R<T> fail(ResponseCode responseCode) {
        R<T> innospotsResponse = new R<>();
        innospotsResponse.fillResponse(responseCode);
        return innospotsResponse;
    }

    public static <T> R<T> fail(String message, String code, String detail) {
        R<T> innospotsResponse = new R<>();
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

    public void fillBody(T body){
        this.setBody(body);
        this.fillResponse(ResponseCode.SUCCESS);
    }

    public long endTimestamp() {
        return ts;
    }
}
