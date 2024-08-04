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

package io.innospots.libra.kernel.module.i18n.configuration;

import io.innospots.base.i18n.LocaleContext;
import io.innospots.base.i18n.LocaleMessageUtils;
import io.innospots.libra.kernel.module.i18n.loader.LocaleContextLoader;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author Raydian
 * @date 2021/1/26
 */
public class ParamLocaleResolver extends CookieLocaleResolver {

    private LocaleContextLoader localeContextLoader;

    public ParamLocaleResolver(LocaleContextLoader localeContextLoader) {
        this.localeContextLoader = localeContextLoader;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return super.resolveLocale(request);
    }

    @Override
    public org.springframework.context.i18n.LocaleContext resolveLocaleContext(HttpServletRequest request) {
        org.springframework.context.i18n.LocaleContext localeContext = super.resolveLocaleContext(request);
        fillLocale(localeContext.getLocale());
        return localeContext;
    }

    private void fillLocale(Locale locale) {
        LocaleMessageUtils.setDefaultLocale(locale);
        LocaleContext localeContext = localeContextLoader.get(locale);
        if (localeContext != null) {
            LocaleMessageUtils.setLocaleContext(localeContext);
        }
    }
}
