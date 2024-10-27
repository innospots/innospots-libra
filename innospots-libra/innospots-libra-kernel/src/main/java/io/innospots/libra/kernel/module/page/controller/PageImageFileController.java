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

package io.innospots.libra.kernel.module.page.controller;

import io.innospots.base.model.response.R;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.page.operator.DashboardFileOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2022/3/12
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "page/files")
@ModuleMenu(menuKey = "libra-page")
//@ModuleMenu(menuKey = "libra-page", key = "libra-page-image-file")
@Tag(name = "Page Dashboard File")
public class PageImageFileController {

    @Autowired
    private DashboardFileOperator dashboardFileOperator;


    @OperationLog(operateType = OperateType.UPLOAD)
    @ResourceItemOperation(key = "Page-publishPage")
    @PostMapping(value = "image", consumes = "multipart/form-data")
    @Operation(summary = "upload dashboard or widget background image")
    public R<String> uploadVizImage(
            @Parameter(name = "fileName") @RequestParam(required = false) String fileName,
            @Parameter(name = "image", required = true) @RequestPart("image") MultipartFile image) {
        return success(dashboardFileOperator.uploadFile(image, fileName));
    }
}
