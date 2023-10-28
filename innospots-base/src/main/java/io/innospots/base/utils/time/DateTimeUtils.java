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

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Raydian
 * @date 2020/12/14
 */
public class DateTimeUtils {


    public static String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 默认时间格式，HH:mm:ss
     */
    public static String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    public static String DEFAULT_SIMPLE_TIME_PATTERN = "HH:mm";

    /**
     * 默认日期时间格式，yyyy-MM-dd HH:mm:ss
     */
    public static String DEFAULT_DATETIME_PATTERN = DEFAULT_DATE_PATTERN + " " + DEFAULT_TIME_PATTERN;

    public static String DATETIME_MS_PATTERN = "yyyyMMddHHmmss.SSS";

    public static String DATETIME_DATA_PATTERN = "yyyyMMdd";

    /**
     * return time duration by human readable format
     * 返回格式为：xx hour, xx minute, xx second
     *
     * @param endTime
     * @param startTime
     * @return
     */
    public static String consume(long endTime, long startTime) {
        long consume = (endTime - startTime);
        long consumeSecond = consume / 1000;
        long hour = consumeSecond / 3600;
        long minute = (consumeSecond - hour * 3600) / 60;
        long second = consumeSecond - hour * 3600 - minute * 60;
        long millisecond = consume - consumeSecond * 1000;
        String time = "";

        if (hour > 0) {
            time += hour + " hours, ";
        }
        if (minute > 0 || hour > 0) {
            time += minute + " minutes, ";
        }
        if (second > 0 || minute > 0 || hour > 0) {
            time += second + " seconds, ";
        }
        if (millisecond >= 0 || second > 0 || minute > 0 || hour > 0) {
            time += millisecond + " ms.";
        }

        return time;
    }

    /**
     * 计算耗时
     *
     * @param endTime
     * @param startTime
     * @return
     */
    public static String prettyDuration(LocalDateTime endTime, LocalDateTime startTime) {
        return consume(endTime.toInstant(ZoneOffset.UTC).toEpochMilli(), startTime.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    /**
     * return time duration by human readable format
     * 返回格式为：xx hour, xx minute, xx second
     *
     * @param startTime
     * @return
     */
    public static String consume(long startTime) {
        return consume(System.currentTimeMillis(), startTime);
    }


    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = localDateTime.atZone(zoneId);
            return Date.from(zdt.toInstant());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String formatLocalDateTime(LocalDateTime dateTime, String datePattern) {
        if (dateTime == null) {
            return null;
        }

        DateTimeFormatter dateTimeFormatter = null;
        if (datePattern == null || datePattern.isEmpty()) {
            dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
        } else {
            dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);
        }
        return dateTimeFormatter.format(dateTime);
    }

    public static String formatDate(Date date, String datePattern) {
        FastDateFormat dateFormat;
        if (datePattern == null) {
            dateFormat = FastDateFormat.getInstance(DEFAULT_DATETIME_PATTERN);
        } else {
            dateFormat = FastDateFormat.getInstance(datePattern);

        }
        return dateFormat.format(date);
    }

    public static Date parseDate(String strDate, String pattern) {
        FastDateFormat dateFormat = FastDateFormat.getInstance(pattern);
        try {
            return dateFormat.parse(strDate);
        } catch (Exception pe) {
            return null;
        }
    }

    public static <T extends Date> String getDiffDayStr(T day1, T day2) {
        if (day1 == null || day2 == null) {
            return "---";
        }
        long diff = day1.getTime() - day2.getTime();
        if (diff < 0) {
            return "---";
        }
        diff = Math.abs(diff) / 1000;
        long day = diff / 3600 / 24;
        long hour = (diff - (day * 3600 * 24)) / 3600;
        long minu = diff % 3600 / 60;
        long second = diff % 3600 % 60;
        return (day == 0 ? "" : day + "天") + (hour == 0 ? "" : hour + "小时") + (minu == 0 ? "" : minu + "分钟") + (second == 0 ? "" : second + "秒");
    }

    public static int getDiffDay(Date day1, Date day2) {
        if (day1 == null || day2 == null) {
            return 0;
        }
        long diff = day1.getTime() - day2.getTime();
        diff = Math.abs(diff) / 1000;
        return Math.round(diff / (3600 * 24));
    }

    public static Date asDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            localDateTime = LocalDateTime.now();
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isToday(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
        String param = format.format(date);
        String now = format.format(new Date());
        if (now.equals(param)) {
            return true;
        }
        return false;
    }

    public static LocalDateTime normalizeDateTime(Object dateTime) {
        if (dateTime instanceof LocalDateTime) {
            return (LocalDateTime) dateTime;
        } else if (dateTime instanceof String) {
            return LocalDateTime.parse((String) dateTime, DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN));
        } else if (dateTime instanceof Date) {
            return Instant.ofEpochMilli(((Date) dateTime).getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else if (dateTime instanceof Long) {
            return Instant.ofEpochMilli((Long) dateTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

}
