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

package io.innospots.base.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.model.response.ResponseCode;
import lombok.Getter;
import lombok.Setter;

/**
 * error response
 *
 * @author Raydian
 * @version 1.0.0
 * @date 2020/10/31
 */
@Getter
@Setter
public class ErrorResponse {

    private String message;

    private String code;

    private String module;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String detail;

    public ErrorResponse() {
    }

    public ErrorResponse(String message, String code, String module, String detail) {
        this.message = message;
        this.code = code;
        this.module = module;
        this.detail = detail;
    }

    public ErrorResponse(String message, String code, String module) {
        this(message, code, module, null);
    }

    public static ErrorResponse build(BaseException exception) {
        return new ErrorResponse(exception.message, exception.getCode(), exception.getModule(), exception.getDetail());
    }

    public static ErrorResponse build(ResponseCode responseCode, String module) {
        return new ErrorResponse(responseCode.getInfo(), responseCode.getCode(), module);
    }

    public static ErrorResponse build(ResponseCode responseCode, String module, String detail) {
        return new ErrorResponse(responseCode.getInfo(), responseCode.getCode(), module, detail);
    }

    public static ErrorResponse build(RuntimeException exception) {
        return new ErrorResponse(exception.getMessage(), ResponseCode.FAIL.getCode(), "kernel");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(", module='").append(module).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append("code='").append(code).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
