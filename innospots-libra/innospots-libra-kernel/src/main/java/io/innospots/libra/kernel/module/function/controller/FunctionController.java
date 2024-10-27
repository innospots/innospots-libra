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

package io.innospots.libra.kernel.module.function.controller;

import io.innospots.base.function.definition.model.FunctionDefinition;
import io.innospots.base.function.definition.operator.FunctionDefinitionOperator;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.kernel.module.function.model.FunctionCategory;
import io.innospots.libra.kernel.module.function.operator.FunctionViewOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;


/**
 * @author Smars
 * @date 2021/8/29
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "function")
@Tag(name = "Function Definition")
public class FunctionController extends BaseController {

    @Autowired
    private FunctionViewOperator functionViewOperator;

    @Autowired
    private FunctionDefinitionOperator functionDefinitionOperator;

    @GetMapping("list")
    @Operation(summary = "function list")
    public R<List<FunctionCategory>> listFunctions(
            @RequestParam(required = false) String functionType,
            @RequestParam(required = false) String cateType) {
        return success(functionViewOperator.listFunctionCategory(functionType, cateType));
    }


    @OperationLog(operateType = OperateType.CREATE, primaryField = "functionId", idParamPosition = 0)
    @PostMapping
    @Operation(summary = "create new function")
    public R<Boolean> createFunction(@RequestBody FunctionDefinition functionDefinition) {
        return R.success(functionDefinitionOperator.createFunction(functionDefinition));
    }


    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{functionId}")
    @Operation(summary = "delete function")
    public R<Boolean> deleteFunction(@PathVariable Integer functionId) {
        return R.success(functionDefinitionOperator.deleteFunction(functionId));
    }

    @OperationLog(operateType = OperateType.UPDATE, primaryField = "functionId", idParamPosition = 0)
    @PutMapping
    @Operation(summary = "update function")
    public R<Boolean> updateFunction(@RequestBody FunctionDefinition functionDefinition) {
        return R.success(functionDefinitionOperator.updateFunction(functionDefinition));
    }

}
