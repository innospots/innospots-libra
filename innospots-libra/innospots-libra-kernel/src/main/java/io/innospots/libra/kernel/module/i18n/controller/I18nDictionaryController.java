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

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.kernel.module.i18n.operator.I18nDictionaryOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "i18n/dictionary")
@ModuleMenu(menuKey = "libra-translation")
//@ModuleMenu(menuKey = "libra-dictionary",parent = "libra-translation")
@Tag(name = "I18n Dictionary")
public class I18nDictionaryController extends BaseController {

    private I18nDictionaryOperator i18nDictionaryOperator;

    public I18nDictionaryController(I18nDictionaryOperator i18nDictionaryOperator) {
        this.i18nDictionaryOperator = i18nDictionaryOperator;
    }


    @GetMapping("list-app")
    @Operation(summary = "list app of i18n dictionary")
    public InnospotsResponse<List<String>> listApp() {
        return success(i18nDictionaryOperator.listApps());
    }

    @GetMapping("list-module")
    @Operation(summary = "list module of i18n dictionary")
    public InnospotsResponse<List<String>> listModule() {
        return success(i18nDictionaryOperator.listModules());
    }

    @GetMapping("list-module/app/{app}")
    @Operation(summary = "list module of i18n dictionary by app")
    public InnospotsResponse<List<String>> listModule(@PathVariable String app) {
        return success(i18nDictionaryOperator.listModulesByAppName(app));
    }

}
