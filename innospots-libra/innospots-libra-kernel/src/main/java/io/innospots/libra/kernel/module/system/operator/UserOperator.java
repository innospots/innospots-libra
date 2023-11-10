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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.crypto.PasswordEncoder;
import io.innospots.base.crypto.RsaKeyManager;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.base.events.NotificationAnnotation;
import io.innospots.libra.base.menu.AuthMode;
import io.innospots.base.data.request.FormQuery;
import io.innospots.libra.kernel.module.system.dao.UserDao;
import io.innospots.libra.kernel.module.system.entity.SysUserEntity;
import io.innospots.libra.kernel.module.system.converter.UserInfoConverter;
import io.innospots.libra.kernel.module.system.model.user.UserForm;
import io.innospots.libra.kernel.module.system.model.user.UserPassword;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SYS_USER CRUD
 *
 * @author chenc
 * @date 2021/2/7 23:06
 */
@Slf4j
@Service
public class UserOperator extends ServiceImpl<UserDao, SysUserEntity> {

    private final PasswordEncoder passwordEncoder;

    private final AuthProperties authProperties;

    public UserOperator(PasswordEncoder passwordEncoder, AuthProperties authProperties) {
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    /**
     * add user
     *
     * @param user
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @NotificationAnnotation(name = "${event.create.user}", code = "create_user",
            module = "libra-user",
            title = "${event.create.user.title}", content = "${event.create.user.content}")
    public UserInfo createUser(UserForm user) {
        if (StringUtils.isBlank(user.getPassword())) {
            throw ValidatorException.buildMissingException(this.getClass(), "password must not be null");
        }
        this.checkDifferentUserName(user.getUserName(), null);
        String password = RsaKeyManager.decrypt(user.getPassword(), authProperties.getPrivateKey());
        user.setPassword(passwordEncoder.encode(password));
        UserInfoConverter userInfoConverter = UserInfoConverter.INSTANCE;
        SysUserEntity sysUser = userInfoConverter.formModel2Entity(user);
        sysUser.setStatus(DataStatus.ONLINE);
        this.save(sysUser);
        return userInfoConverter.entityToModel(sysUser);
    }

    /**
     * update user
     *
     * @param user
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @NotificationAnnotation(name = "${event.update.user}", code = "update_user",
            module = "libra-user",
            title = "${event.update.user.title}", content = "${event.update.user.content}")
    public boolean updateUser(UserForm user) {
        if (authProperties.getDefaultSuperAdminUserId().equals(user.getUserId()) &&
                authProperties.getMode() == AuthMode.EXHIBITION) {
            throw AuthenticationException.buildPermissionException(this.getClass(), "this is exhibition mode, not allow change admin user.");
        }
        this.checkDifferentUserName(user.getUserName(), user.getUserId());
        SysUserEntity sysUser = this.getById(user.getUserId());
        if (sysUser == null) {
            throw ResourceException.buildExistException(this.getClass(), "user does not exist", user.getUserId());
        }
        sysUser.setEmail(user.getEmail());
        sysUser.setRealName(user.getRealName());
        sysUser.setUserName(user.getUserName());
        sysUser.setMobile(user.getMobile());
        sysUser.setDepartment(user.getDepartment());
        sysUser.setRemark(user.getRemark());
        sysUser.setAvatarKey(user.getAvatarKey());
        return this.updateById(sysUser);
    }

    /**
     * update user status
     *
     * @param userId
     * @param userStatus
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Integer userId, DataStatus userStatus) {
        this.checkUserExist(userId);
        UpdateWrapper<SysUserEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(SysUserEntity::getUserId, userId)
                .set(SysUserEntity::getStatus, userStatus);
        return this.update(wrapper);
    }

    /**
     * change user password
     *
     * @param userPassword
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(UserPassword userPassword) {
        if (authProperties.getDefaultSuperAdminUserId().equals(userPassword.getUserId()) &&
                authProperties.getMode() == AuthMode.EXHIBITION) {
            throw AuthenticationException.buildPermissionException(this.getClass(), "this is exhibition mode, not allow change admin password.");
        }
        this.checkUserExist(userPassword.getUserId());
        SysUserEntity user = this.getById(userPassword.getUserId());
        String password = RsaKeyManager.decrypt(userPassword.getNewPassword(), authProperties.getPrivateKey());
        user.setPassword(passwordEncoder.encode(password));
        return this.updateById(user);
    }

    /**
     * delete user
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Integer userId) {
        if (authProperties.getDefaultSuperAdminUserId().equals(userId)) {
            //superAdmin not be allowed delete
            return false;
        }
        return this.removeById(userId);
    }

    public List<SimpleUser> listByIds(List<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<SysUserEntity> userEntities = super.listByIds(userIds);
        return userEntities.stream().map(UserInfoConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(userEntities.size())));
    }

    public List<SimpleUser> listByNames(List<String> userNames) {
        if (CollectionUtils.isEmpty(userNames)) {
            return Collections.emptyList();
        }
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysUserEntity> lambda = queryWrapper.lambda();
        lambda.in(SysUserEntity::getUserName, userNames);
        List<SysUserEntity> userEntities = super.list(queryWrapper);
        return userEntities.stream().map(UserInfoConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(userEntities.size())));
    }

    /**
     * view user
     *
     * @param userId
     * @return
     */
    public UserInfo getUser(Integer userId) {
        SysUserEntity user = this.getById(userId);
        log.debug("user entity:{}", user);
        if (user == null) {
            throw ResourceException.buildExistException(this.getClass(), "user does not exist");
        }
        user.setPassword("------");
        return UserInfoConverter.INSTANCE.entityToModel(user);
    }


    /**
     * Filter data sets by criteria
     *
     * @return
     */
    public PageBody<UserInfo> pageUsers(FormQuery request) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysUserEntity> lambda = queryWrapper.lambda();
        if (StringUtils.isNotEmpty(request.getQueryInput())) {
            lambda.or().like(SysUserEntity::getUserName, request.getQueryInput())
                    .or().like(SysUserEntity::getRealName, request.getQueryInput());
        }
        if (StringUtils.isNotBlank(request.getSort())) {
            queryWrapper.orderBy(true, request.getAsc(),
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        }

        PageBody<UserInfo> pageBody = new PageBody<>();
        List<SysUserEntity> entities;
        if (request.getPaging()) {
            IPage<SysUserEntity> oPage = new Page<>(request.getPage(), request.getSize());
            IPage<SysUserEntity> entityPage = super.page(oPage, queryWrapper);
            entities = entityPage.getRecords();
            pageBody.setCurrent(entityPage.getCurrent());
            pageBody.setPageSize(entityPage.getSize());
            pageBody.setTotal(entityPage.getTotal());
            pageBody.setTotalPage(entityPage.getPages());
        } else {
            entities = super.list(queryWrapper);
        }
        entities.forEach(v -> v.setPassword(null));

        pageBody.setList(entities.stream().map(UserInfoConverter.INSTANCE::entityToModel).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size()))));
        return pageBody;
    }


    /**
     * check different user have the same username
     *
     * @param username
     */
    private void checkDifferentUserName(String username, Integer userId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysUserEntity> lambda = queryWrapper.lambda();
        lambda.eq(SysUserEntity::getUserName, username);
        if (userId != null) {
            lambda.ne(SysUserEntity::getUserId, userId);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), "user name", username);
        }
    }


    /**
     * check user exist
     *
     * @param userId
     * @return
     */
    private void checkUserExist(Integer userId) {
        QueryWrapper<SysUserEntity> query = new QueryWrapper<>();
        query.lambda().eq(SysUserEntity::getUserId, userId);
        long count = this.count(query);
        if (count <= 0) {
            throw ResourceException.buildAbandonException(this.getClass(), "user does not exist");
        }
    }
}