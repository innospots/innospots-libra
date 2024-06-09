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

package io.innospots.base.quartz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Enums;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.utils.time.CronUtils;
import io.innospots.base.utils.time.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Getter
@Setter
@Slf4j
public class TimeConfig {

    /**
     * 周期单位
     */
    public static final String FIELD_PERIOD_UNIT = "period_unit";

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


    private String runTime;

    private TimePeriod timePeriod;

    private List<String> periodTimes;

    @JsonIgnore
    private ScheduleMode scheduleMode;


    public static String getRunTimesFormat(ScheduleMode scheduleMode) {
        if (ScheduleMode.ONCE.equals(scheduleMode)) {
            return "yyyy-MM-dd HH:mm";
        } else {
            return "HH:mm";
        }
    }

    public Date runDateTime() {
        return DateTimeUtils.parseDate(this.runTime, getRunTimesFormat(this.scheduleMode));
    }


    public String cronExpression(ScheduleMode scheduleMode) {
        String cronExpression = null;
        try {
            LocalTime localTime = this.runTime == null ? null : LocalTime.parse(this.runTime, DateTimeFormatter.ofPattern(this.getRunTimesFormat(scheduleMode)));
            cronExpression = CronUtils.createCronExpression(this.timePeriod, this.periodTimes, localTime);
            log.info("cron expression:{}", cronExpression);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw ConfigException.buildParamException(this.getClass(), "cronExpression is error, " + e.getMessage(), this.toString());
        }
        return cronExpression;
    }

    public String cronExpression(){
        return cronExpression(this.scheduleMode);
    }

    public static TimeConfig build(Map<String, Object> params, Class<?> targetClass) {

        String scheduleModeStr = valueString(FIELD_SCHEDULE_MODE, params);
        ScheduleMode scheduleMode = scheduleModeStr != null ? Enums.getIfPresent(ScheduleMode.class, scheduleModeStr).orNull() : null;
        String runDateTime = null;
        TimeConfig timeConfig = new TimeConfig();
        timeConfig.scheduleMode = scheduleMode;

        if (scheduleMode == ScheduleMode.ONCE) {
            runDateTime = valueString(FIELD_RUN_DATE_TIME, params);
            assertNotNull(runDateTime, FIELD_RUN_DATE_TIME,targetClass);
            timeConfig.runTime = runDateTime;
            return timeConfig;
        }

        String periodUnitStr = valueString(FIELD_PERIOD_UNIT, params);
        TimePeriod timePeriod = periodUnitStr != null ? Enums.getIfPresent(TimePeriod.class, periodUnitStr).orNull() : null;
        timeConfig.timePeriod = timePeriod;
        assertNotNull(timePeriod, FIELD_PERIOD_UNIT,targetClass);

        String periodTimesStr = null;
        String runTime = valueString(FIELD_RUN_TIME, params);
        timeConfig.runTime = runTime;

        List<String> periodTimes = null;
        switch (timePeriod) {
            case MONTH:
                periodTimesStr = valueString(FIELD_PERIOD_MONTH_TIME, params);
                assertNotNull(runTime, FIELD_RUN_TIME,targetClass);
                break;
            case WEEK:
                periodTimesStr = valueString(FIELD_PERIOD_WEEK_TIME, params);
                assertNotNull(runTime, FIELD_RUN_TIME,targetClass);
                break;
            case DAY:
                periodTimesStr = valueString(FIELD_PERIOD_DAY_TIME, params);
                assertNotNull(runTime, FIELD_RUN_TIME,targetClass);
                break;
            case HOUR:
                periodTimesStr = valueString(FIELD_PERIOD_HOUR_TIME, params);
                break;
            case MINUTE:
                periodTimesStr = valueString(FIELD_PERIOD_MINUTE_TIME, params);
                break;
            default:
                break;
        }


        if (StringUtils.isNotEmpty(periodTimesStr)) {
            periodTimes = Arrays.stream(periodTimesStr.split(","))
                    .map(String::valueOf).collect(Collectors.toList());
        } else {
            periodTimes = new ArrayList<>();
        }
        timeConfig.periodTimes = periodTimes;

        return timeConfig;
    }

    private static String valueString(String key, Map<String, Object> params) {
        return params.get(key) == null ? null : String.valueOf(params.get(key));
    }

    private static void assertNotNull(Object value, String field, Class<?> targetClass) {
        if (value == null) {
            throw ConfigException.buildParamException(targetClass, field + " value is null.");
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("runTime='").append(runTime).append('\'');
        sb.append(", timePeriod=").append(timePeriod);
        sb.append(", periodTimes=").append(periodTimes);
        sb.append('}');
        return sb.toString();
    }
}
