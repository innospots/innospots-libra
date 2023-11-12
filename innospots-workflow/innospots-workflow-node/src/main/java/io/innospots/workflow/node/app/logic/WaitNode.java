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

package io.innospots.workflow.node.app.logic;


import io.innospots.base.quartz.TimePeriod;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.execution.operator.IScheduledNodeExecutionOperator;
import io.innospots.workflow.core.execution.model.scheduled.ScheduledNodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2021/4/24
 */
public class WaitNode extends BaseNodeExecutor {

    public static final String FIELD_PERIOD_UNIT = "time_period";
    public static final String FIELD_PERIOD_TIME = "interval_time";
    public static final String FIELD_RUNNING_DATE_TIME = "running_date_time";
    public static final String FIELD_DELAY_MODE = "delay_mode";

    //protected ScheduledNodeContextRecordApiClient scheduledNodeContextRecordApiClient;

    private TimePeriod timePeriod;
    private Integer intervalTime;
    private String runningDateTime;
    private DelayMode delayMode;
    private IScheduledNodeExecutionOperator scheduledNodeExecutionOperator;

    @Override
    protected void initialize() {
        delayMode = DelayMode.valueOf(valueString(FIELD_DELAY_MODE));
        if (delayMode == DelayMode.PERIOD) {
            timePeriod = TimePeriod.valueOf(valueString(FIELD_PERIOD_UNIT));
            intervalTime = valueInteger(FIELD_PERIOD_TIME);
        } else {
            runningDateTime = valueString(FIELD_RUNNING_DATE_TIME);
        }

        scheduledNodeExecutionOperator = BeanContextAwareUtils.getBean(IScheduledNodeExecutionOperator.class);
        //scheduledNodeContextRecordApiClient = ApplicationContextUtils.getBean(ScheduledNodeContextRecordApiClient.class);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            nodeOutput.setResults(executionInput.getData());
        }
        nodeExecution.addOutput(nodeOutput);
        nodeExecution.setStatus(ExecutionStatus.PENDING);
        ScheduledNodeExecution scheduledNodeExecution = ScheduledNodeExecution.build(nodeExecution);
        scheduledNodeExecution.setScheduledTime(scheduledDateTime());
        scheduledNodeExecutionOperator.insert(scheduledNodeExecution);
    }

    private LocalDateTime scheduledDateTime() {
        LocalDateTime dateTime;
        if (delayMode == DelayMode.FIXED) {
            dateTime = DateTimeUtils.normalizeDateTime(runningDateTime);
        } else {
            dateTime = LocalDateTime.now();
            switch (timePeriod) {
                case DAY:
                    dateTime = dateTime.plusDays(intervalTime);
                    break;
                case MONTH:
                    dateTime = dateTime.plusMonths(intervalTime);
                    break;
                case WEEK:
                    dateTime = dateTime.plusWeeks(intervalTime);
                    break;
                case MINUTE:
                    dateTime = dateTime.plusMinutes(intervalTime);
                    break;
                case HOUR:
                    dateTime = dateTime.plusHours(intervalTime);
                default:
                    break;
            }
        }

        return dateTime;
    }

    @Override
    protected void end(NodeExecution nodeExecution, FlowExecution flowExecution) {
        flowExecution.resetCurrentNodeKey(this.nodeKey());
        flowExecution.setStatus(ExecutionStatus.PENDING);
    }

    @Override
    protected void after(NodeExecution nodeExecution) {
        if (nodeExecutionListeners != null) {
            for (INodeExecutionListener nodeExecutionListener : nodeExecutionListeners) {
                nodeExecutionListener.complete(nodeExecution);
            }
        }
    }

    /**
     *
     */
    public enum DelayMode {
        /**
         *
         */
        PERIOD,
        FIXED;
    }

}
