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

package io.innospots.connector.schema.controller;

import io.innospots.base.data.minder.DataConnectionMinderManager;
import io.innospots.base.data.schema.AppCredentialInfo;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.data.schema.SimpleAppCredential;
import io.innospots.base.data.schema.config.ConnectionMinderSchema;
import io.innospots.base.data.schema.config.ConnectionMinderSchemaLoader;
import io.innospots.base.data.schema.reader.IConnectionCredentialReader;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.connector.schema.mapper.CredentialConvertMapper;
import io.innospots.connector.schema.operator.AppCredentialOperator;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
@RequestMapping(PATH_ROOT_ADMIN + "apps/credential")
@ModuleMenu(menuKey = "libra-apps")
@Tag(name = "App Credential")
public class CredentialController extends BaseController {


    private final AppCredentialOperator appCredentialOperator;

    private final IConnectionCredentialReader connectionCredentialReader;

    public CredentialController(
            AppCredentialOperator appCredentialOperator,
            IConnectionCredentialReader connectionCredentialReader) {
        this.appCredentialOperator = appCredentialOperator;
        this.connectionCredentialReader = connectionCredentialReader;

    }

    @PostMapping("connection/test")
    @Operation(summary = "test connection", description = "Connection test")
    public InnospotResponse<Object> testConnection(
            @Parameter(name = "credentialInfo") @Validated @RequestBody AppCredentialInfo appCredentialInfo,
            BindingResult bindingResult) {
        if (StringUtils.isBlank(appCredentialInfo.getEncryptFormValues())) {
            throw buildInvalidException(this.getClass(), "schemaDatasource formValues can not be empty");
        }

        // fill credential
        ConnectionCredential connection = connectionCredentialReader.fillCredential(appCredentialInfo);
        Object connectionTest = DataConnectionMinderManager.testConnection(connection);
        return success(connectionTest);
    }


    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @OperationLog(operateType = CREATE, primaryField = "credentialId")
    @Operation(summary = "create credential")
    public InnospotResponse<AppCredentialInfo> createCredentialInfo(
            @Parameter(name = "credentialInfo") @Validated @RequestBody AppCredentialInfo appCredentialInfo,
            BindingResult bindingResult) {
        AppCredentialInfo create = appCredentialOperator.createCredential(appCredentialInfo);
        return success(create);
    }

    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @OperationLog(operateType = UPDATE, primaryField = "credentialId")
    @Operation(summary = "update credential")
    public InnospotResponse<AppCredentialInfo> updateCredentialInfo(
            @Parameter(name = "credentialInfo") @Validated @RequestBody AppCredentialInfo appCredentialInfo,
            BindingResult bindingResult) {
        AppCredentialInfo update = appCredentialOperator.updateCredential(appCredentialInfo);
        return success(update);
    }

    @DeleteMapping("{credentialId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(operateType = DELETE, idParamPosition = 0)
    @Operation(summary = "delete credential")
    public InnospotResponse<Boolean> deleteCredentialInfo(
            @Parameter(name = "credentialId") @PathVariable Integer credentialId) {
        Boolean delete = appCredentialOperator.deleteCredential(credentialId);
        return success(delete);
    }

    @GetMapping("{credentialId}")
    @Operation(summary = "get credential")
    public InnospotResponse<AppCredentialInfo> getCredentialInfo(
            @Parameter(name = "credentialId") @PathVariable Integer credentialId) {
        AppCredentialInfo view = appCredentialOperator.getCredential(credentialId);
        return success(view);
    }

    @GetMapping("list")
    @Operation(summary = "list CredentialInfos")
    public InnospotResponse<List<AppCredentialInfo>> listCredentialInfos(
            @Parameter(name = "queryInput") @RequestParam(value = "queryInput", required = false) String queryInput,
            @Parameter(name = "configCode") @RequestParam(value = "configCode", required = false) String configCode,
            @Parameter(name = "connector") @RequestParam(value = "connector", required = false) String connector,
            @Parameter(name = "sort", description = "sort field") @RequestParam(value = "sort", required = false, defaultValue = "createdTime") String sort
    ) {
        List<AppCredentialInfo> list = appCredentialOperator.listCredentials(queryInput, configCode, connector, sort);
        return success(list);
    }

    @GetMapping("page")
    @Operation(summary = "page list")
    public InnospotResponse<PageBody<AppCredentialInfo>> CredentialInfoPages(
            @Parameter(name = "queryInput") @RequestParam(value = "queryInput", required = false) String queryInput,
            @Parameter(name = "configCode") @RequestParam(value = "configCode", required = false) String configCode,
            @Parameter(name = "connector") @RequestParam(value = "connector", required = false) String connector,
            @Parameter(name = "sort", description = "sort field") @RequestParam(value = "sort", required = false, defaultValue = "createdTime") String sort,
            @Parameter(name = "page") @RequestParam("page") int page,
            @Parameter(name = "size") @RequestParam("size") int size
    ) {
        PageBody<AppCredentialInfo> pages = appCredentialOperator.pageCredentials(queryInput, configCode, connector, sort, page, size);
        return success(pages);
    }

    @GetMapping("simple/list")
    @Operation(summary = "list simple credentials")
    public InnospotResponse<List<SimpleAppCredential>> listSimpleCredentials() {
        List<SimpleAppCredential> simpleList = new ArrayList<>();
        List<AppCredentialInfo> list = appCredentialOperator.listCredentials(null, null, null, null);
        for (AppCredentialInfo info : list) {
            ConnectionMinderSchema minderSchema = ConnectionMinderSchemaLoader.getConnectionMinderSchema(info.getConnectorName());
            SimpleAppCredential simple = CredentialConvertMapper.INSTANCE.credentialToSimple(info);
            simple.setConnectType(minderSchema.getConnectType());
            simple.setIcon(minderSchema.getIcon());
            simpleList.add(simple);
        }
        return success(simpleList);
    }

    @GetMapping("simple/list/{appNodeCode}")
    @Operation(summary = "list app node credentials by code")
    public InnospotResponse<List<SimpleAppCredential>> listSimpleCredentials(
            @PathVariable String appNodeCode,@RequestParam(required = false) String configCode) {
        return success(appCredentialOperator.listSimpleAppCredentials(appNodeCode,configCode));
    }

}
