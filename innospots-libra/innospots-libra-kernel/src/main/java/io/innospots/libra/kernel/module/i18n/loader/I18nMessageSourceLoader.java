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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.libra.kernel.module.i18n.model.LocaleMessage;
import io.innospots.libra.kernel.module.i18n.operator.I18nMessageOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.StaticMessageSource;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @date 2021/12/20
 */
@Slf4j
public class I18nMessageSourceLoader extends StaticMessageSource {

    private I18nMessageOperator i18nMessageOperator;

    private Cache<String, LocaleMessage> localCache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    public I18nMessageSourceLoader(I18nMessageOperator i18nMessageOperator) {
        this.i18nMessageOperator = i18nMessageOperator;
    }

    @Override
    protected String getMessageInternal(String code, Object[] args, Locale locale) {
        MessageFormat messageFormat = resolveCode(code, locale);
        if (messageFormat == null && !StringUtils.contains(code, " ")
                && (StringUtils.contains(code, ".") || StringUtils.contains(code, "-"))) {
            LocaleMessage localeMessage = localCache.getIfPresent(code + "_" + locale);
            if (localeMessage == null) {
                localeMessage = i18nMessageOperator.getLocaleMessage(code, locale);
                log.debug("i18n, code:{}, locale:{}, args:{}", code, locale, Arrays.toString(args));
                // cache locale message
                synchronized (this){
                    if (localeMessage != null) {
                        this.addMessage(localeMessage.getCode(), localeMessage.locale(), localeMessage.getMessage());
                    } else {
                        localeMessage = new LocaleMessage();
                    }
                    localCache.put(code + "_" + locale, localeMessage);
                }
            }

        }
        return super.getMessageInternal(code, args, locale);
    }
}
