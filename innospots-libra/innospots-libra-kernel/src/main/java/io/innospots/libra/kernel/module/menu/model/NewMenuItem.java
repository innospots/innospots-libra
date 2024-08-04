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

package io.innospots.libra.kernel.module.menu.model;

import io.innospots.libra.base.menu.BaseItem;
import io.innospots.libra.base.menu.OptElement;
import io.innospots.libra.base.menu.ResourceItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * create menu item
 *
 * @author Smars
 * @date 2022/1/13
 */
@Getter
@Setter
@Schema(title = "create new menu item request model")
public class NewMenuItem extends BaseItem {

    @Schema(title = "primary key")
    private Integer resourceId;

    @Schema(title = "parent menu item resource id, primary id, the item is root menu, if parentId is zero")
    @NotNull(message = "parent menu must not be null")
    private Integer parentId;

    @Schema(title = "menu operation")
    private List<OptElement> opts;

    @Schema(title = "default value is 1, the biggest value is sorted in the front.")
    private Integer orders;

    @Schema(title = "true or false")
    private Boolean status;

    @Schema(title = "multi menus have different group")
    private String menuGroup;

    @Schema(title = "key is locale name, value is national translate name.")
    private Map<String, String> i18nNames;

    private String appKey;

    private String appName;

    private String parentItemKeys;

    private ResourceItem.LoadMode loadMode;
}