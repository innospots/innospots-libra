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

package io.innospots.base.script.function.date;

import io.innospots.base.utils.time.DateTimeUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 时间的计算
 * HH:mm:ss
 *
 * @author Smars
 * @date 2021/9/4
 */
public class Tcal {


    /**
     * @param timeUnit 时间单元包括：HOURS, MINUTES,SECONDS @see java.time.temporal.ChronoUnit
     * @param range
     * @param time
     * @return
     */
    public static String plusTime(String timeUnit, Long range, Object time) {
        ChronoUnit chronoUnit = ChronoUnit.valueOf(timeUnit);
        if (chronoUnit != ChronoUnit.SECONDS &&
                chronoUnit != ChronoUnit.MINUTES &&
                chronoUnit != ChronoUnit.HOURS
        ) {
            return null;
        }
        LocalTime localTime = null;
        if (time instanceof String) {
            localTime = LocalTime.parse((String) time, DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_TIME_PATTERN));
        } else if (time instanceof LocalTime) {
            localTime = (LocalTime) time;
        } else if (time instanceof Date) {
            localTime = LocalTime.from(((Date) time).toInstant());
        } else {
            return null;
        }

        localTime = localTime.plus(range, ChronoUnit.valueOf(timeUnit));

        return localTime.format(DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_TIME_PATTERN));
    }
}
