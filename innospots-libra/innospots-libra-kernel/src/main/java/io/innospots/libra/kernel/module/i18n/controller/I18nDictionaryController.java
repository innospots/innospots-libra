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
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.i18n.operator.I18nDictionaryOperator;
import io.innospots.libra.kernel.module.i18n.operator.I18nTransMessageOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

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

    private final I18nDictionaryOperator i18nDictionaryOperator;
    private final I18nTransMessageOperator i18nTransMessageOperator;

    public I18nDictionaryController(I18nDictionaryOperator i18nDictionaryOperator, I18nTransMessageOperator i18nTransMessageOperator) {
        this.i18nDictionaryOperator = i18nDictionaryOperator;
        this.i18nTransMessageOperator = i18nTransMessageOperator;
    }


    @GetMapping("list-app")
    @Operation(description = "list app of i18n dictionary")
    public R<List<String>> listApp() {
        return success(i18nDictionaryOperator.listApps());
    }

    @GetMapping("list-module")
    @Operation(description = "list module of i18n dictionary")
    public R<List<String>> listModule() {
        return success(i18nDictionaryOperator.listModules());
    }

    @GetMapping("list-module/app/{app}")
    @Operation(description = "list module of i18n dictionary by app")
    public R<List<String>> listModule(@PathVariable String app) {
        return success(i18nDictionaryOperator.listModulesByAppName(app));
    }

    @OperationLog(operateType = OperateType.DELETE)
    @DeleteMapping("{dictionaryId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(description = "delete dictionary and translate message")
    public R<Boolean> deleteDictionary(@PathVariable Integer dictionaryId){
        return success(i18nTransMessageOperator.deleteTransMessage(dictionaryId));
    }

}
