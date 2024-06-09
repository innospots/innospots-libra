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

import io.innospots.workflow.core.node.executor.TriggerNode;
import io.innospots.workflow.core.instance.model.NodeInstance;

import java.util.concurrent.TimeUnit;

/**
 * the trigger will be executed by cycle timing
 *
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/11
 */
public class CycleTimerNode extends TriggerNode {

    public static final String FIELD_TIME_UNIT = "time_unit";
    public static final String FIELD_TIME_INTERVAL = "time_interval";

    private TimeUnit timeUnit;

    private Integer timeInterval;

    @Override
    protected void initialize() {
        validFieldConfig(FIELD_TIME_UNIT);
        this.timeInterval = validInteger(FIELD_TIME_INTERVAL);
        timeUnit = TimeUnit.valueOf(validString(FIELD_TIME_UNIT));
        triggerInfo.put(FIELD_TIME_INTERVAL, timeInterval);
        triggerInfo.put(FIELD_TIME_UNIT, timeUnit);
    }


    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Integer getTimeInterval() {
        return timeInterval;
    }


}
