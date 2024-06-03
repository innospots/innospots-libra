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
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.workflow.core.debug.FlowNodeDebugger;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.innospots.base.model.response.InnospotsResponse.success;

/**
 * @author Smars
 * @date 2021/3/12
 */
@RequestMapping(PathConstant.ROOT_PATH + "test")
@RestController
@Tag(name = "Workflow Webhook Test")
public class WebhookTestEndpoint {


    private FlowNodeDebugger flowNodeDebugger;


    public WebhookTestEndpoint(FlowNodeDebugger flowNodeDebugger) {
        this.flowNodeDebugger = flowNodeDebugger;
    }

    @PostMapping("webhook/{flowKey}")
    @Operation(summary = "post webhook")
    public WorkflowResponse eventPost(@Parameter(required = true) @PathVariable String flowKey,
                                      @Parameter(required = false) @RequestParam Map<String, Object> params,
                                      @Parameter(required = false) @RequestHeader Map<String, Object> headers,
                                      @Parameter(required = false) @RequestBody Map<String, Object> body) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("headers", headers);
        payload.put("params", params);
        payload.put("body", body);
        return flowNodeDebugger.testWebhook(flowKey, payload);
    }

    @GetMapping("webhook/{flowKey}")
    @Operation(summary = "get webhook")
    public WorkflowResponse eventGet(@Parameter(required = true) @PathVariable String flowKey,
                                     @Parameter(required = false) @RequestParam(required = false) Map<String, Object> params,
                                     @Parameter(required = false) @RequestHeader(required = false) Map<String, Object> headers) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("headers", headers);
        payload.put("params", params);
        payload.put("body", Collections.emptyMap());
        return flowNodeDebugger.testWebhook(flowKey, payload);
    }


    @PostMapping(value = "{flowKey}")
    @Operation(summary = "post webhook upload files")
    public InnospotsResponse<String> eventPost(
            @Parameter(required = true) @PathVariable String flowKey,
            @Parameter(required = false) @RequestHeader Map<String, Object> headers,
            @RequestParam Map<String, Object> requestParams
    ) {
        System.out.println(requestParams);
        return success();
    }


}
