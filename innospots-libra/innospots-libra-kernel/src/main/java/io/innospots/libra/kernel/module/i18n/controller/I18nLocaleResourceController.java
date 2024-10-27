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

package io.innospots.libra.kernel.module.i18n.controller;

import io.innospots.base.model.response.R;
import io.innospots.libra.kernel.module.i18n.operator.I18nDictionaryOperator;
import io.innospots.libra.kernel.module.i18n.service.I18nLocaleResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.TreeMap;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.kernel.module.i18n.loader.LocaleCountryLoader.localeList;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/23
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "i18n/locale")
@Tag(name = "I18n Locale Resource")
public class I18nLocaleResourceController {

    private I18nLocaleResourceService localeResourceService;

    private I18nDictionaryOperator i18nDictionaryOperator;

    public I18nLocaleResourceController(I18nLocaleResourceService localeResourceService,
                                        I18nDictionaryOperator i18nDictionaryOperator) {
        this.localeResourceService = localeResourceService;
        this.i18nDictionaryOperator = i18nDictionaryOperator;
    }

    @GetMapping("list")
    @Operation(summary = "list available locale code,when create new language and select the language code")
    public R<TreeMap<String, String>> listAvailableLocale() {
        return success(localeList());
    }

    /**
     * get the national resources according module and locale
     *
     * @param localeCode ex. zh_CN, en_US
     * @param module
     * @return
     */
    @GetMapping("{localeCode}/{module}.json")
    @Operation(summary = "")
    public R<TreeMap<String, String>> getLocaleResource(HttpServletRequest request,
                                                        @Parameter @PathVariable String localeCode,
                                                        @Parameter @PathVariable String module) {
        /*
        LocaleContext localeContext = getLocaleContext();
        if (!localeCode.equalsIgnoreCase(localeContext.getLocale().toString())) {
            paramLocaleResolver.resolveLocale(request);
        }
         */

        return success(localeResourceService.getLocaleResource(module, localeCode));
    }

    @GetMapping("switch")
    public R<List<String>> switchLocale(@Parameter @RequestParam String locale) {
        return success(i18nDictionaryOperator.listModules());
    }
}
