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
 * @author chenc
 * @date 2021/2/7 17:56
 */
public class ResourceException extends BaseException {

    private ResourceException(Class<?> invokeClass, ResponseCode responseCode, Throwable cause, Object... params) {
        super(invokeClass, responseCode, cause, params);
    }

    private ResourceException(Class<?> invokeClass, ResponseCode responseCode, Object... params) {
        super(invokeClass, responseCode, params);
    }

    public static ResourceException buildAbandonException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_ABANDON, params);
    }

    public static ResourceException buildCreateException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_CREATE_FAILED, params);
    }

    public static ResourceException buildDeleteException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_DELETE_FAILED, params);
    }

    public static ResourceException buildUpdateException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_UPDATE_FAILED, params);
    }

    public static ResourceException buildStatusException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_STATUS_ERROR, params);
    }

    public static ResourceException buildExistException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_EXIST, params);
    }

    public static ResourceException buildNotExistException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_NOT_EXIST, params);
    }

    public static ResourceException buildDuplicateException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_DUPLICATE, params);
    }

    public static ResourceException buildInstallException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_INSTALL_ERROR, params);
    }

    public static ResourceException buildTypeException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.CONFIG_TYPE_INVALID, params);
    }

    public static ResourceException buildFileTypeException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.FILE_TYPE_INVALID, params);
    }

    public static ResourceException buildIOException(Class<?> invokeClass, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_IO_ERROR, params);
    }

    public static ResourceException buildIOException(Class<?> invokeClass, Throwable cause, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_IO_ERROR, cause, params);
    }

    public static ResourceException buildUploadException(Class<?> invokeClass, Throwable cause, Object... params) {
        return new ResourceException(invokeClass, ResponseCode.RESOURCE_UPLOAD_ERROR, cause, params);
    }

}
