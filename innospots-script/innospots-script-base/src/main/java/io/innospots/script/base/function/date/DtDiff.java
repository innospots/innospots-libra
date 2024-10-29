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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 比较两个日期时间差
 *
 * @author Smars
 * @date 2021/9/5
 */
public class DtDiff {


    public static long diffDt(String timeUnit, Object startDt, Object endDt) {
        LocalDateTime start = DateTimeUtils.normalizeDateTime(startDt);
        LocalDateTime end = DateTimeUtils.normalizeDateTime(endDt);
        return ChronoUnit.valueOf(timeUnit).between(start, end);
    }
}
