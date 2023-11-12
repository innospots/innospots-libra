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


    /**
     * 周期单位
     */
    public static final String FIELD_PERIOD_UNIT = "period_unit";


    /**
     * 时间周期
     */
//    public static final String FIELD_PERIOD_TIME = "period_times";

    public static final String FIELD_PERIOD_DAY_TIME = "period_day_times";

    public static final String FIELD_PERIOD_MINUTE_TIME = "period_minute_times";

    public static final String FIELD_PERIOD_HOUR_TIME = "period_hour_times";

    public static final String FIELD_PERIOD_WEEK_TIME = "period_week_times";

    public static final String FIELD_PERIOD_MONTH_TIME = "period_month_times";

    public static final String FIELD_PERIOD_TIME_VALUES = "period_time_values";

    /**
     * 运行时间
     */
    public static final String FIELD_RUN_TIME = "run_time";


    public static final String FIELD_RUN_DATE_TIME = "run_date_time";

    /**
     * 调度模式
     */
    public static final String FIELD_SCHEDULE_MODE = "schedule_mode";

    private String cronExpression;


    @Override
    protected void initialize() {

        String scheduleModeStr = valueString(FIELD_SCHEDULE_MODE);
        ScheduleMode scheduleMode = scheduleModeStr != null ? Enums.getIfPresent(ScheduleMode.class, scheduleModeStr).orNull() : null;
        String runDateTime = null;
        eventBody.put(FIELD_SCHEDULE_MODE, scheduleMode);

        if (scheduleMode == ScheduleMode.ONCE) {
            validFieldConfig(FIELD_RUN_DATE_TIME);
            runDateTime = valueString(FIELD_RUN_DATE_TIME);
            eventBody.put(FIELD_RUN_TIME, runDateTime);
            return;
        }



        String periodUnitStr = valueString(FIELD_PERIOD_UNIT);
        TimePeriod timePeriod = periodUnitStr != null ? Enums.getIfPresent(TimePeriod.class, periodUnitStr).orNull() : null;
        if (timePeriod == null) {
            throw ConfigException.buildMissingException(this.getClass(), FIELD_PERIOD_UNIT);
        }
        String periodTimesStr = null;
        String runTime = valueString(FIELD_RUN_TIME);
        List<String> periodTimes = null;
        switch (timePeriod) {
            case MONTH:
                periodTimesStr = valueString(FIELD_PERIOD_MONTH_TIME);
                validFieldConfig(FIELD_RUN_TIME);
                break;
            case WEEK:
                periodTimesStr = valueString(FIELD_PERIOD_WEEK_TIME);
                validFieldConfig(FIELD_RUN_TIME);
                break;
            case DAY:
                periodTimesStr = valueString(FIELD_PERIOD_DAY_TIME);
                validFieldConfig(FIELD_RUN_TIME);
                break;
            case HOUR:
                periodTimesStr = valueString(FIELD_PERIOD_HOUR_TIME);
                break;
            case MINUTE:
                periodTimesStr = valueString(FIELD_PERIOD_MINUTE_TIME);
                break;
            default:
                break;
        }

        if (runTime != null) {
            eventBody.put(FIELD_RUN_TIME, runTime);
        }

        if (StringUtils.isNotEmpty(periodTimesStr)) {
            periodTimes = Arrays.stream(periodTimesStr.split(","))
                    .map(String::valueOf).collect(Collectors.toList());
        } else {
            periodTimes = new ArrayList<>();
        }


        eventBody.put(FIELD_PERIOD_UNIT, timePeriod);

        eventBody.put(FIELD_PERIOD_TIME_VALUES, periodTimes);

        cronExpression = cronExpression();

        eventBody.put("cron_expression", cronExpression);

        log.info("cronTimeNode: {} , {}", this.nodeKey(), eventBody);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
        nodeExecution.setMessage(eventBody.toString());
    }

    public ScheduleMode scheduleMode() {
        return (ScheduleMode) eventBody.get(FIELD_SCHEDULE_MODE);
    }

    public TimePeriod timePeriod() {
        return (TimePeriod) eventBody.get(FIELD_PERIOD_UNIT);
    }

    public List<String> periodTimes() {
        return eventBody.containsKey(FIELD_PERIOD_TIME_VALUES) ?
                (List<String>) eventBody.get(FIELD_PERIOD_TIME_VALUES) : Collections.emptyList();
    }

    public String runTimes() {
        return (String) eventBody.get(FIELD_RUN_TIME);
    }

    public Date startTime() {
        return DateTimeUtils.parseDate(this.runTimes(), this.getRunTimesFormat());
    }


    private String getRunTimesFormat() {
        ScheduleMode scheduleMode = scheduleMode();
        if (ScheduleMode.ONCE.equals(scheduleMode)) {
            return "yyyy-MM-dd HH:mm";
        } else {
            return "HH:mm";
        }
    }

    /**
     * build crontab expression
     *
     * @return
     */
    public String cronExpression() {
        try {
            if (cronExpression == null) {
                LocalTime localTime = this.runTimes() == null ? null : LocalTime.parse(this.runTimes(), DateTimeFormatter.ofPattern(this.getRunTimesFormat()));
                cronExpression = CronUtils.createCronExpression(this.timePeriod(), this.periodTimes(), localTime);
                log.info("cronTimeNode: {}, cron expression:{}", this.nodeKey(), cronExpression);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw ConfigException.buildParamException(this.getClass(), "cronExpression is error, " + e.getMessage() + " , config: " + eventBody);
        }

        return cronExpression;
    }

}
