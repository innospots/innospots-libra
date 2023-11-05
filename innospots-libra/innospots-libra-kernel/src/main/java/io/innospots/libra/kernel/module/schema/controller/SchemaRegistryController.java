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
import io.innospots.base.connector.schema.SchemaCatalog;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.kernel.module.schema.operator.SchemaRegistryOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2021-02-04
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "apps/schema/registry")
@ModuleMenu(menuKey = "libra-apps-schema")
@Tag(name = "Schema Registry")
public class SchemaRegistryController extends BaseController {

//    private final SchemaRegistryOperator schemaRegistryOperator;

    private final DataConnectionMinderManager dataConnectionMinderManager;


    public SchemaRegistryController(
            SchemaRegistryOperator schemaRegistryOperator,
            DataConnectionMinderManager dataConnectionMinderManager) {
//        this.schemaRegistryOperator = schemaRegistryOperator;
        this.dataConnectionMinderManager = dataConnectionMinderManager;
    }

//    @OperationLog(operateType = OperateType.CREATE, primaryField = "registryId")
//    @PostMapping
//    @ResourceItemOperation(key = "SchemaDatasource-updateSchemaDatasource")
//    @Operation(summary = "create schema registry")
//    public InnospotResponse<SchemaRegistry> createSchemaRegistry(
//            @Parameter(name = "schema registry") @Validated @RequestBody SchemaRegistry schemaRegistry, BindingResult bindingResult) {
//        SchemaRegistry save = schemaRegistryOperator.createSchemaRegistry(schemaRegistry);
//        return success(save);
//    }
//
//    @OperationLog(operateType = OperateType.UPDATE, primaryField = "registryId")
//    @PutMapping
//    @ResourceItemOperation
//    @Operation(summary = "update schema registry", description = "")
//    public InnospotResponse<SchemaRegistry> updateSchemaRegistry(
//            @Parameter(name = "schema registry") @Validated @RequestBody SchemaRegistry schemaRegistry, BindingResult bindingResult) {
//        SchemaRegistry update = schemaRegistryOperator.updateSchemaRegistry(schemaRegistry);
//        return success(update);
//    }
//
//    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
//    @DeleteMapping("{registryId}")
//    @ResourceItemOperation
//    @Operation(summary = "delete schema registry", description = "")
//    public InnospotResponse<Boolean> deleteSchemaRegistry(
//            @Parameter(name = "registryId") @PathVariable Integer registryId) {
//        Boolean delete = schemaRegistryOperator.deleteSchemaRegistry(registryId);
//        return success(delete);
//    }
//
//    @GetMapping("list")
//    @Operation(summary = "list schema registries", description = "")
//    public InnospotResponse<List<SchemaRegistry>> listSchemaRegistries(
//            @Parameter(name = "credentialId") @RequestParam(value = "credentialId") Integer credentialId,
//            @Parameter(name = "includeField") @RequestParam(value = "includeField", required = false, defaultValue = "true") Boolean includeField
//    ) {
//        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialId);
//        List<SchemaRegistry> schemaRegistryList = minder.schemaRegistries(includeField);
//        return success(schemaRegistryList);
//    }

    @GetMapping("catalog/list")
    @Operation(summary = "list schema catalog from middleware", description = "")
    public InnospotResponse<List<SchemaCatalog>> listSchemaRegistries(
            @Parameter(name = "credentialId") @RequestParam(value = "credentialKey") String credentialKey) {
        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialKey);
        List<SchemaCatalog> schemaCatalogs = minder.schemaCatalogs();
        return success(schemaCatalogs);
    }

//    @GetMapping
//    @Operation(summary = "get schema registry", description = "support connectType: QUEUE,JDBC")
//    public InnospotResponse<SchemaRegistry> getSchemaRegistry(
//            @Parameter(name = "credentialId") @RequestParam(value = "credentialId") Integer credentialId,
//            @Parameter(name = "tableName", description = "When the credential's connectType = JDBC, tableName is required")
//            @RequestParam(value = "tableName", required = false) String tableName,
//            @Parameter(name = "registryId", description = "When the credential's connectType = QUEUE, registryId is required")
//            @RequestParam(value = "registryId", required = false) Integer registryId) {
//        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialId);
//        SchemaRegistry schemaRegistry = null;
//        if (registryId != null) {
//            schemaRegistry = minder.schemaRegistry(registryId);
//        } else {
//            schemaRegistry = minder.schemaRegistry(tableName);
//        }
//        return success(schemaRegistry);
//    }

//    @GetMapping("fetch-sample")
//    @Operation(summary = "schema registry fetch sample")
//    public InnospotResponse<Object> fetchSample(
//            @Parameter(name = "credentialId") @RequestParam(value = "credentialId") Integer credentialId,
//            @Parameter(name = "tableName") @RequestParam(value = "tableName") String tableName) {
//        Object result = dataConnectionMinderManager.fetchSample(credentialId, tableName);
//        return success(result);
//    }
//
//    @GetMapping("fetch-samples")
//    @Operation(summary = "schema registry fetch samples", description = "support SourceType: QUEUE,JDBC")
//    public InnospotResponse<PageBody<Map<String, Object>>> fetchSamples(
//            @Parameter(name = "credentialId") @RequestParam(value = "credentialId") Integer credentialId,
//            @Parameter(name = "page") @RequestParam("page") int page,
//            @Parameter(name = "size") @RequestParam("size") int size,
//            @Parameter(name = "tableName", description = "When the credential's connectType = JDBC, tableName is required") @RequestParam(value = "tableName", required = false) String tableName,
//            @Parameter(name = "registryId", description = "When the credential's connectType = QUEUE, registryId is required") @RequestParam(value = "registryId", required = false) Integer registryId) {
//        IDataConnectionMinder minder = dataConnectionMinderManager.getMinder(credentialId);
//        SchemaRegistry schemaRegistry = null;
//        if (registryId != null) {
//            schemaRegistry = minder.schemaRegistry(registryId);
//        } else {
//            schemaRegistry = minder.schemaRegistry(tableName);
//        }
//        return success(dataConnectionMinderManager.fetchSamples(credentialId, schemaRegistry, page, size));
//    }

}
