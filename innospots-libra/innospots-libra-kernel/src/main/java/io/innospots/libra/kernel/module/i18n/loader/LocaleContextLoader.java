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

package io.innospots.libra.kernel.module.i18n.loader;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.innospots.base.i18n.LocaleContext;
import io.innospots.libra.kernel.module.i18n.model.I18nCurrency;
import io.innospots.libra.kernel.module.i18n.model.I18nLanguage;
import io.innospots.libra.kernel.module.i18n.operator.I18nCurrencyOperator;
import io.innospots.libra.kernel.module.i18n.operator.I18nLanguageOperator;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/20
 */
@Component
public class LocaleContextLoader {

    private I18nLanguageOperator languageOperator;

    private I18nCurrencyOperator currencyOperator;

    public LocaleContextLoader(I18nLanguageOperator languageOperator, I18nCurrencyOperator currencyOperator) {
        this.languageOperator = languageOperator;
        this.currencyOperator = currencyOperator;
    }

    private LoadingCache<Locale, LocaleContext> loadingCache = Caffeine.newBuilder().
            expireAfterAccess(2, TimeUnit.DAYS).build(locale -> {
                LocaleContext localeContext = new LocaleContext();
                localeContext.setLocale(locale);
                I18nLanguage i18nLanguage = languageOperator.getLanguage(locale.getDisplayLanguage());
                if (i18nLanguage != null) {
                    I18nCurrency i18nCurrency = currencyOperator.getCurrency(i18nLanguage.getCurrencyId());
                    localeContext.setDecimalSeparator(i18nLanguage.getDecimalSeparator());
                    localeContext.setThousandSeparator(i18nLanguage.getThousandSeparator());
                    localeContext.setTimeFormat(i18nLanguage.getTimeFormat());
                    localeContext.setDateFormat(i18nLanguage.getDateFormat());
                    if (i18nCurrency != null) {
                        localeContext.setCurrencyCode(i18nCurrency.getCode());
                        localeContext.setCurrencyDecimalDigits(i18nCurrency.getDecimalDigits());
                        localeContext.setCurrencyLeftSign(i18nCurrency.getLeftSign());
                        localeContext.setCurrencyRightSign(i18nCurrency.getRightSign());
                    }
                }

                return localeContext;
            });


    public LocaleContext get(Locale locale) {
        return loadingCache.get(locale);
    }
}