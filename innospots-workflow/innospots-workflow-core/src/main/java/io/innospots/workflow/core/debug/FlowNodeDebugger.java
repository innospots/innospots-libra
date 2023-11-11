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

package io.innospots.workflow.core.debug;


import io.innospots.workflow.core.execution.ExecutionResource;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.node.NodeExecutionDisplay;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/12
 */
public interface FlowNodeDebugger {


    Map<String, NodeExecutionDisplay> execute(Long workflowInstanceId, String nodeKey,
                                              List<Map<String, Object>> inputs);

    NodeExecutionDisplay execute(DebugPayload debugPayload);

    ExecutionResource updateTestFile(MultipartFile uploadFile, boolean force);

    FlowExecution currentExecuting(Long workflowInstanceId);

    Map<String, NodeExecutionDisplay> readNodeExecutions(Long workflowInstanceId,
                                                         List<String> nodeKeys);

    WorkflowResponse testWebhook(String flowKey, Map<String, Object> input);

    WorkflowResponse testWebhook(String flowKey, List<Map<String, Object>> inputs);

    FlowExecution stop(String flowExecutionId);

    FlowExecution stopByFlowKey(String flowExecutionId);

}
