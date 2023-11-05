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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.data.body.PageBody;
import io.innospots.libra.kernel.module.system.model.role.RoleInfo;
import io.innospots.base.utils.CCH;
import io.innospots.base.data.request.FormQuery;
import io.innospots.libra.kernel.module.system.dao.RoleDao;
import io.innospots.libra.kernel.module.system.entity.SysRoleEntity;
import io.innospots.libra.kernel.module.system.enums.SystemRoleCode;
import io.innospots.libra.kernel.module.system.converter.RoleInfoBeanConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ROLE CRUD
 *
 * @author chenc
 * @date 2021/2/9 20:29
 */
@Service
public class RoleOperator extends ServiceImpl<RoleDao, SysRoleEntity> {

    /**
     * save role
     *
     * @param role
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleInfo createRole(RoleInfo role) {
        this.checkDifferentRoleCode(role);
        this.checkDifferentRoleName(role);
        RoleInfoBeanConverter roleInfoBeanConverter = RoleInfoBeanConverter.INSTANCE;
        SysRoleEntity entity = roleInfoBeanConverter.modelToEntity(role);

        super.save(entity);
        return roleInfoBeanConverter.entityToModel(entity);
    }

    /**
     * update role
     *
     * @param role
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRole(RoleInfo role) {
        this.checkDefaultRoleCode(role.getRoleCode());
        this.checkDifferentRoleCode(role);
        this.checkDifferentRoleName(role);
        SysRoleEntity entity = this.getRoleEntity(role.getRoleId());
        entity.setRoleCode(role.getRoleCode());
        entity.setRoleName(role.getRoleName());
        entity.setAdmin(role.isAdmin());
        return super.updateById(entity);
    }

    /**
     * delete role
     *
     * @param roleId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRole(Integer roleId) {
        SysRoleEntity entity = this.getRoleEntity(roleId);
        this.checkDefaultRoleCode(entity.getRoleCode());
        return super.removeById(roleId);
    }

    /**
     * view role overview
     *
     * @param roleId
     * @return
     */
    public RoleInfo getRole(Integer roleId) {
        SysRoleEntity entity = this.getRoleEntity(roleId);
        return RoleInfoBeanConverter.INSTANCE.entityToModel(entity);
    }

    /**
     * Filter data sets by criteria
     *
     * @return
     */
    public PageBody<RoleInfo> list(FormQuery request) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysRoleEntity> lambda = queryWrapper.lambda();
        if (StringUtils.isNotEmpty(request.getQueryInput())) {
            lambda.like(SysRoleEntity::getRoleName, request.getQueryInput());
        }
        Integer projectId = CCH.projectId();
        if (projectId != null) {
            lambda.eq(SysRoleEntity::getProjectId, projectId);
        }
        if (StringUtils.isNotBlank(request.getSort())) {
            queryWrapper.orderBy(true, request.getAsc(),
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        }
        PageBody<RoleInfo> pageBody = new PageBody<>();
        List<SysRoleEntity> entities;
        if (!request.getPaging()) {
            entities = super.list(queryWrapper);

        } else {
            IPage<SysRoleEntity> iPage = new Page<>(request.getPage(), request.getSize());
            IPage<SysRoleEntity> entityPage = super.page(iPage, queryWrapper);
            entities = entityPage.getRecords();
            pageBody.setCurrent(entityPage.getCurrent());
            pageBody.setPageSize(entityPage.getSize());
            pageBody.setTotal(entityPage.getTotal());
            pageBody.setTotalPage(entityPage.getPages());
        }
        List<RoleInfo> roleInfos = entities.stream().map(RoleInfoBeanConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
        pageBody.setList(roleInfos);
        return pageBody;
    }

    /**
     * list role By roleIds
     *
     * @param roleIds
     * @return
     */
    public List<RoleInfo> listByRoleIds(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        List<SysRoleEntity> entities = super.listByIds(roleIds);
        return entities.stream().map(RoleInfoBeanConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    public boolean hasRoles(List<Integer> roleIds) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(SysRoleEntity::getRoleId, roleIds);
        long count = this.count(queryWrapper);
        return count == roleIds.size();
    }

    /**
     * check different role have the same roleCode
     *
     * @param role
     */
    private void checkDifferentRoleCode(RoleInfo role) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysRoleEntity> lambda = queryWrapper.lambda();
        lambda.eq(SysRoleEntity::getRoleCode, role.getRoleCode());
        if (role.getRoleId() != null) {
            lambda.ne(SysRoleEntity::getRoleId, role.getRoleId());
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "role code", role.getRoleCode());
        }
    }

    /**
     * check different role have the same roleName
     *
     * @param role
     */
    private void checkDifferentRoleName(RoleInfo role) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysRoleEntity> lambda = queryWrapper.lambda();
        lambda.eq(SysRoleEntity::getRoleName, role.getRoleName());
        if (role.getRoleId() != null) {
            lambda.ne(SysRoleEntity::getRoleId, role.getRoleId());
        }
        long count = super.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "role name", role.getRoleName());
        }
    }


    /**
     * check role code
     *
     * @param roleCode
     */
    private void checkDefaultRoleCode(String roleCode) {
        SystemRoleCode systemRoleCode = SystemRoleCode.getSystemRoleCodeByName(roleCode);
        if (systemRoleCode != null) {
            throw ValidatorException.buildInvalidException(this.getClass(), "The default roles built into the system cannot be modified. RoleCode : {%s}", roleCode);
        }
    }

    /**
     * check role exist
     *
     * @param roleId
     * @return
     */
    private SysRoleEntity getRoleEntity(Integer roleId) {
        SysRoleEntity roleEntity = super.getById(roleId);
        if (roleEntity == null) {
            throw ResourceException.buildExistException(this.getClass(), "role does not exist", roleId);
        }
        return roleEntity;
    }
}