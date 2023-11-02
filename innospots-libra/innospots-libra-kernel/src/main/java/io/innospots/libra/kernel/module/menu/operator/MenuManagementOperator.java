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

package io.innospots.libra.kernel.module.menu.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.utils.StringConverter;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.menu.BaseItem;
import io.innospots.libra.base.menu.ItemType;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.events.MenuDelEvent;
import io.innospots.libra.kernel.module.menu.converter.MenuResourceBeanConverter;
import io.innospots.libra.kernel.module.menu.dao.MenuResourceDao;
import io.innospots.libra.kernel.module.menu.entity.MenuResourceEntity;
import io.innospots.libra.kernel.module.menu.model.MenuOrders;
import io.innospots.libra.kernel.module.menu.model.MenuResourceItem;
import io.innospots.libra.kernel.module.menu.model.NewMenuItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenc
 * @date 2021/6/20 21:58
 */
@Slf4j
@Service
public class MenuManagementOperator extends ServiceImpl<MenuResourceDao, MenuResourceEntity> {

    /**
     * create menu
     *
     * @param menuItem
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResourceItem createMenu(NewMenuItem menuItem) {
        if (StringUtils.isEmpty(menuItem.getItemKey()) && menuItem.getResourceId() == null) {
            boolean hasExist;
            do {
                menuItem.setItemKey(menuItem.getItemType().name() + "_" + StringConverter.randomKey(8));
                hasExist = this.checkItemKey(menuItem);

            } while (hasExist);
        } else {
            if (this.checkItemKey(menuItem)) {
                throw ResourceException.buildDuplicateException(this.getClass(), "menuItem duplicate itemKey or resourceId" + menuItem);
            }
        }

        MenuResourceEntity menuResourceEntity = MenuResourceBeanConverter.INSTANCE.newItemEntity(menuItem);
        menuResourceEntity.setShowMenu(Boolean.TRUE);
        super.save(menuResourceEntity);
        return MenuResourceBeanConverter.INSTANCE.entityToModel(menuResourceEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMenu(NewMenuItem menuItem) {
        ResourceItem item = LibraClassPathExtPropertiesLoader.getResourceItem(menuItem.getItemKey());
        if (item != null) {
            if (item.isAdminDefault() || item.getViewScope() == BaseItem.ViewScope.V_PUBLIC) {
                if (!menuItem.getStatus()) {
                    //can't offline public and admin resource
                    throw ValidatorException.buildInvalidException(this.getClass(), menuItem.getItemKey(), " the resource item is system resource, which can't be set offline.");
                }
            }
        }
        MenuResourceEntity menuResourceEntity = this.getMenuById(menuItem.getResourceId());
        if (!menuResourceEntity.getItemKey().equals(menuItem.getItemKey())) {
            throw ValidatorException.buildInvalidException(this.getClass(), menuItem.getResourceId(), "menuItemKey can't be changed");
        }
        menuResourceEntity = MenuResourceBeanConverter.INSTANCE.newItemEntity(menuItem);
        return super.updateById(menuResourceEntity);
    }

    /**
     * batch delete menu
     *
     * @param resourceIds
     * @return
     */
    public Boolean deleteMenuByIds(List<Integer> resourceIds) {
        List<MenuResourceEntity> resourceEntities = listByIds(resourceIds);
        for (MenuResourceEntity resourceEntity : resourceEntities) {
            ResourceItem resourceItem = LibraClassPathExtPropertiesLoader.getResourceItem(resourceEntity.getItemKey());
            if (resourceItem != null) {
                if (resourceItem.isAdminDefault()) {
                    throw ResourceException.buildDeleteException(this.getClass(), resourceItem.getItemKey(), "the system resource can't be deleted");
                }
            }
        }//end for
        return this.removeByIds(resourceIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMenuById(Integer resourceId) {
        List<Integer> resourceIds = new ArrayList<>();
        this.traverseSubResourceIds(resourceId, resourceIds);
        resourceIds.add(resourceId);
        List<MenuResourceEntity> resourceEntities = listByIds(resourceIds);
        List<String> itemKeys = resourceEntities.stream().map(MenuResourceEntity::getItemKey).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        Boolean result = this.deleteMenuByIds(resourceIds);
        if (result) {
            EventBusCenter.async(new MenuDelEvent(resourceId, itemKeys));
        }
        return result;
    }

    /**
     * list current resource
     *
     * @return
     */
    public List<MenuResourceItem> listMenuItems(String queryInput) {
        List<MenuResourceEntity> resourceEntities = this.listByItemType(queryInput, null);
        if (StringUtils.isBlank(queryInput)) {
            return fillMenuResourceTree(resourceEntities);
        }
        return searchMenuResource(resourceEntities);
    }

    /**
     * list itemType resource
     *
     * @param itemType
     * @return
     */
    public List<MenuResourceItem> listMenuItemsByItemType(ItemType itemType) {
        List<MenuResourceEntity> resourceEntities = this.listByItemType(null, itemType);
        return this.fillMenuResourceTree(resourceEntities);
    }

    /**
     * traverse sub menu ids
     *
     * @param resourceId
     * @param resourceIds
     */
    private void traverseSubResourceIds(Integer resourceId, List<Integer> resourceIds) {
        List<MenuResourceEntity> entityList = this.getMenuByParentIdAndItemTypes(resourceId, null);
        if (CollectionUtils.isNotEmpty(entityList)) {
            resourceIds.addAll(entityList.stream().map(MenuResourceEntity::getResourceId).collect(Collectors.toList()));
            for (MenuResourceEntity entity : entityList) {
                this.traverseSubResourceIds(entity.getResourceId(), resourceIds);
            }
        }
    }

    /**
     * update menu status
     *
     * @param resourceId
     * @param status
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Integer resourceId, boolean status) {
        this.checkMenuExist(resourceId);
        MenuResourceEntity menuItem = this.getMenuById(resourceId);
        ResourceItem item = LibraClassPathExtPropertiesLoader.getResourceItem(menuItem.getItemKey());
        if (item != null) {
            if (item.isAdminDefault() || item.getViewScope() == BaseItem.ViewScope.V_PUBLIC) {
                if (!status) {
                    //can't offline public and admin resource
                    throw ResourceException.buildUpdateException(this.getClass(), menuItem.getItemKey(), "the system resource can't be set offline.");
                }
            }
        }
        UpdateWrapper<MenuResourceEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(MenuResourceEntity::getResourceId, resourceId)
                .set(MenuResourceEntity::getStatus, status);
        boolean flag = this.update(wrapper);
        if (flag) {
            List<MenuResourceEntity> entities = this.getMenuByParentIdAndItemTypes(resourceId, null);
            if (CollectionUtils.isNotEmpty(entities)) {
                if (!status) {
                    for (MenuResourceEntity entity : entities) {
                        entity.setStatus(false);
                    }
                    this.updateBatchById(entities);
                }

            } else {
                if (status) {
                    MenuResourceEntity entity = this.getMenuById(resourceId);
                    if (entity.getParentId() != null && entity.getParentId() > 0) {
                        entity = this.getMenuById(entity.getParentId());
                        if (!entity.getStatus()) {
                            entity.setStatus(true);
                            this.updateById(entity);
                        }
                    }
                }
            }
        }
        return flag;
    }

    /**
     * update meum status by appKey
     *
     * @param appKey
     * @param status
     * @return
     */
    public long updateMenuStatusByAppKey(String appKey, Boolean status) {
        return this.baseMapper.updateStatusByAppKey(appKey, status);
    }

    private List<MenuResourceItem> searchMenuResource(List<MenuResourceEntity> resourceEntities) {
        List<MenuResourceItem> menuResourceItems = resourceEntities.stream().map(MenuResourceBeanConverter.INSTANCE::entityToModel).collect(Collectors.toList());
        Map<Integer, List<MenuResourceEntity>> menuResourceMap = resourceEntities.stream().filter(menuResource -> menuResource.getParentId() != null)
                .collect(Collectors.groupingBy(MenuResourceEntity::getParentId));

        this.buildSubResourceItems(menuResourceItems, menuResourceMap);
        List<Integer> resourceIds = new ArrayList<>();
        this.buildResourceId(menuResourceItems, resourceIds);
        Map<Integer, Integer> map = resourceIds.stream().collect(Collectors.toMap(Function.identity(), v -> 1, Integer::sum));
        menuResourceItems.removeIf(item -> MapUtils.isNotEmpty(map) && map.get(item.getResourceId()) > 1);

        return menuResourceItems;
    }

    private void buildResourceId(List<MenuResourceItem> menuItems, List<Integer> resourceIds) {
        for (MenuResourceItem item : menuItems) {
            resourceIds.add(item.getResourceId());
            if (CollectionUtils.isNotEmpty(item.getSubItems())) {
                this.buildResourceId(item.getSubItems(), resourceIds);
            }
        }
    }

    private List<MenuResourceItem> fillMenuResourceTree(List<MenuResourceEntity> resourceEntities) {
        resourceEntities.forEach(item -> {
            if (item.getOrders() == null) {
                item.setOrders(0);
            }
        });
        resourceEntities.sort(Comparator.comparing(MenuResourceEntity::getOrders).reversed()
                .thenComparing(MenuResourceEntity::getCreatedTime));
        List<MenuResourceItem> menuResourceItems = resourceEntities.stream()
                .filter(menuResource -> menuResource.getParentId() == null || menuResource.getParentId() == 0)
                .map(MenuResourceBeanConverter.INSTANCE::entityToModel).collect(Collectors.toList());
        Map<Integer, List<MenuResourceEntity>> menuResourceMap = resourceEntities.stream().filter(menuResource -> menuResource.getParentId() != null)
                .collect(Collectors.groupingBy(MenuResourceEntity::getParentId));

        this.buildSubResourceItems(menuResourceItems, menuResourceMap);
        return menuResourceItems;
    }

    private void buildSubResourceItems(List<MenuResourceItem> menuItems, Map<Integer, List<MenuResourceEntity>> menuResourceMap) {
        for (MenuResourceItem item : menuItems) {

            if (MapUtils.isNotEmpty(menuResourceMap) && CollectionUtils.isNotEmpty(menuResourceMap.get(item.getResourceId()))) {
                List<MenuResourceEntity> menuResourceEntities = menuResourceMap.get(item.getResourceId());
                menuResourceEntities.sort(Comparator.comparing(MenuResourceEntity::getOrders).reversed()
                        .thenComparing(MenuResourceEntity::getCreatedTime));

                List<MenuResourceItem> subItems = menuResourceEntities.stream().filter(menuResource -> menuResource.getItemType() == ItemType.CATEGORY)
                        .map(MenuResourceBeanConverter.INSTANCE::entityToModel).collect(Collectors.toList());
                item.setSubItems(subItems);

                if (CollectionUtils.isNotEmpty(subItems)) {
                    this.buildSubResourceItems(subItems, menuResourceMap);
                }

                item.getSubItems().addAll(menuResourceEntities.stream()
                        .filter(menuResourceEntity -> menuResourceEntity.getItemType() != ItemType.CATEGORY)
                        .map(MenuResourceBeanConverter.INSTANCE::entityToModel).collect(Collectors.toList()));

                if (CollectionUtils.isNotEmpty(item.getSubItems())) {
                    item.getSubItems().forEach(BaseItem::fillI18n);
                }
            }
            item.fillI18n();
        }
    }

    /**
     * get menu by id
     *
     * @param resourceId
     * @return
     */
    private MenuResourceEntity getMenuById(Integer resourceId) {
        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        query.lambda().eq(MenuResourceEntity::getResourceId, resourceId);
        MenuResourceEntity menuResourceEntity = super.getOne(query);
        if (menuResourceEntity == null) {
            throw ResourceException.buildAbandonException(this.getClass(), "menu does not exist");
        }
        return menuResourceEntity;
    }

    /**
     * get menu by parentId and itemType
     *
     * @param resourceId
     * @return
     */
    private List<MenuResourceEntity> getMenuByParentIdAndItemTypes(Integer resourceId, List<ItemType> itemTypes) {
        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        if (resourceId != null) {
            query.lambda().eq(MenuResourceEntity::getParentId, resourceId);
        }
        if (CollectionUtils.isNotEmpty(itemTypes)) {
            query.lambda().in(MenuResourceEntity::getItemType, itemTypes);
        }
        query.lambda().orderByAsc(MenuResourceEntity::getOrders);
        return super.list(query);
    }

    public List<MenuResourceEntity> getByItemKey(String itemKey) {
        if (StringUtils.isBlank(itemKey)) {
            return Collections.emptyList();
        }
        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        query.lambda().eq(MenuResourceEntity::getItemKey, itemKey);
        return super.list(query);
    }

    public List<MenuResourceItem> orderItems(MenuOrders menuOrders) {
        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        query.lambda()
                .in(MenuResourceEntity::getItemKey, menuOrders.getItemKeys()).eq(MenuResourceEntity::getStatus, true)
                .or()
                .eq(MenuResourceEntity::getParentId, menuOrders.getParentId()).eq(MenuResourceEntity::getStatus, true);
        log.debug("query:{}", query.getTargetSql());
        List<MenuResourceEntity> menuResourceEntities = this.list(query);

        for (MenuResourceEntity menuResourceEntity : menuResourceEntities) {
            if (menuResourceEntity.getOrders() == null) {
                menuResourceEntity.setOrders(0);
            }
            menuResourceEntity.setParentId(menuOrders.getParentId());
            //menuResourceEntity.setParentItemKeys(menuOrders.getParentItemKeys());
        }
        Map<String, Integer> orderIdMap = new LinkedHashMap<>();
        int count = menuOrders.getItemKeys().size();
        for (String itemKey : menuOrders.getItemKeys()) {
            orderIdMap.put(itemKey, count--);
        }

        for (MenuResourceEntity menuResourceEntity : menuResourceEntities) {
            menuResourceEntity.setOrders(orderIdMap.get(menuResourceEntity.getItemKey()));
        }

        this.updateBatchById(menuResourceEntities);

        menuResourceEntities.sort(Comparator.comparing(MenuResourceEntity::getOrders).reversed()
                .thenComparing(MenuResourceEntity::getCreatedTime));

        List<MenuResourceItem> resourceItems = new ArrayList<>();
        for (MenuResourceEntity menuResourceEntity : menuResourceEntities) {
            MenuResourceItem menuResourceItem = MenuResourceBeanConverter.INSTANCE.entityToModel(menuResourceEntity);
            menuResourceItem.fillI18n();
            resourceItems.add(menuResourceItem);
        }

        return resourceItems;
    }

    /**
     * condition query
     *
     * @param queryInput
     * @param itemType
     * @return
     */
    private List<MenuResourceEntity> listByItemType(String queryInput, ItemType itemType) {
        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        if (itemType != null) {
            query.lambda().eq(MenuResourceEntity::getItemType, itemType);
        }
        if (StringUtils.isNotBlank(queryInput)) {
            query.lambda().likeRight(MenuResourceEntity::getName, queryInput);
        }
        return this.list(query);
    }

    /**
     * check different menu have the same itemKey
     *
     * @param menu
     */
    private boolean checkItemKey(NewMenuItem menu) {
        QueryWrapper<MenuResourceEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<MenuResourceEntity> lambda = queryWrapper.lambda();
        if (menu.getItemKey() != null) {
            lambda.eq(MenuResourceEntity::getItemKey, menu.getItemKey());
        }

        if (menu.getResourceId() != null) {
            lambda.ne(MenuResourceEntity::getResourceId, menu.getResourceId());
        }
        long count = super.count(queryWrapper);
        return count > 0;
    }

    /**
     * check menu exist
     *
     * @param resourceId
     * @return
     */
    private void checkMenuExist(Integer resourceId) {
        QueryWrapper<MenuResourceEntity> query = new QueryWrapper<>();
        query.lambda().eq(MenuResourceEntity::getResourceId, resourceId);
        long count = this.count(query);
        if (count <= 0) {
            throw ResourceException.buildAbandonException(this.getClass(), "menu does not exist");
        }
    }
}