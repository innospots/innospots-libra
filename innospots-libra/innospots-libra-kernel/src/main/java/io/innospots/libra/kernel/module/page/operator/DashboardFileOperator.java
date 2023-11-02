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

package io.innospots.libra.kernel.module.page.operator;

import io.innospots.base.enums.ImageType;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.libra.base.configuration.InnospotsConfigProperties;
import io.innospots.libra.base.utils.ImageFileUploader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Alfred
 * @date 2022/3/12
 */
@Slf4j
@Service
@AllArgsConstructor
public class DashboardFileOperator {

    private static final String DASHBOARD_FILE_PATH = "/resources/image/dashboard/";

    private InnospotsConfigProperties innospotsConfigProperties;

    public String uploadFile(MultipartFile file, String fileName) {

        if (!ImageFileUploader.checkSize(file)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SIZE_ERROR, ResponseCode.IMG_SIZE_ERROR.info());
        }

        if (!ImageFileUploader.checkSuffix(file, ImageFileUploader.IMG_SUFFIX_AVATAR)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SUFFIX_ERROR, ResponseCode.IMG_SUFFIX_ERROR.info());
        }

        String filePath = UUID.randomUUID() + (StringUtils.isBlank(fileName) ? file.getOriginalFilename() : fileName);
        String parentPath = innospotsConfigProperties.getUploadFilePath() + DASHBOARD_FILE_PATH;

        try {
            ImageFileUploader.upload(file, parentPath, filePath, ImageType.OTHER);
        } catch (IOException e) {
            log.error("upload image error:", e);
            throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_UPLOAD_ERROR, ResponseCode.IMG_UPLOAD_ERROR.info());
        }
        return DASHBOARD_FILE_PATH + filePath;
    }
}
