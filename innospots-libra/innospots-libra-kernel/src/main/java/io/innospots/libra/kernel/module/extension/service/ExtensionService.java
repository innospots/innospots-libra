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

package io.innospots.libra.kernel.module.extension.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.innospots.base.config.InnospotsConfigProperties;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.ZipFileUtils;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.base.configuration.InnospotsConsoleProperties;
import io.innospots.libra.base.extension.ExtensionStatus;
import io.innospots.libra.base.extension.LibraExtensionInformation;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.kernel.module.extension.converter.AppInstallmentConverter;
import io.innospots.libra.kernel.module.extension.entity.ExtDefinitionEntity;
import io.innospots.libra.kernel.module.extension.entity.ExtInstallmentEntity;
import io.innospots.libra.kernel.module.extension.model.ExtensionInstallInfo;
import io.innospots.libra.kernel.module.extension.operator.ExtDefinitionOperator;
import io.innospots.libra.kernel.module.extension.operator.ExtInstallmentOperator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ApplicationService
 *
 * @author Wren
 * @date 2022/7/26-22:57
 */
@Slf4j
@Service
@AllArgsConstructor
public class ExtensionService {

    private InnospotsConfigProperties innospotsConfigProperties;

    private ExtDefinitionOperator extDefinitionOperator;

    private ExtInstallmentOperator extInstallmentOperator;


    public List<LibraExtensionInformation> listLibraAppInformation(ExtensionStatus status) {
        List<LibraExtensionInformation> resList = new ArrayList<>();
        if (false) {
            //query app info from market
            //Query the defined application information of this service
            //Merge information
        } else {
            resList = extDefinitionOperator.listExtensions();
        }
        return resList;
    }


    private String getAppUploadFilePath(String fileName) {
        String appName = "";
        String appVersion = "";
        //TODO
        String[] arrs = fileName.substring(0, fileName.lastIndexOf(".")).split("_");
        appName = arrs[0];
        appVersion = arrs[1];
        String downPath = innospotsConfigProperties.getUploadFilePath() + File.separator + appName + File.separator + appVersion;
        return downPath;
    }


    /**
     * 通过本地上传发布包文件注册发布应用和更新新版本应用
     *
     * @param file 发布包文件
     * @return
     */
    public LibraExtensionProperties registryApplication4Local(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        //判断文件类型 zip
        if (!fileName.endsWith(".zip") && !fileName.endsWith(".ZIP")) {
            throw ResourceException.buildFileTypeException(this.getClass(), "file type is not zip");
        }

        String appDownPath = getAppUploadFilePath(fileName);
        File appDownFile = new File(appDownPath);
        if (!appDownFile.exists()) {
            appDownFile.mkdirs();
        }
        if (!appDownFile.isDirectory()) {
            throw ResourceException.buildCreateException(this.getClass(), "file is not directory", appDownPath);
        }

        String[] subFileNameArr = appDownFile.list();
        if (subFileNameArr != null && subFileNameArr.length > 0) {
            //TODO 目录中存在文件保存 或是 删除掉目录中的文件
            throw ResourceException.buildCreateException(this.getClass(), "file directory contains file", appDownPath);
        }

        String appFilePath = appDownPath + File.separator + fileName;
        //保存文件
        try {
            FileUtils.cleanDirectory(appDownFile);
            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(appFilePath));
        } catch (IOException e) {
            throw ResourceException.buildCreateException(this.getClass(), "file save error", appDownPath);
        }


        //解压文件
        List<String> zipFileNames = null;
        try {
            zipFileNames = ZipFileUtils.unZip(appFilePath, appDownPath);
        } catch (IOException e) {
            throw ResourceException.buildCreateException(this.getClass(), "file unzip error", appDownPath);
        }

        // 验证文件名称
        String appMetaDataFilePath = null;
        boolean hasJarFile = false;
        if (zipFileNames != null && zipFileNames.size() > 0) {
            for (String name : zipFileNames) {
                if (name.endsWith(".json")) {
                    appMetaDataFilePath = appDownPath + File.separator + name;
                }
                if (name.endsWith(".jar")) {
                    hasJarFile = true;
                }
            }

        }

        if (!hasJarFile) {
            throw ResourceException.buildCreateException(this.getClass(), "app unzip not contains jar file", appDownPath);
        }

        if (appMetaDataFilePath == null && appMetaDataFilePath.length() == 0) {
            throw ResourceException.buildCreateException(this.getClass(), "app unzip not contains metadata file", appDownPath);
        }

