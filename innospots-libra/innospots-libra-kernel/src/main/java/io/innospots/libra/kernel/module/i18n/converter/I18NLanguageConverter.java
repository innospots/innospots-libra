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

package io.innospots.libra.kernel.module.i18n.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.kernel.module.i18n.entity.I18nLanguageEntity;
import io.innospots.libra.kernel.module.i18n.model.I18nLanguage;
import io.innospots.libra.kernel.module.i18n.model.TransHeaderColumn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * I18nLanguageConvertMapper
 *
 * @author Wren
 * @date 2022/1/16-22:17
 */
@Mapper
public interface I18NLanguageConverter extends BaseBeanConverter<I18nLanguage,I18nLanguageEntity> {

    I18NLanguageConverter INSTANCE = Mappers.getMapper(I18NLanguageConverter.class);

    /**
     * update I18nLanguageEntity from I18nLanguage
     *
     * @param entity
     * @param model
     */
    @Mapping(target = "languageId", ignore = true)
    void updateEntity4Model(@MappingTarget I18nLanguageEntity entity, I18nLanguage model);


    /**
     * I18nLanguageEntity to TransHeaderColumn
     *
     * @param entity
     * @return
     */
    @Mapping(target = "nationalFlagIcon", source = "entity.icon")
    TransHeaderColumn entityToTransHeaderColumn(I18nLanguageEntity entity);

}
