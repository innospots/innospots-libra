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

import cn.hutool.http.ContentType;
import io.innospots.base.exception.*;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.i18n.LocaleMessageUtils;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.response.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Smars
 * @date 2021/2/14
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        logger.error("exception: {}, msg: {}", e.getClass(), e.getMessage());
        String resp;
        if(response.getContentType()!=null && response.getContentType().contains(ContentType.EVENT_STREAM.getValue())){
            resp = "id:\n";
            resp += "event:error-event\n";
            resp += "data:";
            resp += e.getMessage();
            return ResponseEntity.status(response.getStatus()).build();
        }else{
            resp = JSONUtils.toJsonString(ErrorResponse.build(ResponseCode.FAIL,"system", e.getMessage()));
            return ResponseEntity.status(response.getStatus()).body(resp);
        }

    }

    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ErrorResponse handleException(MissingServletRequestParameterException e) {
        logger.error("exception: {}, msg: {}", e.getClass(), e.getMessage());
        return ErrorResponse.build(ResponseCode.PARAM_NULL,"servlet" ,e.getMessage());
    }

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
    public ResponseEntity<String> handleException(RuntimeException e, HttpServletRequest request, HttpServletResponse response) {
        logger.error("runtime exception: ", e);
        String resp;
        if(response.getContentType().contains(ContentType.EVENT_STREAM.getValue())){
            resp = "id:\n";
            resp += "event:error-event\n";
            resp += "data:";
            resp += e.getMessage();
        }else{
            resp = JSONUtils.toJsonString(ErrorResponse.build(e));
        }
        return ResponseEntity.status(response.getStatus()).body(resp);
        //return ErrorResponse.build(e);
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(code = HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse handleException(MultipartException e) {
        logger.error("multipart exception: ", e);
        return ErrorResponse.build(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleException(MethodArgumentNotValidException e) {

        ValidatorException exception;
        if (e.getBindingResult().hasErrors()) {
            exception = buildInvalidException(e.getBindingResult());
        } else {
            exception = ValidatorException.buildInvalidException(e.getObjectName(), e.getMessage());
        }
        logger.error("argument invalid, ", e);
        return ErrorResponse.build(exception);
    }

    private ValidatorException buildInvalidException(BindingResult bindingResult) {
        List<String> messages = new ArrayList<>();
        Set<String> modules = new LinkedHashSet<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            Object[] args;
            if (fieldError.getArguments() != null) {
                args = new Object[fieldError.getArguments().length + 2];
                for (int i = 1; i < fieldError.getArguments().length; i++) {
                    args[i + 2] = fieldError.getArguments()[i];
                }
            } else {
                args = new Object[2];
            }
            //bean name
            args[0] = fieldError.getObjectName();
            //bean field
            args[1] = fieldError.getField();
            //bean value
            args[2] = fieldError.getRejectedValue();
            modules.add(String.valueOf(args[0]));
            String msg = null;
            //codes definitions: ValidType.beanName.fieldName,ValidType.fieldName,ValidType.javaType,ValidType
            //codes: NotBlank.datasource.dyType,NotBlank.dbType, NotBlank.java.util.String, NotBlank
            if (fieldError.getCodes() != null) {
                for (String code : fieldError.getCodes()) {
                    msg = LocaleMessageUtils.message(code, args, "");
                    if (StringUtils.isNotEmpty(msg)) {
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(msg)) {
                msg = fieldError.getObjectName() + "." + fieldError.getField() + " " + fieldError.getDefaultMessage();
            }
            messages.add(msg);
        }//end for
        return ValidatorException.buildInvalidException(String.join(",", modules),
                String.join(",", messages));
    }
}