        //解析metadata文件
        LibraExtensionProperties libraAppProperties;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            libraAppProperties = objectMapper.readValue(new File(appMetaDataFilePath), LibraExtensionProperties.class);
        } catch (IOException e) {
            throw ResourceException.buildCreateException(this.getClass(), "file metadata read error", appMetaDataFilePath);
        }

        // zip 包中的文件名称要保存，在jar迁移到ext_lib目录时知道要拷贝的文件和判断文件是否存在，
        zipFileNames.add(fileName);
        libraAppProperties.setZipFileNames(zipFileNames);

        //注册应用信息
        libraAppProperties = extDefinitionOperator.registryExtensionDefinition(libraAppProperties);
        return libraAppProperties;
    }


    /**
     * 通过应用key安装应用到服务
     *
     * @param extKey
     * @return
     */
    public ExtensionInstallInfo install(String extKey) {
        ExtDefinitionEntity extDefinitionEntity = extDefinitionOperator.getLastVersion(extKey);
        if (extDefinitionEntity == null) {
            log.error("install app error, appKey {} is not exits", extKey);
            throw ResourceException.buildNotExistException(this.getClass(), "extKey: " + extKey);
        }
        if (!ExtensionStatus.valueOf(extDefinitionEntity.getExtensionStatus()).canBeInstall()) {
            throw ResourceException.buildInstallException(this.getClass(), "application is expired, appKey: " + extDefinitionEntity.getExtKey());
        }


        //TODO 是否已经安装  安装信息存在，licenseTime未过期
        ExtInstallmentEntity extInstallmentEntity = extInstallmentOperator.selectByExtKey(extDefinitionEntity.getExtKey());

        if (extInstallmentEntity != null) {
            //验证安装版本
            int compare = extDefinitionEntity.getExtVersion().compareToIgnoreCase(extInstallmentEntity.getInstallVersion());
            if (compare < 0) {
                //当前安装版本小于已经安装版本
                throw ResourceException.buildInstallException(this.getClass(), "application install version error, appKey: " + extDefinitionEntity.getExtKey());
            }
            extInstallmentEntity.setInstallVersion(extDefinitionEntity.getExtVersion());
        } else {
            extInstallmentEntity = new ExtInstallmentEntity();
            extInstallmentEntity.setExtKey(extDefinitionEntity.getExtKey());
            extInstallmentEntity.setInstallVersion(extDefinitionEntity.getExtVersion());
            extInstallmentEntity.setInstallTime(LocalDateTime.now());
            extInstallmentEntity.setExtensionStatus(ExtensionStatus.INSTALLED.name());
        }

        // 复制app jar到AppClassPath目录
        copyToClassPath(extDefinitionEntity);
        //保存或是更新应用安装信息
        extInstallmentEntity = extInstallmentOperator.saveInstallInfo(extInstallmentEntity);
        ExtensionInstallInfo appInstallInfo = AppInstallmentConverter.INSTANCE.entityToModel(extInstallmentEntity, extDefinitionEntity);
        //TODO 通知服务重启

        return appInstallInfo;
    }


    private void copyToClassPath(ExtDefinitionEntity extDefinitionEntity) {

        List<String> files = JSONUtils.toList(extDefinitionEntity.getZipFileNames(), String.class);
        if (files == null || files.isEmpty()) {
            return;
        }

        String fileName = files.stream().filter(item -> {
            if (item.endsWith(".zip") || item.endsWith(".ZIP")) {
                return true;
            } else {
                return false;
            }
        }).findFirst().get();
        List<String> jarFileNames = files.stream().filter(item -> item.endsWith(".jar")).collect(Collectors.toList());
        if (jarFileNames == null && jarFileNames.isEmpty()) {
            throw ResourceException.buildInstallException(this.getClass(), "application install error, jarFileNames not exist  appKey: " + extDefinitionEntity.getExtKey());
        }

        //验证文件路径
        String appDownPath = getAppUploadFilePath(fileName);
        File appDownFile = new File(appDownPath);
        if (!appDownFile.exists()) {
            throw ResourceException.buildInstallException(this.getClass(), "application install error, appDownPath not exist  appKey: " + appDownPath);
        }
        if (!appDownFile.isDirectory()) {
            throw ResourceException.buildInstallException(this.getClass(), "file is not directory", appDownPath);
        }

        //验证jar文件是否存在
        List<String> jarFileNameList = Arrays.asList(appDownFile.list((file, name) -> {
            if (name.toLowerCase().endsWith(".jar")) {
                return true;
            } else {
                return false;
            }
        }));
        for (String name : jarFileNames) {
            if (!jarFileNameList.contains(name)) {
                throw ResourceException.buildInstallException(this.getClass(), "file is not exist " + name);
            }
        }

        //备份classPath中app的jar文件
        File extLibDir = new File(innospotsConfigProperties.getExtLibPath());
        if (!extLibDir.exists()) {
            extLibDir.mkdirs();
        }
        if (!extLibDir.isDirectory()) {
            throw ResourceException.buildCreateException(this.getClass(), "extLibDir is not directory", innospotsConfigProperties.getExtLibPath());
        }

        File[] jarFileArr = extLibDir.listFiles((file, name) -> {
            if (name.startsWith(extDefinitionEntity.getExtKey())) {
                return true;
            } else {
                return false;
            }
        });
        String backupTime = DateTimeUtils.formatLocalDateTime(LocalDateTime.now(), "yyyyMMddHHmmss");
        if (jarFileArr != null && jarFileArr.length > 0) {
            try {
                for (File jarFile : jarFileArr) {
                    FileUtils.moveFile(jarFile, new File(jarFile.getPath() + backupTime));
                }
            } catch (IOException e) {
                log.error("extension install backup file error", e);
                throw ResourceException.buildCreateException(this.getClass(), "extension install backup file error ", extDefinitionEntity.getExtKey());
            }
        }

        //拷贝文件到classPath
        try {
            for (String name : jarFileNames) {
                FileUtils.moveFileToDirectory(new File(appDownPath + File.separator + name), extLibDir, false);

            }
        } catch (IOException e) {
            //TODO 回滚备份的jar
            log.error("extension install move file error", e);
            throw ResourceException.buildCreateException(this.getClass(), "extension install move file error ", extDefinitionEntity.getExtKey());
        }
    }

}
