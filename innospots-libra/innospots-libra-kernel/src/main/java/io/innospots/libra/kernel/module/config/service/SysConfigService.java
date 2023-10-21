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

package io.innospots.libra.kernel.module.config.service;

import io.innospots.base.enums.ImageType;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.model.system.OrganizationInfo;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.ImageFileUploader;
import io.innospots.libra.kernel.module.config.operator.SysConfigOperator;
import io.innospots.libra.kernel.module.system.entity.AvatarResourceEntity;
import io.innospots.libra.kernel.module.system.operator.AvatarResourceOperator;
import io.innospots.libra.kernel.service.SysConfigCommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * SysConfigService
 *
 * @author Wren
 * @date 2022/2/21-23:44
 */
@Slf4j
@Service
public class SysConfigService implements SysConfigCommonService {

    private final SysConfigOperator sysConfigOperator;
    private final AvatarResourceOperator avatarResourceOperator;


    public SysConfigService(SysConfigOperator sysConfigOperator, AvatarResourceOperator avatarResourceOperator) {
        this.sysConfigOperator = sysConfigOperator;
        this.avatarResourceOperator = avatarResourceOperator;
    }

    @Transactional
    public Integer saveOrganizationConfig(OrganizationInfo organizationInfo) {
        String favIcon = organizationInfo.getFavIcon();
        String logo = organizationInfo.getLogo();
        sysConfigOperator.saveOrganizationConfig(organizationInfo);

        //TODO resourceId
        if (StringUtils.isNotEmpty(favIcon)) {
            AvatarResourceEntity iconEntity = avatarResourceOperator.getByResourceIdAndType(CCH.organizationId(), ImageType.FAVICON);
            if (iconEntity == null) {
                avatarResourceOperator.createAvatar(CCH.organizationId(), ImageType.FAVICON, organizationInfo.getFavIcon());
            } else {
                avatarResourceOperator.updateAvatar(CCH.organizationId(), ImageType.FAVICON, organizationInfo.getFavIcon(), null);
            }
        }
        if (StringUtils.isNotEmpty(logo)) {
            AvatarResourceEntity logoEntity = avatarResourceOperator.getByResourceIdAndType(CCH.organizationId(), ImageType.LOGO);
            if (logoEntity == null) {
                avatarResourceOperator.createAvatar(CCH.organizationId(), ImageType.LOGO, organizationInfo.getLogo());
            } else {
                avatarResourceOperator.updateAvatar(CCH.organizationId(), ImageType.LOGO, organizationInfo.getLogo(), null);
            }
        }

        return 1;
    }

    @Override
    public OrganizationInfo getOrganization() {
        OrganizationInfo info = sysConfigOperator.getOrganization();
        if (info != null) {
            AvatarResourceEntity iconEntity = avatarResourceOperator.getByResourceIdAndType(CCH.organizationId(), ImageType.FAVICON);
            if (iconEntity != null) {
                info.setFavIcon(iconEntity.getImageBase64());
            }
            AvatarResourceEntity logoEntity = avatarResourceOperator.getByResourceIdAndType(CCH.organizationId(), ImageType.LOGO);
            if (logoEntity != null) {
                info.setLogo(logoEntity.getImageBase64());
            }
        }

        return info;
    }


    public String uploadFavicon(MultipartFile uploadFile) {

        return null;
    }

    public String uploadLogo(MultipartFile uploadFile) {
        if (!ImageFileUploader.checkSize(uploadFile)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SIZE_ERROR, ResponseCode.IMG_SIZE_ERROR.info());
        }
        if (!ImageFileUploader.checkSuffix(uploadFile, ImageFileUploader.IMG_SUFFIX_AVATAR)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SUFFIX_ERROR, ResponseCode.IMG_SUFFIX_ERROR.info());
        }
        String parentPath = "/upload_config_image" + File.separator;
        String imgName = System.currentTimeMillis() + "_" + uploadFile.getOriginalFilename();
        try {
            ImageFileUploader.upload(uploadFile, parentPath, imgName,ImageType.LOGO);
            String imgPath = parentPath + File.separator + imgName;
            return ImageFileUploader.readImageBase64(imgPath);

        } catch (IOException e) {
            log.error("upload config image error:", e);
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_UPLOAD_ERROR, ResponseCode.IMG_UPLOAD_ERROR.info());
        }
    }

}
