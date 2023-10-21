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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.enums.ImageType;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.utils.ImageFileUploader;
import io.innospots.libra.kernel.module.system.dao.AvatarResourceDao;
import io.innospots.libra.kernel.module.system.entity.AvatarResourceEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * AVATAR_RESOURCE CRUD
 *
 * @author chenc
 * @date 2022/1/19
 */
@Slf4j
@Service
public class AvatarResourceOperator extends ServiceImpl<AvatarResourceDao, AvatarResourceEntity> {

    @Transactional(rollbackFor = Exception.class)
    public void createAvatar(Integer resourceId, ImageType imageType, String imageBase64) {
        AvatarResourceEntity entity = new AvatarResourceEntity();
        entity.setResourceId(resourceId);
        entity.setImageType(imageType);
        entity.setImageBase64(imageBase64);
        super.save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateAvatar(Integer resourceId, ImageType imageType, String avatarBase64, Integer imageSort) {
        if (StringUtils.isBlank(avatarBase64)) {
            throw ValidatorException.buildMissingException(this.getClass(), "avatar must not be null");
        }
        AvatarResourceEntity entity = getByResourceIdAndType(resourceId, imageType);
        if (entity == null) {
            log.warn("avatar not exist, resourceId: {},imageType:{}", resourceId, imageType);
            entity = new AvatarResourceEntity();
            entity.setResourceId(resourceId);
            entity.setImageType(imageType);
            if (imageSort != null) {
                entity.setImageSort(imageSort);
            }
        }
        entity.setImageBase64(avatarBase64);
        return this.saveOrUpdate(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Integer resourceId, ImageType imageType) {
        LambdaQueryWrapper<AvatarResourceEntity> lambda = new QueryWrapper<AvatarResourceEntity>().lambda();
        lambda.eq(AvatarResourceEntity::getResourceId, resourceId);
        lambda.eq(AvatarResourceEntity::getImageType, imageType);
        super.remove(lambda);
    }

    public AvatarResourceEntity getByResourceIdAndType(Integer resourceId, ImageType imageType) {
        return getByResourceIdAndTypeAndSort(resourceId, imageType, null);
    }

    public AvatarResourceEntity getByResourceIdAndTypeAndSort(Integer resourceId, ImageType imageType, Integer imageSort) {
        QueryWrapper<AvatarResourceEntity> query = new QueryWrapper<>();
        query.lambda().eq(AvatarResourceEntity::getResourceId, resourceId);
        query.lambda().eq(AvatarResourceEntity::getImageType, imageType);
        if (imageSort != null) {
            query.lambda().eq(AvatarResourceEntity::getImageSort, imageSort);
        }
        return this.getOne(query);
    }

    public String upload(ImageType imageType, MultipartFile uploadFile) {
        if (!ImageFileUploader.checkSize(uploadFile)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SIZE_ERROR, ResponseCode.IMG_SIZE_ERROR.info());
        }
        if (!ImageFileUploader.checkSuffix(uploadFile, ImageFileUploader.IMG_SUFFIX_AVATAR)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SUFFIX_ERROR, ResponseCode.IMG_SUFFIX_ERROR.info());
        }

        try {
            Path parentPath = Files.createTempDirectory("innospots_" + imageType.getInfo().toLowerCase());
            String imgName = System.currentTimeMillis() + "_" + uploadFile.getOriginalFilename();
            ImageFileUploader.upload(uploadFile, parentPath.toFile().getAbsolutePath(), imgName,imageType);
            String imgPath = parentPath.toFile().getAbsolutePath() + File.separator + imgName;
            log.info("img path: {}", imgPath);
            return ImageFileUploader.readImageBase64(imgPath);

        } catch (IOException e) {
            log.error("upload error:", e);
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_UPLOAD_ERROR, ResponseCode.IMG_UPLOAD_ERROR.info());
        }
    }
}