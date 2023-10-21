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

package io.innospots.libra.kernel.module.system.service;

import io.innospots.base.enums.ImageType;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.model.user.RoleInfo;
import io.innospots.base.model.user.UserInfo;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.model.QueryRequest;
import io.innospots.base.utils.ImageFileUploader;
import io.innospots.libra.kernel.module.system.entity.UserRoleEntity;
import io.innospots.libra.kernel.module.system.model.user.UserForm;
import io.innospots.libra.kernel.module.system.operator.AvatarResourceOperator;
import io.innospots.libra.kernel.module.system.operator.RoleOperator;
import io.innospots.libra.kernel.module.system.operator.UserOperator;
import io.innospots.libra.kernel.module.system.operator.UserRoleOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/9
 */
@Slf4j
@Service
public class UserService {

    private final UserOperator userOperator;
    private final RoleOperator roleOperator;
    private final AvatarResourceOperator avatarResourceOperator;
    private final UserRoleOperator userRoleOperator;


    public UserService(UserOperator userOperator, RoleOperator roleOperator, AvatarResourceOperator avatarResourceOperator, UserRoleOperator userRoleOperator) {
        this.userOperator = userOperator;
        this.roleOperator = roleOperator;
        this.avatarResourceOperator = avatarResourceOperator;
        this.userRoleOperator = userRoleOperator;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserInfo createUser(UserForm userInfo) {
        //check role exist
        boolean hasRoles = roleOperator.hasRoles(userInfo.getRoleIds());
        if (!hasRoles) {
            throw ResourceException.buildAbandonException(this.getClass(), "roles are abandon, roleId:", userInfo.getRoleIds());
        }
        // create user
        UserInfo user = userOperator.createUser(userInfo);
        user.setAvatarBase64(userInfo.getAvatarBase64());
        Integer userId = user.getUserId();
        // create user role
        userRoleOperator.saveUserRoles(userId, userInfo.getRoleIds());
        // add user avatar
        avatarResourceOperator.createAvatar(userId, ImageType.AVATAR, userInfo.getAvatarBase64());
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUser(Integer userId) {
        // delete sys_user
        boolean result = userOperator.deleteUser(userId);
        if (result) {
            // delete user role
            userRoleOperator.deleteByUserIds(Collections.singletonList(userId));
            // delete user avatar
            avatarResourceOperator.deleteResource(userId, ImageType.AVATAR);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(UserForm userInfo) {
        boolean hasRoles = roleOperator.hasRoles(userInfo.getRoleIds());
        if (!hasRoles) {
            throw ResourceException.buildAbandonException(this.getClass(), "roles are abandon, roleId:", userInfo.getRoleIds());
        }
        boolean result = userOperator.updateUser(userInfo);
        if (result) {
            // update user role
            userRoleOperator.saveUserRoles(userInfo.getUserId(), userInfo.getRoleIds());
            // update user avatar
            if (StringUtils.isNotBlank(userInfo.getAvatarKey())) {
                avatarResourceOperator.updateAvatar(userInfo.getUserId(), ImageType.AVATAR, userInfo.getAvatarKey(), null);
            }
        }
        return result;
    }

    public UserInfo getUser(Integer userId) {
        UserInfo userInfo = userOperator.getUser(userId);

        List<UserRoleEntity> entities =
                userRoleOperator.selectRoleByUserId(userId);
        List<RoleInfo> roleInfos = roleOperator.listByRoleIds(entities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList()));

        userInfo.setAdmin(roleInfos.stream().anyMatch(RoleInfo::isAdmin));
        userInfo.setRoleIds(entities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList()));
        return userInfo;
    }

    public PageBody<UserInfo> pageUsers(QueryRequest request) {
        PageBody<UserInfo> pageBody = userOperator.pageUsers(request);
        List<UserInfo> userInfos = pageBody.getList();
        List<Integer> userIds = userInfos.stream().map(UserInfo::getUserId).collect(Collectors.toList());

        List<UserRoleEntity> userRoleEntities = userRoleOperator.selectRoleByUserIds(userIds);
        Map<Integer, List<UserRoleEntity>> userRoleMap = userRoleEntities.stream().collect(Collectors.groupingBy(UserRoleEntity::getUserId));
        List<Integer> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        List<RoleInfo> roleInfos = roleOperator.listByRoleIds(roleIds);

        Map<Integer, RoleInfo> roleInfoMap = roleInfos.stream().collect(Collectors.toMap(RoleInfo::getRoleId, Function.identity()));

        for (UserInfo userInfo : userInfos) {
            Integer userId = userInfo.getUserId();
            List<UserRoleEntity> userRoleEntityList = userRoleMap.get(userId);
            userInfo.setAdmin(false);
            if (CollectionUtils.isNotEmpty(userRoleEntityList)) {
                List<RoleInfo> roles = new ArrayList<>();
                for (UserRoleEntity userRoleEntity : userRoleEntityList) {
                    RoleInfo roleInfo = roleInfoMap.get(userRoleEntity.getRoleId());
                    if (roleInfo != null) {
                        roles.add(roleInfo);
                    }
                }
                userInfo.setRoleIds(roles.stream().map(RoleInfo::getRoleId).collect(Collectors.toList()));
                userInfo.setRoleNames(roles.stream().map(RoleInfo::getRoleName).collect(Collectors.toList()));
                userInfo.setAdmin(roles.stream().anyMatch(RoleInfo::isAdmin));
            }
        }
        return pageBody;
    }

    public String uploadAvatar(MultipartFile uploadFile) {
        if (!ImageFileUploader.checkSize(uploadFile)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SIZE_ERROR, ResponseCode.IMG_SIZE_ERROR.info());
        }
        if (!ImageFileUploader.checkSuffix(uploadFile, ImageFileUploader.IMG_SUFFIX_AVATAR)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SUFFIX_ERROR, ResponseCode.IMG_SUFFIX_ERROR.info());
        }

        try {
            Path parentPath = Files.createTempDirectory("innospots_avatar");
            String imgName = System.currentTimeMillis() + "_" + uploadFile.getOriginalFilename();
            ImageFileUploader.upload(uploadFile, parentPath.toFile().getAbsolutePath(), imgName,ImageType.AVATAR);
            String imgPath = parentPath.toFile().getAbsolutePath() + File.separator + imgName;
            return ImageFileUploader.readImageBase64(imgPath);

        } catch (IOException e) {
            log.error("upload avatar error:", e);
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_UPLOAD_ERROR, ResponseCode.IMG_UPLOAD_ERROR.info());
        }
    }

    /**
     * get users with the same permissions
     *
     * @return
     */
    public List<Integer> getRelatedUser() {
        List<Integer> userIds = new ArrayList<>();
        Integer userId = CCH.userId();
        userIds.add(userId);
        List<UserRoleEntity> userRoleEntities = userRoleOperator.selectRoleByUserId(userId);
        if (CollectionUtils.isNotEmpty(userRoleEntities)) {
            List<Integer> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
            userRoleEntities = userRoleOperator.selectByRoleIds(roleIds);
            userIds = userRoleEntities.stream().map(UserRoleEntity::getUserId).collect(Collectors.toList());
        }
        return userIds;
    }

    /**
     * whether the user has admin role
     *
     * @return
     */
    public boolean currentUserAdminRole() {
        List<UserRoleEntity> userRoleEntities = userRoleOperator.selectRoleByUserId(CCH.userId());
        List<Integer> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        List<RoleInfo> roleInfos = roleOperator.listByRoleIds(roleIds);
        return roleInfos.stream().anyMatch(RoleInfo::isAdmin);
    }
}