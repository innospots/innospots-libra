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

import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.dataset.Dataset;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.schema.dataset.DatasetOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Alfred
 * @date 2022/1/27
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "schema/data-set")
@ModuleMenu(menuKey = "libra-dataset")
@Tag(name = "Schema Dataset")
public class DatasetController {

    private final DatasetOperator datasetOperator;

    public DatasetController(
            DatasetOperator datasetOperator) {
        this.datasetOperator = datasetOperator;
    }

    @PostMapping
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(summary = "create dataset")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "id")
    public InnospotResponse<Dataset> createDataset(
            @Parameter(name = "dataset") @Validated @RequestBody Dataset dataset, BindingResult bindingResult) {
        Dataset create = datasetOperator.createDataset(dataset);
        return success(create);
    }

    @PutMapping
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}")
    @Operation(summary = "update dataset")
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "id")
    public InnospotResponse<Dataset> updateDataset(
            @Parameter(name = "dataset") @Validated @RequestBody Dataset dataset, BindingResult bindingResult) {
        Dataset update = datasetOperator.updateDataset(dataset);
        return success(update);
    }

    @DeleteMapping("{id}")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @OperationLog(idParamPosition = 0, operateType = OperateType.DELETE)
    @Operation(summary = "delete dataset")
    public InnospotResponse<Boolean> deleteDataset(@Parameter(name = "id") @PathVariable String id) {
        Boolean delete = datasetOperator.deleteDataset(id);
        return success(delete);
    }

    @GetMapping("{id}")
    @Operation(summary = "get dataset")
    public InnospotResponse<Dataset> getDatasetById(@Parameter(name = "id") @PathVariable String id) {
        Dataset dataset = datasetOperator.getDatasetById(id);
        return success(dataset);
    }

    @GetMapping("page")
    @Operation(summary = "page datasets")
    public InnospotResponse<PageBody<Dataset>> pageDatasets(
            @Parameter(name = "page") @RequestParam(value = "page") Integer page,
            @Parameter(name = "size") @RequestParam(value = "size") Integer size,
            @Parameter(name = "categoryId") @RequestParam(value = "categoryId", required = false, defaultValue = "0") Integer categoryId,
            @Parameter(name = "queryCode") @RequestParam(value = "queryCode", required = false) String queryCode,
            @Parameter(name = "sort", description = "sort field") @RequestParam(value = "sort", required = false, defaultValue = "createdTime") String sort
    ) {
        PageBody<Dataset> pageBody = datasetOperator.pageDatasets(categoryId, page, size, queryCode, sort);
        return success(pageBody);
    }

    @GetMapping("list")
    @Operation(summary = "list datasets")
    public InnospotResponse<List<Dataset>> listDatasets(
            @Parameter(name = "categoryId") @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @Parameter(name = "queryCode") @RequestParam(value = "queryCode", required = false) String queryCode,
            @Parameter(name = "sort", description = "sort field") @RequestParam(value = "sort", required = false, defaultValue = "createdTime") String sort
    ) {
        List<Dataset> listDatasets = datasetOperator.listDatasets(categoryId, queryCode, sort);
        return success(listDatasets);
    }

}
