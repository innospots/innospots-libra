/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional Typermation regarding copyright ownership.
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

import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.connector.credential.model.CredentialType;
import io.innospots.base.connector.credential.operator.CredentialTypeOperator;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.log.OperateType.*;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/10/30
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "credential-type")
@ModuleMenu(menuKey = "libra-credential-type")
@Tag(name = "CredentialType")
public class CredentialTypeController {

    private final CredentialTypeOperator credentialTypeOperator;

    public CredentialTypeController(CredentialTypeOperator credentialTypeOperator) {
        this.credentialTypeOperator = credentialTypeOperator;
    }

    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @OperationLog(operateType = CREATE, primaryField = "typeCode")
    @Operation(summary = "create credential type")
    public InnospotsResponse<CredentialType> createCredentialType(@Parameter(name = "credentialType") @RequestBody CredentialType credentialType) {
        CredentialType create = credentialTypeOperator.createCredentialType(credentialType);
        return success(create);
    }

    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @OperationLog(operateType = UPDATE, primaryField = "typeCode")
    @Operation(summary = "update credential type")
    public InnospotsResponse<CredentialType> updateCredentialType(@Parameter(name = "credentialType") @Validated @RequestBody CredentialType credentialType) {
        CredentialType update = credentialTypeOperator.updateCredentialType(credentialType);
        return success(update);
    }

    @DeleteMapping("{typeCode}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(operateType = DELETE, idParamPosition = 0)
    @Operation(summary = "delete credential type")
    public InnospotsResponse<Boolean> deleteCredentialType(@Parameter(name = "typeCode") @PathVariable String typeCode) {
        Boolean delete = credentialTypeOperator.deleteCredentialType(typeCode);
        return success(delete);
    }

    @GetMapping("{typeCode}")
    @Operation(summary = "get credential type")
    public InnospotsResponse<CredentialType> getCredentialType(@Parameter(name = "typeCode") @PathVariable String typeCode) {
        CredentialType view = credentialTypeOperator.getCredentialType(typeCode);
        return success(view);
    }


    @GetMapping("page")
    @Operation(summary = "page list")
    public InnospotsResponse<PageBody<CredentialType>> pageCredentialTypes(FormQuery formQuery) {
        PageBody<CredentialType> pages = credentialTypeOperator.pageCredentialTypes(formQuery);
        return success(pages);
    }

    @GetMapping("list")
    @Operation(summary = "list")
    public InnospotsResponse<List<CredentialType>> listCredentialTypes(@Parameter(name = "connectorName") @RequestParam(required = false) String connectorName) {
        List<CredentialType> list = credentialTypeOperator.listCredentialTypes(connectorName);
        return success(list);
    }
}
