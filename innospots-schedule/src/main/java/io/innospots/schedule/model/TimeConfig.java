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

package io.innospots.schedule.model;

import io.innospots.base.exception.ConfigException;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.base.quartz.TimePeriod;
import io.innospots.base.utils.time.CronUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
@Getter
@Setter
@Slf4j
public class TimeConfig {

    private String runTime;

    private TimePeriod timePeriod;

    private List<String> periodTimes;


    private String getRunTimesFormat(ScheduleMode scheduleMode) {
        if (ScheduleMode.ONCE.equals(scheduleMode)) {
            return "yyyy-MM-dd HH:mm";
        } else {
            return "HH:mm";
        }
    }


    public String cronExpression(ScheduleMode scheduleMode) {
        String cronExpression = null;
        try {
            LocalTime localTime = this.runTime == null ? null : LocalTime.parse(this.runTime, DateTimeFormatter.ofPattern(this.getRunTimesFormat(scheduleMode)));
            cronExpression = CronUtils.createCronExpression(this.timePeriod, this.periodTimes, localTime);
            log.info("cron expression:{}", cronExpression);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw ConfigException.buildParamException(this.getClass(), "cronExpression is error, " + e.getMessage(),this.toString());
        }
        return cronExpression;
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
