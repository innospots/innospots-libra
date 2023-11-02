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

import io.innospots.base.enums.DataStatus;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.base.data.request.FormQuery;
import io.innospots.libra.kernel.module.i18n.model.I18nCurrency;
import io.innospots.libra.kernel.module.i18n.operator.I18nCurrencyOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static io.innospots.base.enums.DataStatus.*;
import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "i18n/currency")
@ModuleMenu(menuKey = "libra-currency")
@Tag(name = "I18n Currency")
public class I18nCurrencyController extends BaseController {

    private final I18nCurrencyOperator i18nCurrencyOperator;

    public I18nCurrencyController(I18nCurrencyOperator i18nCurrencyOperator) {
        this.i18nCurrencyOperator = i18nCurrencyOperator;
    }

    @PostMapping
    @OperationLog(operateType = OperateType.CREATE, primaryField = "code")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(summary = "create i18n currency")
    public InnospotResponse<Boolean> createCurrency(@Valid @RequestBody I18nCurrency i18nCurrency) {
        if (i18nCurrency.getStatus() == null) {
            i18nCurrency.setStatus(ONLINE);
        }
        return success(i18nCurrencyOperator.createCurrency(i18nCurrency));
    }


    @OperationLog(operateType = OperateType.UPDATE_STATUS, idParamPosition = 0)
    @PutMapping("{currencyId}/status/{status}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${common.button.status}")
    @Operation(summary = "update i18n currency status")
    public InnospotResponse<Boolean> updateStatus(@PathVariable Integer currencyId, @PathVariable Boolean status) {
        DataStatus dataStatus = status ? ONLINE : OFFLINE;
        return success(i18nCurrencyOperator.updateStatus(currencyId, dataStatus));
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{currencyId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(summary = "delete i18n currency")
    public InnospotResponse<Boolean> deleteCurrency(@PathVariable Integer currencyId) {

        return success(i18nCurrencyOperator.updateStatus(currencyId, REMOVED));
    }

    @OperationLog(operateType = OperateType.UPDATE, primaryField = "currencyId", idParamPosition = 0)
    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(summary = "update i18n currency")
    public InnospotResponse<Boolean> updateCurrency(@Valid @RequestBody I18nCurrency i18nCurrency) {

        return success(i18nCurrencyOperator.updateCurrency(i18nCurrency));
    }

    @GetMapping("page")
    @Operation(summary = "page of i18n currency")
    public InnospotResponse<PageBody<I18nCurrency>> pageCurrencies(FormQuery formRequest) {
        return success(i18nCurrencyOperator.pageCurrencies(formRequest));
    }

    /**
     * @param dataStatus 空，OFFLINE，ONLINE default:ONLINE
     * @return
     */
    @GetMapping("list")
    @Operation(summary = "list of i18n currency")
    public InnospotResponse<List<I18nCurrency>> listCurrencies(@RequestParam(defaultValue = "ONLINE") DataStatus dataStatus) {
        return success(i18nCurrencyOperator.listCurrencies(dataStatus));
    }
}
