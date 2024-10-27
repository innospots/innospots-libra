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

package io.innospots.libra.kernel.module.menu.controller;

import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.module.menu.reader.MenuItemReader;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * config menu items include operation elements
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/7/19
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "menu")
@ModuleMenu(menuKey = "libra-menu")
@Tag(name = "Menu Items")
public class MenuItemController {

    private final MenuItemReader menuItemReader;

    public MenuItemController(MenuItemReader menuItemReader) {
        this.menuItemReader = menuItemReader;
    }

    @GetMapping("items")
    public R<List<ResourceItem>> listMenuItems() {
        return R.success(menuItemReader.listMenuItems());
    }
}
