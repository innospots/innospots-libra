/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.libra.kernel.module.credential.controller;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.credential.model.CredentialType;
import io.innospots.base.connector.credential.operator.CredentialTypeOperator;
import io.innospots.base.connector.credential.reader.IConnectionCredentialReader;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.base.connector.credential.model.SimpleCredentialInfo;
import io.innospots.base.connector.credential.operator.CredentialInfoOperator;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.base.connector.credential.model.CredentialInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.exception.ValidatorException.buildInvalidException;
import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.log.OperateType.*;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2023/1/19
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "credential")
@ModuleMenu(menuKey = "libra-credential")
@Tag(name = "Credential")
public class CredentialInfoController extends BaseController {


    private final CredentialInfoOperator credentialInfoOperator;

    private final IConnectionCredentialReader connectionCredentialReader;

    private final CredentialTypeOperator credentialTypeOperator;

    public CredentialInfoController(
            CredentialInfoOperator credentialInfoOperator,
            IConnectionCredentialReader connectionCredentialReader,
            CredentialTypeOperator credentialTypeOperator) {
        this.credentialInfoOperator = credentialInfoOperator;
        this.connectionCredentialReader = connectionCredentialReader;
        this.credentialTypeOperator = credentialTypeOperator;

    }

    @PostMapping("connection/test")
    @Operation(summary = "test connection", description = "Connection test")
    public InnospotResponse<Object> testConnection(@Parameter(name = "credentialInfo") @Validated @RequestBody CredentialInfo credentialInfo) {
        if (StringUtils.isBlank(credentialInfo.getEncryptFormValues())) {
            throw buildInvalidException(this.getClass(), "schemaDatasource formValues can not be empty");
        }
        CredentialType credentialType = credentialTypeOperator.getCredentialType(credentialInfo.getCredentialTypeCode());
        credentialInfo.setCredentialType(credentialType);
        // fill credential
        ConnectionCredential connection = connectionCredentialReader.fillCredential(credentialInfo);
        Object connectionTest = DataConnectionMinderManager.testConnection(connection);
        return success(connectionTest);
    }


    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @OperationLog(operateType = CREATE, primaryField = "credentialId")
    @Operation(summary = "create credential")
    public InnospotResponse<CredentialInfo> createCredentialInfo(@Parameter(name = "credentialInfo") @Validated @RequestBody CredentialInfo credentialInfo) {
        CredentialInfo create = credentialInfoOperator.createCredential(credentialInfo);
        return success(create);
    }

    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @OperationLog(operateType = UPDATE, primaryField = "credentialId")
    @Operation(summary = "update credential")
    public InnospotResponse<CredentialInfo> updateCredentialInfo(@Parameter(name = "credentialInfo") @Validated @RequestBody CredentialInfo appCredentialInfo) {
        CredentialInfo update = credentialInfoOperator.updateCredential(appCredentialInfo);
        return success(update);
    }

    @DeleteMapping("{credentialKey}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(operateType = DELETE, idParamPosition = 0)
    @Operation(summary = "delete credential")
    public InnospotResponse<Boolean> deleteCredentialInfo(@Parameter(name = "credentialKey") @PathVariable String credentialKey) {
        Boolean delete = credentialInfoOperator.deleteCredential(credentialKey);
        return success(delete);
    }

    @GetMapping("{credentialKey}")
    @Operation(summary = "get credential")
    public InnospotResponse<CredentialInfo> getCredentialInfo(@Parameter(name = "credentialKey") @PathVariable String credentialKey) {
        CredentialInfo view = credentialInfoOperator.getCredential(credentialKey);
        return success(view);
    }


    @GetMapping("page")
    @Operation(summary = "page list")
    public InnospotResponse<PageBody<SimpleCredentialInfo>> CredentialInfoPages(FormQuery formQuery) {
        PageBody<SimpleCredentialInfo> pages = credentialInfoOperator.pageCredentials(formQuery);
        return success(pages);
    }


    @GetMapping("simple/list")
    @Operation(summary = "list credentials by credential type")
    public InnospotResponse<List<SimpleCredentialInfo>> listSimpleCredentials(@RequestParam(required = false) String credentialTypeCode) {
        return success(credentialInfoOperator.listSimpleCredentials(credentialTypeCode));
    }

}
