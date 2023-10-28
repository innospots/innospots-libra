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

package io.innospots.base.re.function.date;

import io.innospots.base.utils.time.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 将日期时间进行增减操作
 * 日期时间格式为：yyyy-MM-dd HH:mm:ss
 *
 * @author Smars
 * @date 2021/9/4
 */
public class Dtcal {

    public static String plusDateTime(String timeUnit, Long range, Object dateTime) {
        LocalDateTime localDateTime = null;
        if (dateTime instanceof String) {
            localDateTime = LocalDateTime.parse((String) dateTime, DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_DATETIME_PATTERN));
        } else if (dateTime instanceof Date) {
            localDateTime = LocalDateTime.from(((Date) dateTime).toInstant());
        } else if (dateTime instanceof LocalDateTime) {
            localDateTime = (LocalDateTime) dateTime;
        } else {
            return null;
        }
        localDateTime = localDateTime.plus(range, ChronoUnit.valueOf(timeUnit));

        return localDateTime.format(DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_DATETIME_PATTERN));
    }
}
