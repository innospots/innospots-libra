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

import io.innospots.base.utils.time.DateTimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 将日期 yyyy-mm-ddd进行增减操作
 *
 * @author Smars
 * @date 2021/9/4
 */
public class Dcal {

    /**
     * 将日期增加指定时间
     *
     * @param numberOfDay 增加值可以为负值
     * @param date
     * @return
     */
    public static String plusDay(Long numberOfDay, Object date) {
        LocalDate localDate = null;
        if (date instanceof String) {
            localDate = LocalDate.parse((String) date, DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_DATE_PATTERN));
        } else if (date instanceof Date) {
            localDate = LocalDate.from(Instant.ofEpochMilli(((Date) date).getTime()));
        } else if (date instanceof LocalDate) {
            localDate = (LocalDate) date;
        } else {
            return null;
        }

        localDate = localDate.plus(numberOfDay, ChronoUnit.DAYS);
        return localDate.format(DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_DATE_PATTERN));
    }
}
