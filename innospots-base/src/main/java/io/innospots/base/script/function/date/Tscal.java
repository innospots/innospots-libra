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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

/**
 * 时间戳的计算
 * HH:mm:ss
 *
 * @author Smars
 * @date 2021/9/4
 */
public class Tscal {


    /**
     * @param timeUnit @see java.time.temporal.ChronoUnit
     * @param range
     * @param time
     * @return
     */
    public static Long plusTs(String timeUnit, Long range, Object time) {
        Instant instant = null;
        if (time instanceof Long) {
            instant = Instant.ofEpochMilli((Long) time);
        } else if (time instanceof TemporalAccessor) {
            instant = Instant.from((TemporalAccessor) time);
        } else if (time instanceof String && ((String) time).matches("[\\d]+")) {
            instant = Instant.ofEpochMilli(Long.parseLong(time.toString()));
        } else {
            return 0L;
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
                .plus(range, ChronoUnit.valueOf(timeUnit))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
