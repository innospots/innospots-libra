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

import io.innospots.base.i18n.I18nMessageSource;
import io.innospots.libra.kernel.module.i18n.loader.I18nMessageSourceLoader;
import io.innospots.libra.kernel.module.i18n.loader.LocaleContextLoader;
import io.innospots.libra.kernel.module.i18n.operator.I18nMessageOperator;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * @author Raydian
 * @date 2021/1/26
 */
@Configuration
public class I18nConfiguration implements WebMvcConfigurer {


    @Bean
    public ParamLocaleResolver localeResolver(LocaleContextLoader localeContextLoader) {
        ParamLocaleResolver paramLocaleResolver = new ParamLocaleResolver(localeContextLoader);
        paramLocaleResolver.setCookieName("lang");
        paramLocaleResolver.setDefaultLocale(Locale.US);
        return paramLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName(LocaleChangeInterceptor.DEFAULT_PARAM_NAME);
        return localeChangeInterceptor;
    }

    @Bean
    public I18nMessageSource i18nMessageSource(MessageSource messageSource) {
        return new I18nMessageSource(){
            @Override
            public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
                return messageSource.getMessage(code,args,defaultMessage,locale);
            }

            @Override
            public String getMessage(String code, Object[] args, Locale locale) {
                return messageSource.getMessage(code, args, locale);
            }
        };
    }

    @Bean
    public MessageSource messageSource(I18nMessageOperator i18nMessageOperator) {
        I18nMessageSourceLoader messageSource = new I18nMessageSourceLoader(i18nMessageOperator);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
