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

package io.innospots.libra.kernel.module.menu.reader;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.menu.ItemType;
import io.innospots.libra.base.menu.OptElement;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.module.menu.converter.MenuResourceConverter;
import io.innospots.libra.kernel.module.menu.dao.MenuResourceDao;
import io.innospots.libra.kernel.module.menu.entity.MenuResourceEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/7/19
 */
@Component
public class MenuItemReader {


    private MenuResourceDao menuResourceDao;

    public MenuItemReader(MenuResourceDao menuResourceDao) {
        this.menuResourceDao = menuResourceDao;
    }

    public List<ResourceItem> listMenuItems() {
        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        query.lambda()
                .eq(MenuResourceEntity::getStatus, true)
                .eq(MenuResourceEntity::getShowMenu, true);
        List<MenuResourceEntity> resourceEntities = menuResourceDao.selectList(query);
        HashSet<OptElement.UriMethod> excludes = new HashSet<>();
        excludes.add(OptElement.UriMethod.GET);
        HashSet<ItemType> itemTypes = new HashSet<>();
        itemTypes.add(ItemType.API);

        resourceEntities.forEach(item -> {
            if (item.getOrders() == null) {
                item.setOrders(0);
            }
        });
        resourceEntities.sort(Comparator.comparing(MenuResourceEntity::getOrders).reversed()
                .thenComparing(MenuResourceEntity::getCreatedTime));

        return convertToMenuTree(resourceEntities, LibraClassPathExtPropertiesLoader.listOptElements(excludes, itemTypes));
    }


    public List<ResourceItem> listMenuItemsByItemKey(Set<String> itemKeys, Set<String> optItemKeys, boolean status) {
        if (CollectionUtils.isEmpty(itemKeys)) {
            return Collections.emptyList();
        }

        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        query.lambda()
                .eq(MenuResourceEntity::getStatus, status)
                .eq(MenuResourceEntity::getShowMenu, true)
                .in(MenuResourceEntity::getItemKey, itemKeys);

        List<MenuResourceEntity> resourceEntities = menuResourceDao.selectList(query);
        resourceEntities.forEach(item -> {
            if (item.getOrders() == null) {
                item.setOrders(0);
            }
        });
        resourceEntities.sort(Comparator.comparing(MenuResourceEntity::getOrders).reversed()
                .thenComparing(MenuResourceEntity::getCreatedTime));

        Map<String, List<OptElement>> optElementMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(optItemKeys)) {
            optElementMap = LibraClassPathExtPropertiesLoader.listOptElements(optItemKeys);
        }
        return convertToMenuTree(resourceEntities, optElementMap);
    }

    /**
     * convert to menuTree from menuResourceEntity list
     *
     * @param resourceEntities
     * @return
     */
    private List<ResourceItem> convertToMenuTree(List<MenuResourceEntity> resourceEntities, Map<String, List<OptElement>> optElementMap) {
        List<ResourceItem> resourceItems = resourceEntities.stream()
                .filter(menuResource -> menuResource.getParentId() == null || menuResource.getParentId() == 0)
                .map(MenuResourceConverter.INSTANCE::entityToItem).collect(Collectors.toList());

        Map<Integer, List<MenuResourceEntity>> menuResourceMap = resourceEntities.stream().filter(menuResource -> menuResource.getParentId() != null)
                .collect(Collectors.groupingBy(MenuResourceEntity::getParentId));

        this.buildSubItems(resourceItems, menuResourceMap, optElementMap);
        return resourceItems;
    }

    /**
     * build sub menu
     *
     * @param resourceItems
     * @param menuResourceMap
     * @return
     */
    private void buildSubItems(List<ResourceItem> resourceItems, Map<Integer, List<MenuResourceEntity>> menuResourceMap, Map<String, List<OptElement>> optElementMap) {
        for (ResourceItem item : resourceItems) {
            item.setOpts(optElementMap.get(item.getItemKey()));
            if (MapUtils.isNotEmpty(menuResourceMap) && CollectionUtils.isNotEmpty(menuResourceMap.get(item.getResourceId()))) {
                List<MenuResourceEntity> menuResourceEntities = menuResourceMap.get(item.getResourceId());

                menuResourceEntities.sort(Comparator.comparing(MenuResourceEntity::getOrders, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
                        .thenComparing(MenuResourceEntity::getCreatedTime));

                List<ResourceItem> subItems = menuResourceEntities.stream().filter(menuResource -> ItemType.valueOf(menuResource.getItemType()) == ItemType.CATEGORY)
                        .map(MenuResourceConverter.INSTANCE::entityToItem).collect(Collectors.toList());
                item.setItems(subItems);

                if (CollectionUtils.isNotEmpty(subItems)) {
                    this.buildSubItems(subItems, menuResourceMap, optElementMap);
                }
                item.getItems().addAll(menuResourceEntities.stream()
                        .filter(menuResourceEntity -> ItemType.valueOf(menuResourceEntity.getItemType()) != ItemType.CATEGORY)
                        .map(MenuResourceConverter.INSTANCE::entityToItem).toList());
                if (CollectionUtils.isNotEmpty(item.getItems())) {
                    item.getItems().forEach(menuItem -> {
                        menuItem.setOpts(optElementMap.get(menuItem.getItemKey()));
                        menuItem.fillI18n();
                    });
                }
            }
            item.fillI18n();
        }
    }


}
