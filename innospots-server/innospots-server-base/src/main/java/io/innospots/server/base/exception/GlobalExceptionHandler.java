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

package io.innospots.server.base.exception;

import io.innospots.base.exception.*;
import io.innospots.base.model.response.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

/**
 * @author Smars
 * @date 2021/2/14
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ErrorResponse handleAuthenticationException(AuthenticationException e) {
        logger.error("authenticationException: {}", e.getMessage());
        return ErrorResponse.build(e);
    }


    @ExceptionHandler(BaseException.class)
    public ErrorResponse handleException(BaseException e) {
        logger.error("base exception: ", e);
        return ErrorResponse.build(e);
    }

    @ExceptionHandler(ScriptException.class)
    public ErrorResponse handleException(ScriptException e) {
        logger.error("script exception: ", e);
        return ErrorResponse.build(e);
    }

    @ExceptionHandler(RuntimeException.class)
    public ErrorResponse handleException(RuntimeException e) {
        logger.error("runtime exception: ", e);
        return ErrorResponse.build(e);
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(code = HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse handleException(MultipartException e) {
        logger.error("multipart exception: ", e);
        return new ErrorResponse(ResponseCode.IMG_SIZE_ERROR.getInfo(), ResponseCode.IMG_SIZE_ERROR.getCode(), "");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleException(MethodArgumentNotValidException e) {

        ValidatorException exception;
        if (e.getBindingResult().hasErrors()) {
            exception = ValidatorException.buildInvalidException(e.getBindingResult());
        } else {
            exception = ValidatorException.buildInvalidException(e.getObjectName(), e.getMessage());
        }
        logger.error("argument invalid, ", e);
        return ErrorResponse.build(exception);
    }
}
