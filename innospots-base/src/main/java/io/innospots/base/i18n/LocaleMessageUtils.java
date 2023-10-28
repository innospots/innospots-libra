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

package io.innospots.base.i18n;


import cn.hutool.core.thread.threadlocal.NamedThreadLocal;
import io.innospots.base.utils.BeanContextAwareUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Raydian
 * @date 2021/1/26
 */
public class LocaleMessageUtils {

    private static final ThreadLocal<LocaleContext> localeContextHolder = new NamedThreadLocal<>("LocaleContext");

    private static Locale defaultLocale;

    private static TimeZone defaultTimeZone;

    public static MessageSource messageSource() {
        if(BeanContextAwareUtils.isLoaded()){
            return BeanContextAwareUtils.getBean(MessageSource.class);
        }
        return null;
    }

    public static String message(String code) {
        return message(code, new Object[]{});
    }

    public static String message(String code, String defaultMessage) {
        return message(code, null, defaultMessage);
    }

    public static String message(String code, String defaultMessage, Locale locale) {
        return message(code, null, defaultMessage, locale);
    }

    public static String message(String code, Locale locale) {
        return message(code, null, "", locale);
    }

    public static String message(String code, Object[] args) {
        return message(code, args, "");
    }

    public static String message(String code, Object[] args, Locale locale) {
        return message(code, args, "", locale);
    }

    public static String message(String code, Object[] args, String defaultMessage) {
        Locale locale = getLocale();
        return message(code, args, defaultMessage, locale);
    }

    public static String message(String code, Object[] args, String defaultMessage, Locale locale) {
        MessageSource messageSource = messageSource();
        if(messageSource == null){
            return code + ": " + Arrays.toString(args);
        }
        String msg = messageSource.getMessage(code, args, defaultMessage, locale);
        if (msg == null) {
            return code;
        }
        return msg;
    }


    public static void resetLocaleContext() {
        localeContextHolder.remove();
    }

    public static void setLocaleContext(LocaleContext localeContext) {
        if (localeContext == null) {
            resetLocaleContext();
        } else {
            localeContextHolder.set(localeContext);
        }
    }

    public static LocaleContext getLocaleContext() {
        LocaleContext localeContext = localeContextHolder.get();
        if (localeContext == null) {
            localeContext = LocaleContext.getDefaultLocalContext();
        }
        return localeContext;
    }


    public static void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
    }

    public static Locale getLocale() {
        return getLocale(getLocaleContext());
    }

    public static Locale getLocale(LocaleContext localeContext) {
        if (localeContext != null) {
            Locale locale = localeContext.getLocale();
            if (locale != null) {
                return locale;
            }
        }
        return defaultLocale != null ? defaultLocale : Locale.getDefault();
    }


    public static void setDefaultTimeZone(TimeZone timeZone) {
        defaultTimeZone = timeZone;
    }

    public static TimeZone getTimeZone() {
        return getTimeZone(getLocaleContext());
    }

    public static TimeZone getTimeZone(LocaleContext localeContext) {
        if (localeContext != null) {
            TimeZone timeZone = localeContext.getTimeZone();
            if (timeZone != null) {
                return timeZone;
            }
        }
        return defaultTimeZone != null ? defaultTimeZone : TimeZone.getDefault();
    }


}
