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

package io.innospots.libra.kernel.module.extension.mapper;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.kernel.module.extension.entity.ExtDefinitionEntity;
import io.innospots.libra.kernel.module.extension.entity.ExtInstallmentEntity;
import io.innospots.libra.kernel.module.extension.model.ExtensionInstallInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@Mapper
public interface AppInstallmentBeanConverter extends BaseBeanConverter {

    AppInstallmentBeanConverter INSTANCE = Mappers.getMapper(AppInstallmentBeanConverter.class);

    /**
     * @param extInstallmentEntity
     * @return
     */
    @Mapping(target = "extKey", source = "extInstallmentEntity.extKey")
    @Mapping(target = "name", source = "extDefinitionEntity.extName")
    @Mapping(target = "icon", source = "extDefinitionEntity.extIcon")
    @Mapping(target = "version", source = "extDefinitionEntity.extVersion")
    @Mapping(target = "status", source = "extInstallmentEntity.extensionStatus")
    ExtensionInstallInfo entityToModel(ExtInstallmentEntity extInstallmentEntity, ExtDefinitionEntity extDefinitionEntity);

    /**
     * @param appInstallInfo
     * @return
     */
    ExtInstallmentEntity modelToEntity(ExtensionInstallInfo appInstallInfo);
}
