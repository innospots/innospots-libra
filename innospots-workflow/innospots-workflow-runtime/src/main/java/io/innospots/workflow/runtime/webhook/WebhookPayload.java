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

package io.innospots.workflow.runtime.webhook;

import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.runtime.webhook.FlowWebhookConfig;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.io.InputStreamSource;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/19
 */
@Getter
@Setter
public class WebhookPayload {

    private String flowKey;

    private Map<String, Object> headers;

    private Map<String, Object> params;

    private Map<String, Object> body;

    private Map<String, InputStreamSource> files;

    private String uri;

    private FlowWebhookConfig.RequestMethod requestMethod;

    private String location;

    public ExecutionInput toExecutionInput() {
        ExecutionInput executionInput = new ExecutionInput();
        Map<String, Object> input = new HashMap<>();
        input.put("headers", headers);
        input.put("params", params);
        input.put("body", body);
        executionInput.addInput(input);
        if (MapUtils.isNotEmpty(files)) {
            for (Map.Entry<String, InputStreamSource> entry : files.entrySet()) {
                ExecutionResource executionResource = new ExecutionResource();
                if (entry.getValue() instanceof MultipartFile) {
                    MultipartFile multipartFile = (MultipartFile) entry.getValue();
                    executionResource.setResourceName(multipartFile.getOriginalFilename());
                    executionResource.setMimeType(multipartFile.getContentType());
                }
                executionResource.setInputStreamSource(entry.getValue());
                executionResource.setExecutionCache(true);

                executionInput.addResource(executionResource);
            }
        }//end if
        return executionInput;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("flowKey='").append(flowKey).append('\'');
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", requestMethod=").append(requestMethod);
        sb.append(", location='").append(location).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
