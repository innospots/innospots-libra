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

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/9
 */
public class AuthenticationException extends BaseException {

    private String requestUri;

    private AuthenticationException(Class<?> invokeClass, ResponseCode responseCode, Throwable cause, Object... params) {
        super(invokeClass, responseCode, cause, params);
    }

    private AuthenticationException(Class<?> invokeClass, ResponseCode responseCode, Object... params) {
        super(invokeClass, responseCode, params);
    }

    public static AuthenticationException buildException(Class<?> invokeClass, Throwable cause, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_FAILED, cause, params);
    }

    public static AuthenticationException buildException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_FAILED, params);
    }

    public static AuthenticationException buildPasswordException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_PASSWORD_INVALID, params);
    }

    public static AuthenticationException buildKaptchaException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_KAPTCHA_INVALID, params);
    }

    public static AuthenticationException buildUserException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_USER_INVALID, params);
    }

    public static AuthenticationException buildTokenExpiredException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_TOKEN_EXPIRED, params);
    }

    public static AuthenticationException buildTokenInvalidException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_TOKEN_INVALID, params);
    }

    public static AuthenticationException buildDecryptException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.AUTH_DECRYPT_ERROR, params);
    }

    public static AuthenticationException buildPermissionException(Class<?> invokeClass, Object... params) {
        return new AuthenticationException(invokeClass, ResponseCode.PERMISSION_DENIED, params);
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
}
