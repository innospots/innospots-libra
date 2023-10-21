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


import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.utils.LocaleMessageUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/6
 */
public class BaseException extends RuntimeException {

    public static final String KEY_PREFIX = "system.exception.";
    /**
     * the field of ResponseCode: code
     */
    protected String code;

    /**
     * exception detail trace information
     */
    protected String detail;

    /**
     * the field of ResponseCode:info, and joint specific exception information
     */
    protected String message;


    /**
     * the actual system exception
     */
    protected Class<? extends Throwable> exceptionClass;

    protected String module;

    public BaseException(Class<?> invokeClass, ResponseCode responseCode, Throwable cause, Object... params) {
        this(invokeClass.getSimpleName(), responseCode, cause, params);
    }

    public BaseException(Class<?> invokeClass, ResponseCode responseCode, String message, String detail) {
        this.module = invokeClass.getSimpleName();
        this.code = responseCode.getCode();
        this.message = message;
        this.detail = detail;
    }

    public BaseException(Class<?> invokeClass, ResponseCode responseCode, Object... params) {
        this(invokeClass.getSimpleName(), responseCode, params);
    }

    public BaseException(Class<?> invokeClass, ResponseCode responseCode, String messageCode, String defaultMessage, Object... params) {
        this(invokeClass.getSimpleName(), responseCode, messageCode, defaultMessage, params);
    }

    public BaseException(Class<?> invokeClass, ResponseCode responseCode, String messageCode, String defaultMessage, Throwable cause, Object... params) {
        this(invokeClass.getSimpleName(), responseCode, messageCode, defaultMessage, cause, params);
    }

    public BaseException(String module, ResponseCode responseCode, Throwable cause, Object... params) {
        super(cause);
        this.module = module;
        this.code = responseCode.getCode();
        this.message = LocaleMessageUtils.message(KEY_PREFIX + this.code, convert(params));
        if (StringUtils.isBlank(this.message)) {
            this.message = responseCode.getInfo() + ", " + Arrays.toString(params);
        }
        if (cause != null) {
            this.exceptionClass = cause.getClass();
            if (cause.getMessage() != null) {
                this.message += ", throwable:" + cause.getMessage();
            }
        }
    }

    public BaseException(String module, ResponseCode responseCode, String messageCode, String defaultMessage, Object... params) {
        this.module = module;
        this.code = responseCode.getCode();
        this.message = LocaleMessageUtils.message(messageCode, convert(params), defaultMessage);
        if (StringUtils.isBlank(this.message)) {
            this.message = responseCode.getInfo() + ", " + Arrays.toString(params);
        }
    }

    public BaseException(String module, ResponseCode responseCode, String messageCode, String defaultMessage, Throwable cause, Object... params) {
        super(cause);
        this.module = module;
        this.code = responseCode.getCode();
        if (cause != null) {
            this.exceptionClass = cause.getClass();
        }
        this.message = LocaleMessageUtils.message(messageCode, convert(params), defaultMessage);
        if (StringUtils.isBlank(this.message)) {
            this.message = responseCode.getInfo() + ", " + Arrays.toString(params);
        }
        if (cause != null && cause.getMessage() != null) {
            detail = cause.getClass().getSimpleName() + " , throwable: " + cause.getMessage();
        }
    }

    public BaseException(String module, ResponseCode responseCode, Object... params) {
        this.module = module;
        this.code = responseCode.getCode();
        this.message = LocaleMessageUtils.message(KEY_PREFIX + this.code, convert(params));
        if (StringUtils.isBlank(this.message)) {
            this.message = responseCode.getInfo() + ", " + Arrays.toString(params);
        }
    }

    public BaseException(String module, String message, ResponseCode responseCode) {
        this.module = module;
        this.code = responseCode.getCode();
        this.message = message;
    }

    private Object[] convert(Object... params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof String && ((String) params[i]).startsWith("${") && ((String) params[i]).endsWith("}")) {
                params[i] = LocaleMessageUtils.message(((String) params[i]).substring(2, ((String) params[i]).length() - 1));
            }
        }
        return params;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("module='").append(module).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", message='").append(message).append('\'');
        if (exceptionClass != null) {
            sb.append(", exceptionClass=").append(exceptionClass);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String getLocalizedMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }

    public String getModule() {
        return module;
    }

    public String getDetail() {
        return detail;
    }
}
