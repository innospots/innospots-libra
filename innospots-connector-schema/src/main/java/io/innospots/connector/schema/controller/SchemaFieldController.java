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
import io.innospots.base.data.minder.IDataConnectionMinder;
import io.innospots.base.data.schema.SchemaField;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.connector.schema.operator.SchemaFieldOperator;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;


/**
 * @author Alfred
 * @date 2021-02-04
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "apps/schema/fields")
@ModuleMenu(menuKey = "libra-apps")
@Tag(name = "Schema Field")
public class SchemaFieldController extends BaseController {

    private final SchemaFieldOperator schemaFieldOperator;


    private final DataConnectionMinderManager dataConnectionMinderManager;

    public SchemaFieldController(
            SchemaFieldOperator schemaFieldOperator,
            DataConnectionMinderManager dataConnectionMinderManager
    ) {
        this.dataConnectionMinderManager = dataConnectionMinderManager;
        this.schemaFieldOperator = schemaFieldOperator;
    }


    @GetMapping("list")
    @Operation(summary = "list schema fields", description = "support SourceType: QUEUE,JDBC Parameter. requirements: registryId is required when SourceType = QUEUE, tableName is required when SourceType = JDBC")
    public InnospotResponse<List<SchemaField>> listSchemaFields(
            @Parameter(name = "credentialId") @RequestParam(value = "credentialId") Integer credentialId,
            @Parameter(name = "tableName") @RequestParam(value = "tableName", required = false) String tableName,
            @Parameter(name = "registryId") @RequestParam(value = "registryId", required = false) Integer registryId) {
        IDataConnectionMinder connectionMinder = dataConnectionMinderManager.getMinder(credentialId);
        SchemaRegistry schemaRegistry = registryId != null ? connectionMinder.schemaRegistry(registryId) : connectionMinder.schemaRegistry(tableName);
        if (schemaRegistry != null) {
            return success(schemaRegistry.getSchemaFields());
        } else {
            return success();
        }
    }

    @OperationLog(operateType = OperateType.UPDATE, primaryField = "fieldId")
    @PostMapping("upsert")
    @Operation(summary = "upsert schema field", description = "support SourceType: QUEUE")
    public InnospotResponse<SchemaField> upsertSchemaField(
            @Parameter(name = "schema field") @Validated @RequestBody SchemaField schemaField, BindingResult bindingResult) {
        SchemaField upsert = schemaFieldOperator.upsertSchemaField(schemaField);
        return success(upsert);
    }

    @PostMapping("parse")
    @Operation(summary = "schema field json parse")
    public InnospotResponse<List<SchemaField>> schemaFieldJsonParse(
            @Parameter(name = "json") @RequestBody String json) {
        List<SchemaField> schemaFields = schemaFieldOperator.parseToSchemaFields(json);
        return success(schemaFields);
    }

    @DeleteMapping("{fieldId}")
    @Operation(summary = "delete schema field", description = "support SourceType: QUEUE")
    public InnospotResponse<Boolean> deleteSchemaField(@Parameter(name = "fieldId") @PathVariable Integer fieldId) {
        Boolean delete = schemaFieldOperator.deleteSchemaField(fieldId);
        return success(delete);
    }

}
