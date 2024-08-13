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

package io.innospots.libra.kernel.navigation;

import io.innospots.base.utils.CCH;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.module.menu.reader.MenuItemReader;
import io.innospots.libra.kernel.module.system.entity.RoleResourceEntity;
import io.innospots.libra.kernel.module.system.model.role.resource.RoleResourceInfo;
import io.innospots.libra.kernel.module.system.service.UserResourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/19
 */
@Component
public class LibraNavigationMenuReader {


    private final MenuItemReader menuItemReader;

    private final UserResourceService userResourceService;

    private final AuthProperties authProperties;

    public LibraNavigationMenuReader(MenuItemReader menuItemReader,
                                     UserResourceService userResourceService,
                                     AuthProperties authProperties) {
        this.menuItemReader = menuItemReader;
        this.userResourceService = userResourceService;
        this.authProperties = authProperties;
    }

    /**
     * show user menu
     *
     * @return
     */
    public List<ResourceItem> showNavigationMenu() {
        if (authProperties.isOpenSuperAdminPermission()) {
            boolean superAdminRole = userResourceService.isSuperAdminRole();
            //user admin
            if (superAdminRole) {
                return menuItemReader.listMenuItems();
            }
        }

        List<RoleResourceEntity> roleResourceEntities = userResourceService.getUserResourceIds(CCH.userId());

        Set<String> menuItemKeys = roleResourceEntities.stream()
                .filter(item -> RoleResourceInfo.RoleResourceType.valueOf(item.getResourceType()) == RoleResourceInfo.RoleResourceType.MENU)
                .map(RoleResourceEntity::getItemKey)
                .collect(Collectors.toSet());
        Set<String> optItemKeys = roleResourceEntities.stream()
                .filter(item -> RoleResourceInfo.RoleResourceType.valueOf(item.getResourceType()) == RoleResourceInfo.RoleResourceType.OPERATION)
                .map(RoleResourceEntity::getItemKey)
                .collect(Collectors.toSet());

        List<ResourceItem> resourceItems = menuItemReader.listMenuItemsByItemKey(menuItemKeys, optItemKeys, true);
        if (CollectionUtils.isEmpty(resourceItems)) {
            resourceItems = LibraClassPathExtPropertiesLoader.getAdminDefaultMenuItems();
        }
        return resourceItems;
    }
}