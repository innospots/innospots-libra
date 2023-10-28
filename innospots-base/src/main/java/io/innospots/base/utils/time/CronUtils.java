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

package io.innospots.base.utils.time;

import io.innospots.base.quartz.TimePeriod;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.List;

/**
 * CronUtils
 *
 * @author Wren
 * @date 2022/4/3-11:37
 */
@Slf4j
public class CronUtils {


    /**
     * 构建Cron表达式
     *
     * @param timePeriod  执行周期
     * @param periodTimes 周期次数
     * @param localTime   周期时间
     * @return String
     */
    public static String createCronExpression(TimePeriod timePeriod, List<String> periodTimes, LocalTime localTime) {
        StringBuffer cronExp = new StringBuffer("");

        if (null == timePeriod) {
            log.error("TimePeriod is null");
            return cronExp.toString();
        }

        if (timePeriod == TimePeriod.MINUTE) {
            cronExp.append("0 0/");
            cronExp.append(periodTimes.get(0));
            cronExp.append(" * * * ? ");
            return cronExp.toString();
        }

        if (localTime != null) {
            //秒
            cronExp.append(localTime.getSecond()).append(" ");
            //分
            cronExp.append(localTime.getMinute()).append(" ");
        } else {
            //秒
            cronExp.append(0).append(" ");
            //分
            cronExp.append(0).append(" ");
        }

        //每小时
        if (TimePeriod.HOUR.equals(timePeriod)) {
            if (periodTimes != null && !periodTimes.isEmpty()) {
                if (periodTimes.size() == 1){
                    cronExp.append("0/").append(periodTimes.get(0));
                } else {
                    cronExp.append(String.join(",", periodTimes));
                }
                cronExp.append(" ");
            } else {
                cronExp.append("* ");
            }
            //日 月 周
            cronExp.append("* * ?");
            return cronExp.toString();
        } else {
            if (localTime != null) {
                cronExp.append(localTime.getHour()).append(" ");
            }
        }

        if (localTime == null) {
            return null;
        }

        if (TimePeriod.DAY.equals(timePeriod)) {
            //每天
            //日
            if (periodTimes != null && !periodTimes.isEmpty()) {
                //cronExp.append(String.join(",",periodTimes));
                //cronExp.append(" ");
                cronExp.append("1/").append(periodTimes.get(0)).append(" ");
            } else {
                cronExp.append("* ");
            }
            //月 周
            cronExp.append("* ?");
        } else if (TimePeriod.WEEK.equals(timePeriod)) {
            //按每周
            //一个月中第几天
            cronExp.append("? ");
            //月份
            cronExp.append("* ");
            //周
            if (periodTimes != null && !periodTimes.isEmpty()) {
                cronExp.append(String.join(",", periodTimes));
            } else {
                cronExp.append("*");
            }

        } else if (TimePeriod.MONTH.equals(timePeriod)) {
            //按每月
            //一个月中的哪几天
            if (periodTimes != null && !periodTimes.isEmpty()) {
                cronExp.append(String.join(",", periodTimes));
                cronExp.append(" ");
            } else {
                cronExp.append("* ");
            }
            //月
            cronExp.append("* ");
            //周
            cronExp.append("?");
        } else {
            log.error("TimePeriod not support:{}", timePeriod);
        }
        return cronExp.toString();
    }

}
