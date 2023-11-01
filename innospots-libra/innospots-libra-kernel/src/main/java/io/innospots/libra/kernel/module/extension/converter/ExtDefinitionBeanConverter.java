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

package io.innospots.libra.kernel.module.extension.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.base.extension.LibraExtensionInformation;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.kernel.module.extension.entity.ExtDefinitionEntity;
import io.innospots.libra.kernel.module.extension.model.ExtensionDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/15
 */
@Mapper
public interface ExtDefinitionBeanConverter extends BaseBeanConverter {

    ExtDefinitionBeanConverter INSTANCE = Mappers.getMapper(ExtDefinitionBeanConverter.class);

    /**
     * @param extDefinitionEntity
     * @return
     */
    @Mapping(target = "name", source = "extDefinitionEntity.extName")
    @Mapping(target = "version", source = "extDefinitionEntity.extVersion")
    @Mapping(target = "status", source = "extDefinitionEntity.extensionStatus")
    ExtensionDefinition entityToModel(ExtDefinitionEntity extDefinitionEntity);

    /**
     * @param extDefinitionEntity
     * @return
     */
    @Mapping(target = "name", source = "extDefinitionEntity.extName")
    @Mapping(target = "icon", source = "extDefinitionEntity.extIcon")
    @Mapping(target = "version", source = "extDefinitionEntity.extVersion")
    @Mapping(target = "status", source = "extDefinitionEntity.extensionStatus")
    LibraExtensionInformation entityToAppInfo(ExtDefinitionEntity extDefinitionEntity);

    /**
     * @param extDefinition
     * @return
     */
    @Mapping(target = "extName", source = "extDefinition.name")
    @Mapping(target = "extVersion", source = "extDefinition.version")
    @Mapping(target = "extensionStatus", source = "extDefinition.status")
    ExtDefinitionEntity modelToEntity(ExtensionDefinition extDefinition);

    /**
     * @param libraAppProperties
     * @return
     */
    ExtensionDefinition propertiesToModel(LibraExtensionProperties libraAppProperties);

    /**
     * @param libraAppProperties
     * @return
     */
    LibraExtensionInformation extPropertiesToExtInfo(LibraExtensionProperties libraAppProperties);

    /**
     * update appDefinitionEntity from appDefinition
     *
     * @param extDefinitionEntity
     * @param extDefinition
     */
    @Mapping(target = "extName", source = "extDefinition.name")
    @Mapping(target = "extVersion", source = "extDefinition.version")
    @Mapping(target = "extensionStatus", ignore = true)
    ExtDefinitionEntity updateEntity4Model(@MappingTarget ExtDefinitionEntity extDefinitionEntity, ExtensionDefinition extDefinition);

    /***
     * list of ExtDefinitionEntity  to list of LibraExtInformation
     * @param entities
     * @return
     */
    default List<LibraExtensionInformation> entityToAppInfoList(List<ExtDefinitionEntity> entities) {
        List<LibraExtensionInformation> list = null;
        if (entities != null) {
            list = new ArrayList<>(entities.size());
            for (ExtDefinitionEntity entity : entities) {
                list.add(entityToAppInfo(entity));
            }
        }
        return list;
    }
}
