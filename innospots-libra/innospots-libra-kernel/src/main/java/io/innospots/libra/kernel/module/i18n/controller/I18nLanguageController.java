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
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.model.system.OrganizationInfo;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.base.data.request.FormQuery;
import io.innospots.libra.kernel.module.i18n.model.I18nLanguage;
import io.innospots.libra.kernel.module.i18n.operator.I18nLanguageOperator;
import io.innospots.libra.kernel.service.SysConfigCommonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static io.innospots.base.enums.DataStatus.*;
import static io.innospots.base.model.response.InnospotResponse.fail;
import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "i18n/language")
@ModuleMenu(menuKey = "libra-language")
@Tag(name = "I18n Language")
public class I18nLanguageController extends BaseController {

    private final SysConfigCommonService sysConfigCommonService;

    private final I18nLanguageOperator i18nLanguageOperator;

    public I18nLanguageController(SysConfigCommonService sysConfigCommonService,
                                  I18nLanguageOperator i18nLanguageOperator) {
        this.i18nLanguageOperator = i18nLanguageOperator;
        this.sysConfigCommonService = sysConfigCommonService;
    }

    /**
     * create i18n language
     *
     * @param i18nLanguage
     * @return Boolean
     */
    @OperationLog(operateType = OperateType.CREATE, primaryField = "name", idParamPosition = 0)
    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(summary = "create i18n language")
    public InnospotResponse<Boolean> createLanguage(@Valid @RequestBody I18nLanguage i18nLanguage) {
        if (i18nLanguage.getStatus() == null) {
            i18nLanguage.setStatus(ONLINE);
        }
        return success(i18nLanguageOperator.create(i18nLanguage));
    }

    /**
     * i18n language exist by locale
     *
     * @param locale
     * @return Boolean
     */
    @GetMapping("exist-locale/{locale}")
    @Operation(summary = "has i18n language")
    public InnospotResponse<Boolean> hasLocaleLanguage(@PathVariable String locale) {
        return success(i18nLanguageOperator.hasExist(locale));
    }

    /**
     * get i18n language
     *
     * @param languageId
     * @return Boolean
     */
    @GetMapping("/{languageId}")
    @Operation(summary = "get i18n language")
    public InnospotResponse<I18nLanguage> getLocaleLanguage(@PathVariable Integer languageId) {
        return success(i18nLanguageOperator.getLanguage(languageId));
    }


    /**
     * update i18n language status online or offline
     *
     * @param languageId
     * @param status     true-online  false-offline
     * @return Boolean
     */
    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("{languageId}/status/{status}")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${common.button.status}")
    @Operation(summary = "update i18n language status online or offline")
    public InnospotResponse<Boolean> updateStatus(@PathVariable Integer languageId, @PathVariable Boolean status) {
        DataStatus dataStatus = status ? ONLINE : OFFLINE;
        if (dataStatus.equals(OFFLINE)) {
            //判断是否可以关闭
            OrganizationInfo organizationInfo = sysConfigCommonService.getOrganization();
            if (organizationInfo != null && languageId.toString().equals(organizationInfo.getDefaultLanguage())) {
                return fail(ResponseCode.RESOURCE_DELETE_FAILED, "organization default locale not offline");
            }
        }
        return success(i18nLanguageOperator.updateStatus(languageId, dataStatus));
    }

    /**
     * delete i18n language
     *
     * @param languageId
     * @return Boolean
     */
    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{languageId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(summary = "delete i18n language")
    public InnospotResponse<Boolean> deleteLanguage(@PathVariable Integer languageId) {
        //判断是否可以关闭
        OrganizationInfo organizationInfo = sysConfigCommonService.getOrganization();
        if (organizationInfo != null && languageId.toString().equals(organizationInfo.getDefaultLanguage())) {
            return fail(ResponseCode.RESOURCE_DELETE_FAILED, "organization default locale not delete");
        }
        return success(i18nLanguageOperator.updateStatus(languageId, REMOVED));
    }

    /**
     * update i18n language
     *
     * @param i18nLanguage
     * @return
     */
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "languageId", idParamPosition = 0)
    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(summary = "update i18n language")
    public InnospotResponse<Boolean> updateLanguage(@Valid @RequestBody I18nLanguage i18nLanguage) {
        if (i18nLanguage.getStatus().equals(OFFLINE)) {
            //判断是否可以关闭
            OrganizationInfo organizationInfo = sysConfigCommonService.getOrganization();
            if (organizationInfo != null && i18nLanguage.getLanguageId().toString().equals(organizationInfo.getDefaultLanguage())) {
                return fail(ResponseCode.RESOURCE_DELETE_FAILED, "organization default locale not delete");
            }
        }

        return success(i18nLanguageOperator.updateLanguage(i18nLanguage));
    }

    /**
     * page of I18nLanguage
     *
     * @param formRequest queryInput: 空，OFFLINE，ONLINE
     * @return
     */
    @GetMapping("page")
    @Operation(summary = "page i18n language")
    public InnospotResponse<PageBody<I18nLanguage>> pageLanguages(FormQuery formRequest) {
        return success(i18nLanguageOperator.pageLanguages(formRequest));
    }

    /**
     * list of I18nLanguage
     *
     * @param dataStatus 空，OFFLINE，ONLINE default:ONLINE
     * @return
     */
    @GetMapping("list")
    @Operation(summary = "list i18n language")
    public InnospotResponse<List<I18nLanguage>> listLanguages(@RequestParam(defaultValue = "ONLINE") DataStatus dataStatus) {
        return success(i18nLanguageOperator.list(dataStatus));
    }

}
