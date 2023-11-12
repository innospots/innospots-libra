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

package io.innospots.workflow.core.exception;

import io.innospots.base.exception.BaseException;
import io.innospots.base.model.response.ResponseCode;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/12
 */
public class NodeBuildException extends BaseException {

    public NodeBuildException(Class<?> invokeClass, ResponseCode responseCode, Throwable cause, Object... params) {
        super(invokeClass, responseCode, cause, params);
    }

    public NodeBuildException(Class<?> invokeClass, ResponseCode responseCode, String message, String detail) {
        super(invokeClass, responseCode, message, detail);
    }

    public NodeBuildException(Class<?> invokeClass, ResponseCode responseCode, Object... params) {
        super(invokeClass, responseCode, params);
    }

    public NodeBuildException(Class<?> invokeClass, ResponseCode responseCode, String messageCode, String defaultMessage, Object... params) {
        super(invokeClass, responseCode, messageCode, defaultMessage, params);
    }

    public NodeBuildException(Class<?> invokeClass, ResponseCode responseCode, String messageCode, String defaultMessage, Throwable cause, Object... params) {
        super(invokeClass, responseCode, messageCode, defaultMessage, cause, params);
    }

    public NodeBuildException(String module, ResponseCode responseCode, Throwable cause, Object... params) {
        super(module, responseCode, cause, params);
    }

    public static NodeBuildException build(String module, ResponseCode responseCode, Throwable cause, String detail){
        return new NodeBuildException(module, responseCode, cause, detail);
    }
    public static NodeBuildException build(String module, Throwable cause,String detail){
        return new NodeBuildException(module, ResponseCode.RESOURCE_BUILD_ERROR, cause, detail);
    }
    public static NodeBuildException build(String module, Throwable cause){
        return new NodeBuildException(module, ResponseCode.RESOURCE_BUILD_ERROR, cause, null);
    }
}
