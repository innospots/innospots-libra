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

package io.innospots.workflow.server.schedule;

import io.innospots.base.events.EventBusCenter;
import io.innospots.base.watcher.AbstractWatcher;
import io.innospots.server.base.registry.ServiceRegistryHolder;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.scheduled.ScheduledNodeExecution;
import io.innospots.workflow.runtime.scheduled.NodeExecutionEventBody;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/9
 */
public class ScheduledNodeExecutionWatcher extends AbstractWatcher {

    private IScheduledNodeExecutionOperator scheduledNodeExecutionOperator;


    public ScheduledNodeExecutionWatcher(IScheduledNodeExecutionOperator scheduledNodeExecutionOperator) {
        this.scheduledNodeExecutionOperator = scheduledNodeExecutionOperator;
    }

    @Override
    public int execute() {

        if (!ServiceRegistryHolder.isRegistry()) {
            return 0;
        }
        String serverKey = ServiceRegistryHolder.getCurrentServer().getServerKey();

        List<ScheduledNodeExecution> scheduledNodeExecutions = scheduledNodeExecutionOperator.scanArrivalTimeExecutions(
                null, serverKey, ServiceRegistryHolder.currentShardingKeys(InnospotsWorkflowProperties.MAX_SHARDING_KEY));

        if (CollectionUtils.isEmpty(scheduledNodeExecutions)) {
            return 15;
        }

        for (ScheduledNodeExecution scheduledNodeExecution : scheduledNodeExecutions) {
            NodeExecutionEventBody eventBody = new NodeExecutionEventBody(scheduledNodeExecution);
            EventBusCenter.getInstance().asyncPost(eventBody);
        }

        return 0;
    }
}
