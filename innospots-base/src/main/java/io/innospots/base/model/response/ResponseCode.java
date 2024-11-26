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

import lombok.Getter;

/**
 * response code definition
 *
 * @author Raydian
 * @version 1.0.0
 * @date 2020/10/31
 */
@Getter
public enum ResponseCode {

    /**
     *
     */

    AUTH_PASSWORD_INVALID("60000", ""),
    AUTH_KAPTCHA_INVALID("60001", ""),
    AUTH_USER_INVALID("60002", "user not exits"),
    AUTH_TOKEN_INVALID("60003", "token invalid"),
    AUTH_TOKEN_EXPIRED("60004", "token expired"),
    AUTH_DECRYPT_ERROR("60005", "decrypt error"),
    AUTH_FAILED("60006", "authentication failed"),
    CRYPTO_ERROR("60007", "crypto error"),
    AUTH_ACCESS_FORBIDDEN("60008", "access forbidden"),
    PERMISSION_DENIED("70000", "permission denied"),
    SCRIPT_INVOKE_ERROR("80000", "script invoke error"),
    SCRIPT_COMPILE_ERROR("80001", ""),
    SCRIPT_EXECUTOR_ERROR("80002", ""),
    DATA_CONNECTION_ERROR("90000", "connection error"),
    DATA_SCHEMA_ERROR("90001", "schema is error"),
    DATA_SCHEMA_MISSING("90002", "schema is missing"),
    DATA_SOURCE_ERROR("90003", "datasource config error"),
    DATA_SOURCE_MISSING("90004", "datasource config"),
    DATA_OPERATION_ERROR("90005", "operation"),
    DATA_SQL_OPERATION_ERROR("90006", "operation"),
    RESOURCE_EXIST("20000", "resource exist"),
    RESOURCE_ABANDON("20001", "resource abandon"),
    RESOURCE_DUPLICATE("20002", "resource duplicated"),
    RESOURCE_COUNT_ERROR("20003", "resource count error"),
    RESOURCE_CREATE_FAILED("20004", "resource create action error"),
    RESOURCE_DELETE_FAILED("20005", "resource delete action error"),
    RESOURCE_UPDATE_FAILED("20006", "resource update error"),
    RESOURCE_STATUS_ERROR("20007", "resource status error"),
    RESOURCE_IO_ERROR("20008", "resource io error"),
    RESOURCE_TYPE_ERROR("20009", "resource type error"),
    RESOURCE_NOT_EXIST("20010", "resource not exist"),
    RESOURCE_INSTALL_ERROR("20011", "resource install error"),
    RESOURCE_PREPARE_ERROR("20012", "resource prepare error"),
    RESOURCE_BUILD_ERROR("20013", "resource build error"),
    RESOURCE_UPLOAD_ERROR("20014", "resource upload error"),
    PARAM_NULL("30000", "parameter is null"),
    PARAM_INVALID("30001", "parameter is invalid"),
    PARAM_COUNT_ERROR("30002", "the number of parameter is not match"),
    PARAM_FIELD_INVALID("30003", "field is invalid"),
    EXECUTE_ERROR("40001", "execute is failed"),
    EXECUTE_FEIGN_ERROR("40002", "execute feign interface is failed"),
    EXECUTE_SCHEDULE_ERROR("40003", "schedule failed"),
    CONFIG_MISSING("50000", "config field is missing"),
    CONFIG_CLASS_INVALID("50001", "node class type is invalid"),
    CONFIG_PARAM_ERROR("50002", "node config param error"),
    CONFIG_TYPE_INVALID("50003", "config type is invalid"),
    FILE_TYPE_INVALID("50004", "file type is invalid"),
    LOADING("11000", "loading data, not return"),
    INITIALIZING("11001", "initializing data, not return"),
    EXCEED_TIMES("12000", "exceed times"),
    SERIALIZE_ERROR("13000", "model to json error"),
    BEAN_CONVERT_ERROR("13001", "json or map to model error"),
    IMG_SIZE_ERROR("14001", "the image size exceeds the maximum limit of 1M"),
    IMG_SUFFIX_ERROR("14002", "the image suffix error"),
    IMG_UPLOAD_ERROR("14003", "the image upload error"),
    PUBLISH_FAILED("15000", "publish failed with error"),
    PUBLISH_DRAFT_MISSING("15001", "draft is missing when publishing"),
    PUBLISH_UNCHANGED("15002", "unchanged, not need to publish"),
    PUBLISH_BUILD_FAILED("15003", "building failed, publish failed"),
    TASK_STATUS_EDIT_ERROR("16001", "Only the creator and principals can edit the task status"),
    SUCCESS("10000", "response success"),
    FAIL("10001", "system error"),
    ASYNC_RUNNING("10002", "async request execute");

    private String code;

    private String info;

    ResponseCode(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String code() {
        return code;
    }

    public String info() {
        return info;
    }


    public static ResponseCode getByCode(String code) {
        ResponseCode responseCode = null;
        for (ResponseCode value : ResponseCode.values()) {
            if (value.getCode().equals(code)) {
                responseCode = value;
            }
        }
        return responseCode;
    }
}
