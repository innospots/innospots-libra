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

package io.innospots.libra.kernel.module.schema.controller;

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaCatalog;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.connector.schema.model.SchemaRegistryType;
import io.innospots.base.connector.schema.operator.SchemaRegistryOperator;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2021-02-04
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "schema/registry")
@ModuleMenu(menuKey = "libra-dataset")
@Tag(name = "Schema Registry")
public class SchemaRegistryController extends BaseController {

    private final SchemaRegistryOperator schemaRegistryOperator;

    private final DataConnectionMinderManager dataConnectionMinderManager;


    public SchemaRegistryController(
            SchemaRegistryOperator schemaRegistryOperator,
            DataConnectionMinderManager dataConnectionMinderManager) {
        this.schemaRegistryOperator = schemaRegistryOperator;
        this.dataConnectionMinderManager = dataConnectionMinderManager;
    }

    @OperationLog(operateType = OperateType.CREATE, primaryField = "registryId")
    @PostMapping
    @ResourceItemOperation(key = "SchemaDatasource-updateSchemaDatasource")
    @Operation(summary = "create schema registry")
    public InnospotsResponse<SchemaRegistry> createSchemaRegistry(
            @Parameter(name = "schema registry") @Validated @RequestBody SchemaRegistry schemaRegistry, BindingResult bindingResult) {
        SchemaRegistry save = schemaRegistryOperator.createSchemaRegistry(schemaRegistry);
        return success(save);
    }

    @OperationLog(operateType = OperateType.UPDATE, primaryField = "registryId")
    @PutMapping
    @ResourceItemOperation
    @Operation(summary = "update schema registry", description = "")
    public InnospotsResponse<SchemaRegistry> updateSchemaRegistry(
            @Parameter(name = "schema registry") @Validated @RequestBody SchemaRegistry schemaRegistry, BindingResult bindingResult) {
        SchemaRegistry update = schemaRegistryOperator.updateSchemaRegistry(schemaRegistry);
        return success(update);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{registryId}")
    @ResourceItemOperation
    @Operation(summary = "delete schema registry", description = "")
    public InnospotsResponse<Boolean> deleteSchemaRegistry(
            @Parameter(name = "registryId") @PathVariable String registryId) {
        Boolean delete = schemaRegistryOperator.deleteSchemaRegistry(registryId);
        return success(delete);
    }

    @GetMapping("list/apps/{appKey}")
    @Operation(summary = "list app schema registries", description = "")
    public InnospotsResponse<List<SchemaRegistry>> listAppSchemaRegistries(
            @Parameter(name = "appKey") @PathVariable String appKey
    ) {
        return success(schemaRegistryOperator.listAppSchemaRegistries(appKey));
    }

    @GetMapping("list")
    @Operation(summary = "list schema registries", description = "")
    public InnospotsResponse<List<SchemaRegistry>> listSchemaRegistries(
            @Parameter(name = "credentialKey") @RequestParam(value = "credentialKey") String credentialKey,
            @Parameter(name = "includeField") @RequestParam(value = "includeField", required = false, defaultValue = "true") Boolean includeField
    ) {
        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialKey);
        List<SchemaRegistry> schemaRegistryList = minder.schemaRegistries(includeField);
        return success(schemaRegistryList);
    }

    @GetMapping("page/apps/{appKey}")
    @Operation(summary = "page schemaRegistry by appKey", description = "")
    public InnospotsResponse<PageBody<SchemaRegistry>> pageSchemaRegistryByType(
            @Parameter(name = "appKey") @PathVariable String appKey,
            @Parameter(name = "name") @RequestParam(value = "name") String name,
            @Parameter(name = "page") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(name = "size") @RequestParam(value = "size", defaultValue = "20") Integer size,
            @Parameter(name = "includeField") @RequestParam(value = "includeField", required = false, defaultValue = "true") Boolean includeField
    ) {
        PageBody<SchemaRegistry> pageBody = schemaRegistryOperator.pageSchemaRegistryByTypeOrAppKey(appKey,name,page,size,List.of(SchemaRegistryType.API,SchemaRegistryType.WORKFLOW,SchemaRegistryType.DATASET),includeField);
        return success(pageBody);
    }

