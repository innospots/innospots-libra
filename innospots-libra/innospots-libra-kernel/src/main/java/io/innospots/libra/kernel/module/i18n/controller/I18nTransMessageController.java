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

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.i18n.model.I18nTransMessageGroup;
import io.innospots.libra.kernel.module.i18n.model.TransHeaderColumn;
import io.innospots.libra.kernel.module.i18n.model.TransMessageForm;
import io.innospots.libra.kernel.module.i18n.operator.I18nTransMessageOperator;
import io.innospots.libra.kernel.module.i18n.service.I18nTransMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "i18n/translation")
@ModuleMenu(menuKey = "libra-translation")
@Tag(name = "I18n Translate Message")
public class I18nTransMessageController extends BaseController {

    private I18nTransMessageOperator i18nTransMessageOperator;

    private I18nTransMessageService i18nTransMessageService;

    public I18nTransMessageController(I18nTransMessageOperator i18nTransMessageOperator, I18nTransMessageService i18nTransMessageService) {
        this.i18nTransMessageOperator = i18nTransMessageOperator;
        this.i18nTransMessageService = i18nTransMessageService;
    }

    @GetMapping("header-column")
    @Operation(description = "list translate header column")
    public R<List<TransHeaderColumn>> transHeaderColumns() {
        return success(i18nTransMessageOperator.transHeaderColumns());
    }
    
    @OperationLog(operateType = OperateType.CREATE)
    @PostMapping("message")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(description = "create translate message")
    public R<I18nTransMessageGroup> createTransMessage(@RequestBody TransMessageForm messageForm) {
        return success(i18nTransMessageOperator.createTransMessage(messageForm));
    }

    @OperationLog(operateType = OperateType.UPDATE)
    @PutMapping("message")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(description = "update translate message")
    public R<Boolean> updateTransMessage(@RequestBody I18nTransMessageGroup messageGroup) {
        return success(i18nTransMessageOperator.updateTransMessageGroup(messageGroup));
    }

    @GetMapping("page")
    @Operation(description = "page translate message")
    public R<PageBody<I18nTransMessageGroup>> pageTranslations(@RequestParam(defaultValue = "") String app, @RequestParam(defaultValue = "") String module,
                                                               @RequestParam(defaultValue = "") String code, @RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return success(i18nTransMessageOperator.pageTranslations(app, module, code, page, size));
    }

    @OperationLog(operateType = OperateType.IMPORT)
    @PostMapping("import-path")
    @Operation(description = "import translate message of path")
    public R<Map<String, String>> importTransMessageOfPath(String filePath) {
        return success(i18nTransMessageService.importCsv(filePath));
    }

    @OperationLog(operateType = OperateType.IMPORT)
    @PostMapping("import-csv")
    @Operation(description = "import translate message of csv file")
    public R<Map<String, String>> importTransMessageOfCsv(@RequestParam("file") MultipartFile file) {

        return success(i18nTransMessageService.importCsv(file));
    }

    @OperationLog(operateType = OperateType.IMPORT)
    @PostMapping("import-excel")
    @Operation(description = "import translate message of excel file")
    public R<Map<String, String>> importTransMessageOfExcel(@RequestParam("file") MultipartFile file) {

        return success(i18nTransMessageService.importExcel(file));
    }

}
