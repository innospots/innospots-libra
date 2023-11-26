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
 * @date 2022/2/15
 */
public class WorkflowPublishException extends BaseException {


    private WorkflowPublishException(Class<?> invokeClass, ResponseCode responseCode, String message, String detail) {
        super(invokeClass, responseCode, message, detail);
    }

    private WorkflowPublishException(Class<?> invokeClass, ResponseCode responseCode, Throwable cause, Object... params) {
        super(invokeClass, responseCode, cause, params);
    }

    public static WorkflowPublishException buildException(Class<?> invokeClass, String message, String detail) {
        return new WorkflowPublishException(invokeClass, ResponseCode.PUBLISH_FAILED, message, detail);
    }

    public static WorkflowPublishException buildDraftMissingException(Class<?> invokeClass, String message) {
        return new WorkflowPublishException(invokeClass, ResponseCode.PUBLISH_DRAFT_MISSING, message, null);
    }

    public static WorkflowPublishException buildUnchangedException(Class<?> invokeClass, String message) {
        return new WorkflowPublishException(invokeClass, ResponseCode.PUBLISH_UNCHANGED, message, null);
    }

    public static WorkflowPublishException buildBuildingFailedException(Class<?> invokeClass, Throwable cause, Object... params) {
        return new WorkflowPublishException(invokeClass, ResponseCode.PUBLISH_BUILD_FAILED, cause, params);
    }
}
