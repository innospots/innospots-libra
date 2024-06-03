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
import io.innospots.base.quartz.QuartzScheduleManager;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.workflow.runtime.config.WorkflowRuntimeProperties;
import io.innospots.workflow.runtime.container.RunTimeContainerManager;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.manage.FlowManager;
import io.innospots.workflow.runtime.server.WorkflowWebhookServer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.innospots.base.model.response.InnospotsResponse.success;

/**
 * @author Smars
 * @date 2021/3/23
 */
@RequestMapping(PathConstant.ROOT_PATH + "workflow/management")
@RestController
@Tag(name = "Workflow Management Endpoint")
public class WorkflowManagementEndpoint {

    private FlowManager flowManager;

    private RunTimeContainerManager containerManager;

    private QuartzScheduleManager quartzScheduleManager;

    private WorkflowRuntimeProperties workflowRuntimeProperties;

    public WorkflowManagementEndpoint(FlowManager flowManager, RunTimeContainerManager containerManager, QuartzScheduleManager quartzScheduleManager,
                                      WorkflowRuntimeProperties workflowRuntimeProperties) {
        this.flowManager = flowManager;
        this.containerManager = containerManager;
        this.quartzScheduleManager = quartzScheduleManager;
        this.workflowRuntimeProperties = workflowRuntimeProperties;
    }

    @PostMapping("load/{workInstanceId}/{revision}")
    @Operation(summary = "load workflow instance")
    public InnospotsResponse<Flow> load(@PathVariable Long workInstanceId, @PathVariable Integer revision) {
        Flow flow = flowManager.loadFlow(workInstanceId, revision);
        return success(flow);
    }

    @PostMapping("load/{workInstanceId}")
    @Operation(summary = "load latest workflow instance")
    public InnospotsResponse<Flow> load(@PathVariable Long workInstanceId) {
        Flow flow = flowManager.loadFlow(workInstanceId);
        return success(flow);
    }

    @PutMapping("clear/{workInstanceId}/{revision}")
    @Operation(summary = "clear workflow instance cache")
    public InnospotsResponse<Boolean> clear(@PathVariable Long workInstanceId, @PathVariable Integer revision) {
        return success(flowManager.clear(workInstanceId, revision));
    }

    @PutMapping("clear/{workInstanceId}")
    @Operation(summary = "clear latest workflow instance")
    public InnospotsResponse<Boolean> clear(@PathVariable Long workInstanceId) {
        return success(flowManager.clear(workInstanceId));
    }

    @GetMapping("info/{workInstanceId}/{revision}")
    @Operation(summary = "show workflow instance")
    public InnospotsResponse<Flow> info(@PathVariable Long workInstanceId, @PathVariable Integer revision) {
        return success(flowManager.findFlow(workInstanceId, revision));
    }


    @GetMapping("info/{workInstanceId}")
    @Operation(summary = "show latest workflow instance")
    public InnospotsResponse<Flow> info(@PathVariable Long workInstanceId) {
        return success(flowManager.findFlow(workInstanceId));
    }

    @GetMapping("runtime")
    @Operation(summary = "published workflow in runtime")
    public InnospotsResponse<Map<String, Object>> runtime() {
        return success(containerManager.runtimeTriggers());
    }

    @GetMapping("schedule-infos")
    @Operation(summary = "published schedule workflow")
    public InnospotsResponse<List<Map<String, Object>>> scheduleInfo() {
        return success(quartzScheduleManager.schedulerInfo());
    }

    @GetMapping("webhook-address")
    @Operation(summary = "webhook address")
    public InnospotsResponse<Map<String, String>> apiAddress() {
        Map<String, String> flowInfo = new LinkedHashMap<>();
        String host = workflowRuntimeProperties.getHost();
        if (StringUtils.isEmpty(host)) {
            host = BeanContextAwareUtils.serverIpAddress();
        }
        flowInfo.put("webhookApiTest",
                "http://" +
                        host +
                        ":" + BeanContextAwareUtils.serverPort() + PathConstant.ROOT_PATH +
                        "test/webhook"
        );
        WorkflowWebhookServer webhookServer = BeanContextAwareUtils.getBean(WorkflowWebhookServer.class);
        flowInfo.put("webhookApiServer",
                "http://" +
                        host +
                        ":" + workflowRuntimeProperties.getPort() +
                        PathConstant.ROOT_PATH + PathConstant.RUNTIME_PATH
        );
        return InnospotsResponse.success(flowInfo);
    }


}
