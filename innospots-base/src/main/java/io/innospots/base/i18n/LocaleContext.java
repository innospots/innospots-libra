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

package io.innospots.base.i18n;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/20
 */
@Getter
@Setter
public class LocaleContext {

    private static final Logger logger = LoggerFactory.getLogger(LocaleContext.class);

    private Locale locale;

    private TimeZone timeZone;

    private String lanName;

    private String decimalSeparator = ".";

    private String thousandSeparator = ",";

    private String currencyCode;

    private String currencyLeftSign;

    private String currencyRightSign;

    private Integer currencyDecimalDigits = 2;

    private String dateFormat;

    private String timeFormat;

    private static LocaleContext defaultLocalContext;

    public LocaleContext() {
        // server default language is en_US
        this.locale = Locale.SIMPLIFIED_CHINESE;
        this.timeZone = TimeZone.getDefault();
        this.lanName = this.locale.getDisplayName();
        this.currencyCode = "USD";
        this.currencyLeftSign = "$";
        this.currencyRightSign = "";
        this.timeFormat = "HH:mm:ss";
        this.dateFormat = "yyyy-MM-dd";
    }

    public static LocaleContext getDefaultLocalContext() {
        if (defaultLocalContext == null) {
            defaultLocalContext = new LocaleContext();
        }

        return defaultLocalContext;
    }

}
