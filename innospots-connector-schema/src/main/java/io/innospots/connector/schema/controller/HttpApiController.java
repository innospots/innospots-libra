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

import io.innospots.base.data.http.HttpConnection;
import io.innospots.base.data.http.HttpDataExecutor;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.data.schema.ApiSchemaRegistry;
import io.innospots.base.data.credential.ConnectionCredential;
import io.innospots.base.data.credential.IConnectionCredentialReader;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.connector.schema.operator.HttpApiOperator;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.menu.ItemType.BUTTON;


/**
 * @author Alfred
 * @date 2021-08-21
 */
@Deprecated
//@RestController
//@RequestMapping(PATH_ROOT_ADMIN + "apps/schema/api")
//@ModuleMenu(menuKey = "libra-apps-api")
//@Tag(name = "Schema Http")
public class HttpApiController extends BaseController {

    private final HttpApiOperator httpApiOperator;

    private final IConnectionCredentialReader connectionCredentialReader;

    public HttpApiController(HttpApiOperator httpApiOperator,
                             IConnectionCredentialReader connectionCredentialReader) {
        this.httpApiOperator = httpApiOperator;
        this.connectionCredentialReader = connectionCredentialReader;
    }

    @OperationLog(operateType = OperateType.CREATE, primaryField = "registryId")
    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(summary = "create http api registry")
    public InnospotResponse<ApiSchemaRegistry> createHttpApi(@Validated @RequestBody ApiSchemaRegistry apiSchemaRegistry) {

        ApiSchemaRegistry save = httpApiOperator.createApiRegistry(apiSchemaRegistry);
        return success(save);
    }

    @OperationLog(operateType = OperateType.UPDATE, primaryField = "registryId")
    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(summary = "update http api")
    public InnospotResponse<ApiSchemaRegistry> updateHttpApi(@Validated @RequestBody ApiSchemaRegistry apiSchemaRegistry) {
        ApiSchemaRegistry update = httpApiOperator.updateApiRegistry(apiSchemaRegistry);
        return success(update);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{registryId}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(summary = "delete http api")
    public InnospotResponse<Boolean> deleteHttpApi(@Parameter(name = "registryId") @PathVariable Integer registryId) {
        Boolean delete = httpApiOperator.deleteApiRegistry(registryId);
        return success(delete);
    }

    @GetMapping("{registryId}")
    @Operation(summary = "get api registry by id")
    public InnospotResponse<ApiSchemaRegistry> getHttpApiById(@Parameter(name = "registryId") @PathVariable Integer registryId) {
        ApiSchemaRegistry apiSchemaRegistry = httpApiOperator.getApiRegistry(registryId);
        return success(apiSchemaRegistry);
    }

    @GetMapping
    @Operation(summary = "list api registry")
    public InnospotResponse<List<ApiSchemaRegistry>> listHttpApi(
            @Parameter(name = "queryCode") @RequestParam(value = "queryCode", required = false) String queryCode,
            @Parameter(name = "sort", description = "sort field") @RequestParam(value = "sort", required = false, defaultValue = "createdTime") String sort) {
        List<ApiSchemaRegistry> list = httpApiOperator.listApiRegistries(queryCode, sort);
        return success(list);
    }


    @OperationLog(operateType = OperateType.FETCH, primaryField = "registryId", idParamPosition = 0)
    @PostMapping("fetch-sample")
    @Operation(summary = "http api fetch-sample")
    public InnospotResponse<Object> fetchSample(@Validated @RequestBody ApiSchemaRegistry apiSchemaRegistry) {
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(apiSchemaRegistry.getCredentialId());
        HttpConnection httpConnection = new HttpConnection(connectionCredential);
        HttpDataExecutor httpDataExecutor = new HttpDataExecutor(httpConnection);
//        HttpDataExecutor httpDataExecutor = new HttpDataExecutor(httpConnection, apiSchemaRegistry);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setBody(apiSchemaRegistry.getParamValue());
        return success(httpDataExecutor.execute(itemRequest).getBody());
    }
}
