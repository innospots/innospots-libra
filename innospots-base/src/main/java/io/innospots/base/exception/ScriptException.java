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

package io.innospots.base.exception;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.model.response.ResponseCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2021/5/25
 */
@Getter
@Setter
public class ScriptException extends BaseException {

    private String scriptType;

    private ScriptException(Class<?> invokeClass, ResponseCode responseCode, String scriptType, Throwable cause, Object... params) {
        super(invokeClass, responseCode, cause, params);
        this.scriptType = scriptType;
    }

    private ScriptException(Class<?> invokeClass, ResponseCode responseCode, String scriptType, Object... params) {
        super(invokeClass, responseCode, params);
        this.scriptType = scriptType;
    }

    public static ScriptException buildCompileException(Class<?> invokeClass, String scriptType, Throwable cause, Object... params) {
        return new ScriptException(invokeClass, ResponseCode.SCRIPT_COMPILE_ERROR, scriptType, cause, params);
    }

    public static ScriptException buildInvokeException(Class<?> invokeClass, String scriptType, Throwable cause, Object... params) {
        return new ScriptException(invokeClass, ResponseCode.SCRIPT_INVOKE_ERROR, scriptType, cause, params);
    }

    public static ScriptException buildCompileException(Class<?> invokeClass, String scriptType, Object... params) {
        return new ScriptException(invokeClass, ResponseCode.SCRIPT_COMPILE_ERROR, scriptType, params);
    }

    public static ScriptException buildInvokeException(Class<?> invokeClass, String scriptType, Object... params) {
        return new ScriptException(invokeClass, ResponseCode.SCRIPT_INVOKE_ERROR, scriptType, params);
    }

    public static ScriptException buildExecutorException(Class<?> invokeClass, String scriptType, Object... params) {
        return new ScriptException(invokeClass, ResponseCode.SCRIPT_EXECUTOR_ERROR, scriptType, params);
    }


}
