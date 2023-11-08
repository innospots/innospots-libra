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

package io.innospots.workflow.core.execution.scheduled;


import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.node.NodeExecution;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDateTime;

import static io.innospots.workflow.core.config.InnospotWorkflowProperties.MAX_SHARDING_KEY;


/**
 * schedule node execution that will be execute when the schedule time arrives
 *
 * @author Smars
 * @date 2021/9/19
 */
@Getter
@Setter
public class ScheduledNodeExecution {

    private String nodeExecutionId;

    private String flowExecutionId;

    private LocalDateTime scheduledTime;

    private int shardingKey;

    private String serverKey;

    private String nodeKey;

    private ExecutionStatus status;

    private String message;

    private Integer projectId;

    private Integer orgId;

    private LocalDateTime updatedTime;

    public ScheduledNodeExecution() {
    }

    public static ScheduledNodeExecution build(NodeExecution nodeExecution) {
        ScheduledNodeExecution scheduledNodeExecution = new ScheduledNodeExecution();
        scheduledNodeExecution.setNodeExecutionId(nodeExecution.getNodeExecutionId());
        scheduledNodeExecution.setFlowExecutionId(nodeExecution.getFlowExecutionId());
        scheduledNodeExecution.setNodeKey(nodeExecution.getNodeKey());
        scheduledNodeExecution.setOrgId(CCH.organizationId());
        scheduledNodeExecution.setProjectId(CCH.projectId());
        scheduledNodeExecution.setStatus(ExecutionStatus.PENDING);
        scheduledNodeExecution.setShardingKey(RandomUtils.nextInt(0, MAX_SHARDING_KEY));
        return scheduledNodeExecution;
    }
}
