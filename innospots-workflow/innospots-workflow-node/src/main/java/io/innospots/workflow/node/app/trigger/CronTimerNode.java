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

package io.innospots.workflow.node.app.trigger;

import com.google.common.base.Enums;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.base.quartz.TimeConfig;
import io.innospots.base.quartz.TimePeriod;
import io.innospots.base.utils.time.CronUtils;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.TriggerNode;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Raydian
 * @date 2021/1/3
 */
@Slf4j
public class CronTimerNode extends TriggerNode {

    private TimeConfig timeConfig;

    private String cronExpression;


    @Override
    protected void initialize() {
        timeConfig = TimeConfig.build(ni.getData(), this.getClass());
        cronExpression = cronExpression();
        triggerInfo.put(TimeConfig.class.getSimpleName(),timeConfig);

        triggerInfo.put("cronExpression", cronExpression);

        log.info("cronTimeNode: {} , {}", this.nodeKey(), triggerInfo);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
        nodeExecution.setMessage(triggerInfo.toString());
    }

    public ScheduleMode scheduleMode() {
        return timeConfig.getScheduleMode();
    }

    public Date startTime() {
        return timeConfig.runDateTime();
    }


    /**
     * build crontab expression
     *
     * @return
     */
    public String cronExpression() {
        return timeConfig.cronExpression();
    }

}
