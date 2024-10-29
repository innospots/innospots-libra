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

package io.innospots.script.base.function.date;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 基于时间戳，抽取对应的时间单元值
 *
 * @author Smars
 * @date 2021/9/5
 */
public class TsEx {


    public static Long extract(String timeUnit, Object input) {
        LocalDateTime dateTime = null;
        long ts = 0;
        if (input instanceof String && ((String) input).matches("[\\d]+")) {
            ts = Long.parseLong((String) input);
        } else if (input instanceof Number) {
            ts = Long.parseLong(input.toString());
        }
        if (ts == 0) {
            return ts;
        }

        dateTime = new Date(ts).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        ChronoUnit unit = ChronoUnit.valueOf(timeUnit);
        long v = 0;
        switch (unit) {
            case SECONDS:
                v = dateTime.getLong(ChronoField.SECOND_OF_MINUTE);
                break;
            case DAYS:
                v = dateTime.get(ChronoField.DAY_OF_MONTH);
                break;
            case MINUTES:
                v = dateTime.get(ChronoField.MINUTE_OF_HOUR);
                break;
            case HOURS:
                v = dateTime.get(ChronoField.HOUR_OF_DAY);
                break;
            case WEEKS:
                v = dateTime.get(ChronoField.DAY_OF_WEEK);
                break;
            case YEARS:
                v = dateTime.get(ChronoField.YEAR);
                break;
            case MONTHS:
                v = dateTime.get(ChronoField.MONTH_OF_YEAR);
                break;
            default:
                break;
        }

        return v;

    }
}
