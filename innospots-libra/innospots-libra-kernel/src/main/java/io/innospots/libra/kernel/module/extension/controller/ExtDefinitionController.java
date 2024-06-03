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

package io.innospots.libra.kernel.module.extension.controller;

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.extension.LibraExtensionInformation;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.module.extension.exporter.ExtMenuExporter;
import io.innospots.libra.kernel.module.extension.operator.ExtDefinitionOperator;
import io.innospots.libra.kernel.module.extension.service.ExtensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.fail;
import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.base.model.response.ResponseCode.RESOURCE_ABANDON;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader.getLibraExtProperties;

/**
 * the menu can be fetched by appKey
 *
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/19
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "extension-store")
@ModuleMenu(menuKey = "libra-extension-store")
@Tag(name = "Extension Store")
public class ExtDefinitionController {

    private ExtensionService extensionService;

    private ExtDefinitionOperator extDefinitionOperator;

    private ExtMenuExporter extMenuExporter;

    public ExtDefinitionController(ExtDefinitionOperator extDefinitionOperator, ExtMenuExporter extMenuExporter,
                                   ExtensionService extensionService) {
        this.extDefinitionOperator = extDefinitionOperator;
        this.extMenuExporter = extMenuExporter;
        this.extensionService = extensionService;
    }


    @GetMapping("{extKey}/modules")
    @Operation(summary = "extension modules")
    public InnospotsResponse<List<ResourceItem>> listMenus(@PathVariable String extKey) {
        LibraExtensionProperties libraExtProperties = getLibraExtProperties(extKey);
        if (libraExtProperties == null) {
            return fail(RESOURCE_ABANDON);
        } else {
            List<ResourceItem> resourceItems = libraExtProperties.getModules();
            List<ResourceItem> newItems = new ArrayList<>();
            for (ResourceItem resourceItem : resourceItems) {
                ResourceItem newItem = resourceItem.moduleItem();
                newItems.add(newItem);
            }
            return success(newItems);
        }

    }

    @GetMapping("items/{parentMenuKey}")
    @Operation(summary = "extension dynamic sub menus")
    public InnospotsResponse<List<ResourceItem>> listSubMenus(@PathVariable String parentMenuKey) {

        return success(extMenuExporter.listMenuItems(parentMenuKey));
    }

    @GetMapping("list")
    @Operation(summary = "list definition extensions")
    public InnospotsResponse<List<LibraExtensionInformation>> listAppInfos() {
        //return success(appDefinitionOperator.listApplications());
        return success(extensionService.listLibraAppInformation(null));
    }

    /*
    @Operation(summary = "download the extension definition from market")
    @ResourceItemOperation(type = BUTTON, icon = "download", name = "${app.main.button.download}")
    @PostMapping("download/market/{id}")
    @OperationLog(operateType = OperateType.DOWNLOAD,idParamPosition = 0)
    public InnospotResponse<Boolean> downloadApplication4Market(@PathVariable String id){
        return InnospotResponse.success(true);
    }

     */

    /*
    @Deprecated
    @Operation(summary = "download the extension definition from desk")
    @ResourceItemOperation(type = BUTTON, icon = "download", name = "${app.main.button.download_desk}")
    @PostMapping("download/desk")
    @OperationLog(operateType = OperateType.DOWNLOAD)
    public InnospotResponse<LibraExtensionProperties> downloadExtension4Local(@RequestParam("file") MultipartFile file){
        return InnospotResponse.success(extensionService.registryApplication4Local(file));
    }
    */

    /**
     * Interface not in use, view {@link #}
     * @param extKey
     * @return @see InnospotResponse
     */
    /*
    @Deprecated
    @Operation(summary = "registry the extension definition")
    @PostMapping("registry/{extKey}")
    @OperationLog(operateType = OperateType.DOWNLOAD,idParamPosition = 0)
    public InnospotResponse<LibraExtensionProperties> registryExtension(@PathVariable String extKey){
        return InnospotResponse.success(extDefinitionOperator.registryExtensionDefinition(extKey));
    }

     */

}
