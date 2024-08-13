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

package io.innospots.libra.kernel.module.system.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ValidatorException;
import io.innospots.libra.kernel.module.system.dao.RoleResourceDao;
import io.innospots.libra.kernel.module.system.entity.RoleResourceEntity;
import io.innospots.libra.kernel.module.system.converter.RoleResourceInfoConverter;
import io.innospots.libra.kernel.module.system.model.role.resource.RoleResourceInfo;
import io.innospots.libra.kernel.module.system.model.role.resource.RoleResourceRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenc
 * @date 2021/6/26 22:58
 */
@Service
public class RoleResourceOperator extends ServiceImpl<RoleResourceDao, RoleResourceEntity> {

    /**
     * list by roleIds
     *
     * @param roleIds
     * @return
     */
    public List<RoleResourceEntity> listByRoleIds(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        QueryWrapper<RoleResourceEntity> query = new QueryWrapper<>();
        query.lambda().in(RoleResourceEntity::getRoleId, roleIds);

        return super.list(query);
    }

    public boolean hasRoleResource(List<Integer> roleIds, String itemKey) {
        QueryWrapper<RoleResourceEntity> query = new QueryWrapper<>();
        query.lambda()
                .in(RoleResourceEntity::getRoleId, roleIds)
                .eq(RoleResourceEntity::getItemKey, itemKey);
        long count = this.count(query);
        return count > 0;
    }


