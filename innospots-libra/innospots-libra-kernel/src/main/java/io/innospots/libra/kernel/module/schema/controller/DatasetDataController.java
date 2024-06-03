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

import datart.ViewExecuteParam;
import datart.provider.Dataframe;
import datart.provider.StdSqlOperator;
import io.innospots.base.data.dataset.DatasetExecuteParam;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.kernel.module.schema.dataset.DataframeExecutor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.MapUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2022/2/24
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "schema/data")
@ModuleMenu(menuKey = "libra-dataset")
@Tag(name = "Schema Dataset Data")
public class DatasetDataController {

    private final DataframeExecutor dataframeExecutor;

    public DatasetDataController(DataframeExecutor dataframeExecutor) {
        this.dataframeExecutor = dataframeExecutor;
    }

    @PostMapping("execute")
    @Operation(summary = "dataset execute")
    public InnospotsResponse<Dataframe> execute(
            @Parameter(name = "credentialKey") @RequestParam("credentialKey") String credentialKey,
            @Parameter(name = "page") @RequestParam("page") int page,
            @Parameter(name = "size") @RequestParam("size") int size,
            @Parameter(name = "datasetExecuteParam") @RequestBody DatasetExecuteParam datasetExecuteParam) {
        Dataframe dataframe = dataframeExecutor.datasetData(credentialKey, page, size, datasetExecuteParam);
        return success(dataframe);
    }

    @PostMapping
    @Operation(summary = "dataset data")
    public InnospotsResponse<Dataframe> datasetData(
            @Parameter(name = "viewExecuteParam") @Validated @RequestBody ViewExecuteParam viewExecuteParam,
            BindingResult bindingResult) {
        Dataframe dataframe = dataframeExecutor.datasetData(viewExecuteParam);
        return success(dataframe);
    }

    @PostMapping("function/validate")
    @Operation(summary = "dataset function validate")
    public InnospotsResponse<Boolean> functionValidate(
            @Parameter(name = "validateParam") @RequestBody Map<String, Object> validateParam) {
        if (MapUtils.isEmpty(validateParam)) {
            return success(false);
        }
        Object viewId = validateParam.get("viewId");
        Object snippet = validateParam.get("snippet");
        if (viewId == null || snippet == null) {
            throw ValidatorException.buildInvalidException(this.getClass(), "viewId or snippet cannot be empty");
        }
        Boolean validata = dataframeExecutor.functionValidate(String.valueOf(viewId), String.valueOf(snippet));
        return success(validata);
    }

    @GetMapping("function/support/{viewId}")
    @Operation(summary = "function support list")
    public InnospotsResponse<Set<StdSqlOperator>> functionValidate(
            @Parameter(name = "viewId") @PathVariable(name = "viewId") String viewId) {
        Set<StdSqlOperator> sqlOperators = dataframeExecutor.supportedFunctions(viewId);
        return success(sqlOperators);
    }

}