    @GetMapping("page")
    @Operation(summary = "page schemaRegistry")
    public InnospotsResponse<PageBody<SchemaRegistry>> pageSchemaRegistries(
            @Parameter(name = "page") @RequestParam(value = "page") Integer page,
            @Parameter(name = "size") @RequestParam(value = "size") Integer size,
            @Parameter(name = "registryType",description = "VIEW,DATASET,ENTITY,API,TABLE,TABLE_VIEW,WORKFLOW,TOPIC") @RequestParam(value = "registryType", required = false) SchemaRegistryType registryType,
            @Parameter(name = "credentialKey") @RequestParam(value = "credentialKey", required = false) String credentialKey,
            @Parameter(name = "scope",description = "app,general") @RequestParam(value = "scope", required = false) String scope,
            @Parameter(name = "appKey") @RequestParam(value = "appKey", required = false) String appKey,
            @Parameter(name = "categoryId") @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @Parameter(name = "name") @RequestParam(value = "name", required = false) String name,
            @Parameter(name = "sort", description = "sort desc, default field createdTime") @RequestParam(value = "sort", required = false, defaultValue = "createdTime") String sort
    ) {
        PageBody<SchemaRegistry> pageBody = schemaRegistryOperator.pageSchemaRegistries(name,sort,
                 appKey, scope,
                 credentialKey,
                 categoryId,  registryType,
                 page, size);
        return success(pageBody);
    }

    @GetMapping("catalog/list")
    @Operation(summary = "list schema catalog from middleware", description = "")
    public InnospotsResponse<List<SchemaCatalog>> listSchemaRegistries(
            @Parameter(name = "credentialKey") @RequestParam(value = "credentialKey") String credentialKey) {
        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialKey);
        List<SchemaCatalog> schemaCatalogs = minder.schemaCatalogs();
        return success(schemaCatalogs);
    }

    @GetMapping
    @Operation(summary = "get schema registry", description = "support connectType: QUEUE,JDBC")
    public InnospotsResponse<SchemaRegistry> getSchemaRegistry(
            @Parameter(name = "credentialKey") @RequestParam(value = "credentialKey") String credentialKey,
            @Parameter(name = "tableName", description = "When the credential's connectType = JDBC, tableName is required")
            @RequestParam(value = "tableName", required = false) String tableName,
            @Parameter(name = "registryId", description = "When the credential's connectType = QUEUE, registryId is required")
            @RequestParam(value = "registryId", required = false) String registryId) {
        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialKey);
        SchemaRegistry schemaRegistry = null;
        if (registryId != null) {
            schemaRegistry = minder.schemaRegistryById(registryId);
        } else {
            schemaRegistry = minder.schemaRegistryByCode(tableName);
        }
        return success(schemaRegistry);
    }

    @GetMapping("fetch-sample")
    @Operation(summary = "schema registry fetch sample")
    public InnospotsResponse<Object> fetchSample(
            @Parameter(name = "credentialId") @RequestParam(value = "credentialKey") String credentialKey,
            @Parameter(name = "tableName") @RequestParam(value = "tableName") String tableName) {
        Object result = dataConnectionMinderManager.fetchSample(credentialKey, tableName);
        return success(result);
    }

    /*
    @GetMapping("fetch-samples")
    @Operation(summary = "schema registry fetch samples", description = "support SourceType: QUEUE,JDBC")
    public InnospotResponse<PageBody<Map<String, Object>>> fetchSamples(
            @Parameter(name = "credentialId") @RequestParam(value = "credentialKey") String credentialKey,
            @Parameter(name = "page") @RequestParam("page") int page,
            @Parameter(name = "size") @RequestParam("size") int size,
            @Parameter(name = "tableName", description = "When the credential's connectType = JDBC, tableName is required") @RequestParam(value = "tableName", required = false) String tableName,
            @Parameter(name = "registryId", description = "When the credential's connectType = QUEUE, registryId is required") @RequestParam(value = "registryId", required = false) String registryId) {
        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialKey);
        SchemaRegistry schemaRegistry = null;
        if (registryId != null) {
            schemaRegistry = minder.schemaRegistryById(registryId);
        } else {
            schemaRegistry = minder.schemaRegistryByCode(tableName);
        }
        return success(dataConnectionMinderManager.fetchSamples(credentialKey, schemaRegistry, page, size));
    }

     */

}