    /**
     * list role resource
     *
     * @return
     */
    @Deprecated
    public List<RoleResourceInfo> listMenuAuth() {
        QueryWrapper<RoleResourceEntity> query = new QueryWrapper<>();
        query.lambda().isNotNull(RoleResourceEntity::getItemKey);
        List<RoleResourceEntity> entities = super.list(query);
        return entities.stream().map(RoleResourceInfoConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    public List<RoleResourceInfo> listMenuAuthorities(String itemKey) {
        QueryWrapper<RoleResourceEntity> query = new QueryWrapper<>();
        query.lambda()
                .eq(RoleResourceEntity::getResourceType, RoleResourceInfo.RoleResourceType.MENU.name());
        if (StringUtils.isNotBlank(itemKey)) {
            query.lambda().eq(RoleResourceEntity::getItemKey, itemKey);
        }
        List<RoleResourceEntity> entities = super.list(query);
        return entities.stream().map(RoleResourceInfoConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    public List<RoleResourceInfo> listOperateAuthorities(Integer roleId) {
        QueryWrapper<RoleResourceEntity> query = new QueryWrapper<>();
        query.lambda()
                .eq(RoleResourceEntity::getRoleId, roleId)
                .eq(RoleResourceEntity::getResourceType, RoleResourceInfo.RoleResourceType.OPERATION.name());
        List<RoleResourceEntity> entities = super.list(query);
        return entities.stream().map(RoleResourceInfoConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleResourceAuthority(RoleResourceRequest roleResourceRequest) {
        //itemKey+roleId
        Set<String> itemKeyRoleIds = new HashSet<>();
        Set<Integer> roleIds = new HashSet<>();
        List<String> itemKeys = new ArrayList<>();
        ;
        for (Map.Entry<String, List<Integer>> entry : roleResourceRequest.getMenuItemRoles().entrySet()) {
            itemKeys.add(entry.getKey());
            for (Integer roleId : entry.getValue()) {
                itemKeyRoleIds.add(entry.getKey() + ":" + roleId);
                roleIds.add(roleId);
            }
        }//end for current role resource

        if (roleIds.isEmpty()) {
            throw ValidatorException.buildMissingException(this.getClass(), "roleId is missing");
        }
        String itemKey = null;
        if (roleResourceRequest.getMenuItemRoles().size() == 1) {
            itemKey = itemKeys.get(0);
        }

        RoleResourceInfo.RoleResourceType resourceType = roleResourceRequest.getRoleResourceType();
        QueryWrapper<RoleResourceEntity> query = new QueryWrapper<>();
        query.lambda().eq(RoleResourceEntity::getResourceType, resourceType.name());
        if (resourceType == RoleResourceInfo.RoleResourceType.OPERATION) {
            query.lambda().in(RoleResourceEntity::getRoleId, roleIds);
        }
        if (StringUtils.isNotBlank(itemKey)) {
            query.lambda().eq(RoleResourceEntity::getItemKey, itemKey);
        }

        //current the resources of the roles in db
        List<RoleResourceEntity> entities = super.list(query);

        Set<String> newItemKeyRoleIds = new HashSet<>(itemKeyRoleIds);

        Set<Integer> removeResourceRoleIds = new HashSet<>();

        for (RoleResourceEntity resourceEntity : entities) {
            String key = resourceEntity.getItemKey() + ":" + resourceEntity.getRoleId();
            //has exist, not need to update
            newItemKeyRoleIds.remove(key);

            //saved resources not in the request
            if (!itemKeyRoleIds.contains(key)) {
                //role resource in the table, but not include current request
                removeResourceRoleIds.add(resourceEntity.getRoleResourceId());
            }
        }//end role resource entity
        boolean up = true;
        //add new role resources
        if (!newItemKeyRoleIds.isEmpty()) {
            List<RoleResourceEntity> newRoleResources = new ArrayList<>();
            for (String newItemKeyRoleId : newItemKeyRoleIds) {
                String[] keyRoleIds = newItemKeyRoleId.split(":");
                RoleResourceEntity roleResourceEntity = new RoleResourceEntity();
                roleResourceEntity.setResourceType(roleResourceRequest.getRoleResourceType().name());
                roleResourceEntity.setItemKey(keyRoleIds[0]);
                roleResourceEntity.setRoleId(Integer.valueOf(keyRoleIds[1]));
                newRoleResources.add(roleResourceEntity);
            }//end new item key
            up = this.saveBatch(newRoleResources);
        }

        //delete old role resources
        if (!removeResourceRoleIds.isEmpty()) {
            up = this.removeByIds(removeResourceRoleIds) && up;
        }


        return up;
    }


/*
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenuAuth(RoleResourceRequest roleResourceRequest) {
        // add menu-auth role resource or add operate-auth role resource
        boolean result = this.addMenuAuthRoleResource(roleResourceRequest.getNewItems())
                && this.addOperateAuthRoleResource(roleResourceRequest.getNewOperateItems());
        // delete role resource
        if (result) {
            result = this.deleteRoleResource(roleResourceRequest.getRemoveItems());
        }
        return result;
    }
*/
    /**
     * add menu-auth role resource
     *
     * @param newItems
     * @return
     */
        /*
    @Deprecated
    public boolean addMenuAuthRoleResource(Map<Integer, List<Integer>> newItems) {
        if (MapUtils.isEmpty(newItems)) {
            return true;
        }
        List<RoleResourceEntity> entities = new ArrayList<>();
        newItems.forEach((key, roleIds) -> {
            if (CollectionUtils.isNotEmpty(roleIds)) {
                roleIds.forEach(roleId -> {
                    RoleResourceEntity entity = new RoleResourceEntity();
                    entity.setRoleId(roleId);
                    entity.setResourceId(key);
                    entities.add(entity);
                });
            }
        });
        return super.saveBatch(entities);
    }

     */

    /**
     * add operate-auth role resource
     *
     * @param newOperateItems
     * @return
     */
    @Deprecated
    public boolean addOperateAuthRoleResource(Map<Integer, List<String>> newOperateItems) {
        if (MapUtils.isEmpty(newOperateItems)) {
            return true;
        }
        List<RoleResourceEntity> entities = new ArrayList<>();
        newOperateItems.forEach((key, itemKeys) -> {
            if (CollectionUtils.isNotEmpty(itemKeys)) {
                itemKeys.forEach(itemKey -> {
                    RoleResourceEntity entity = new RoleResourceEntity();
                    entity.setRoleId(key);
                    entity.setItemKey(itemKey);
                    entities.add(entity);
                });
            }
        });
        return super.saveBatch(entities);
    }

    /**
     * delete role resource
     *
     * @param removeItems
     * @return
     */
    @Deprecated
    public boolean deleteRoleResource(List<Integer> removeItems) {
        if (CollectionUtils.isEmpty(removeItems)) {
            return true;
        }
        return super.removeByIds(removeItems);
    }

    public boolean deleteRoleResourceByItemKey(String itemKey) {
        QueryWrapper<RoleResourceEntity> whereWrapper = new QueryWrapper<>();
        whereWrapper.lambda().eq(RoleResourceEntity::getItemKey, itemKey);
        return this.remove(whereWrapper);
    }

    public boolean deleteRoleResourceByItemKeys(List<String> itemKeys) {
        if (CollectionUtils.isEmpty(itemKeys)) {
            return true;
        }
        QueryWrapper<RoleResourceEntity> whereWrapper = new QueryWrapper<>();
        whereWrapper.lambda().in(RoleResourceEntity::getItemKey, itemKeys);
        return this.remove(whereWrapper);
    }

    public boolean deleteRoleResourceByRoleId(Integer roleId) {
        QueryWrapper<RoleResourceEntity> whereWrapper = new QueryWrapper<>();
        whereWrapper.lambda().eq(RoleResourceEntity::getRoleId, roleId);
        return this.remove(whereWrapper);
    }

}