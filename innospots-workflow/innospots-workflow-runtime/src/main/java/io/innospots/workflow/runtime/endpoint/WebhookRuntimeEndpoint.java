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

package io.innospots.workflow.runtime.endpoint;


import io.innospots.base.constant.PathConstant;
import io.innospots.workflow.core.webhook.WebhookPayload;
import io.innospots.workflow.core.webhook.WorkflowResponse;
import io.innospots.workflow.runtime.container.WebhookRuntimeContainer;
import io.innospots.workflow.runtime.utils.WebhookPayloadConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/12
 */

@RequestMapping(PathConstant.ROOT_PATH + PathConstant.RUNTIME_PATH)
@RestController
@Tag(name = "Webhook Runtime Api")
public class WebhookRuntimeEndpoint {


    private WebhookRuntimeContainer webhookRuntimeContainer;

    public WebhookRuntimeEndpoint(WebhookRuntimeContainer webhookRuntimeContainer) {
        this.webhookRuntimeContainer = webhookRuntimeContainer;
    }

    @PostMapping(value = "{flowKey}")
    @Operation(summary = "post webhook")
    public WorkflowResponse apiPost(
            HttpServletRequest request,
            @Parameter(required = true) @PathVariable String flowKey,
            @Parameter(required = false) @RequestHeader Map<String, Object> headers,
            @RequestParam Map<String, Object> requestParams,
            @RequestPart(required = false) MultipartFile[] multipartFiles,
            @RequestBody Map<String, Object> body
    ) {
        WebhookPayload payload = WebhookPayloadConverter.convert(flowKey,request,headers,requestParams,body);
        return webhookRuntimeContainer.execute(payload);
    }

    @PostMapping("v2/{flowKey}")
    @Operation(summary = "post webhook")
    public WorkflowResponse eventPost(@Parameter(required = true) @PathVariable String flowKey,
                                      @Parameter(required = false) @RequestParam Map<String, Object> params,
                                      @Parameter(required = false) @RequestHeader Map<String, Object> headers,
                                      @Parameter(required = false) @RequestBody Map<String, Object> body) {


        Map<String, Object> payload = new HashMap<>();
        payload.put("headers", headers);
        payload.put("params", params);
        payload.put("body", body);
        return null;
        //return flowNodeDebugger.testWebhook(flowKey,payload);
    }

    @GetMapping("{flowKey}")
    @Operation(summary = "get webhook")
    public WorkflowResponse apiGet(
            @Parameter(required = true) @PathVariable String flowKey,
                                   HttpServletRequest request,
                                     @Parameter(required = false) @RequestParam(required = false) Map<String, Object> params,
                                     @Parameter(required = false) @RequestHeader(required = false) Map<String, Object> headers) {

        WebhookPayload payload = WebhookPayloadConverter.convert(flowKey,request,headers,params,null);
        return webhookRuntimeContainer.execute(payload);
    }


}
