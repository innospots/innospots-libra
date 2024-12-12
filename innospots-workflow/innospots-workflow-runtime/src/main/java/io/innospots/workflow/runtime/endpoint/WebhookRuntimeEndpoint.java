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
import io.innospots.workflow.core.sse.FlowEmitter;
import io.innospots.workflow.runtime.webhook.WebhookPayload;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponse;
import io.innospots.workflow.runtime.container.WebhookRuntimeContainer;
import io.innospots.workflow.runtime.webhook.WebhookPayloadConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/12
 */

@RequestMapping(PathConstant.ROOT_PATH + PathConstant.WORKFLOW_RUNTIME_PATH)
@RestController
@Tag(name = "webhook runtime api")
public class WebhookRuntimeEndpoint {


    private WebhookRuntimeContainer webhookRuntimeContainer;

    public WebhookRuntimeEndpoint(WebhookRuntimeContainer webhookRuntimeContainer) {
        this.webhookRuntimeContainer = webhookRuntimeContainer;
    }

    @Operation(description = "get webhook response")
    @GetMapping("response/{contextId}")
    public WorkflowResponse getResponse(@PathVariable String contextId){
        return webhookRuntimeContainer.getResponseByContextId(contextId);
    }

    @PostMapping(value = "async/{path}")
    @Operation(description = "async post webhook")
    public WorkflowResponse asyncPost(
            HttpServletRequest request,
            @Parameter(required = true) @PathVariable String path,
            @Parameter(required = false) @RequestHeader Map<String, Object> headers,
            @RequestParam Map<String, Object> requestParams,
            @RequestBody Map<String, Object> body
    ) {
        WebhookPayload payload = WebhookPayloadConverter.convert(path,request,headers,requestParams,body);
        return webhookRuntimeContainer.asyncExecute(payload);
    }

    @PostMapping(value = "{path}")
    @Operation(description = "post webhook")
    public WorkflowResponse apiPost(
            HttpServletRequest request,
            @Parameter(required = true) @PathVariable String path,
            @Parameter(required = false) @RequestHeader Map<String, Object> headers,
            @RequestParam Map<String, Object> requestParams,
            @RequestBody Map<String, Object> body
    ) {
        WebhookPayload payload = WebhookPayloadConverter.convert(path,request,headers,requestParams,body);
        return webhookRuntimeContainer.execute(payload);
    }

    @PostMapping("v2/{path}")
    @Operation(description = "post webhook v2")
    public WorkflowResponse eventPost(@Parameter(required = true) @PathVariable String path,
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

    @GetMapping("{path}")
    @Operation(description = "get webhook")
    public WorkflowResponse apiGet(
            @Parameter(required = true) @PathVariable String path,
                                   HttpServletRequest request,
                                     @Parameter(required = false) @RequestParam(required = false) Map<String, Object> params,
                                     @Parameter(required = false) @RequestHeader(required = false) Map<String, Object> headers) {

        WebhookPayload payload = WebhookPayloadConverter.convert(path,request,headers,params,null);
        return webhookRuntimeContainer.execute(payload);
    }

    @GetMapping("stream/{flowKey}")
    @Operation(description = "api stream webhook")
    public SseEmitter apiStream(String flowExecutionId,String flowKey){
        return FlowEmitter.createResponseEmitter(flowExecutionId,flowKey);
    }

}
