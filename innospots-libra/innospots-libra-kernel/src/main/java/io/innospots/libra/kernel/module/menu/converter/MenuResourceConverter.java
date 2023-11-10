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

package io.innospots.libra.kernel.module.menu.converter;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.base.menu.OptElement;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.module.menu.entity.MenuResourceEntity;
import io.innospots.libra.kernel.module.menu.model.MenuResourceItem;
import io.innospots.libra.kernel.module.menu.model.NewMenuItem;
import io.innospots.libra.kernel.module.system.model.role.resource.OperateMenuResourceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * @author Smars
 * @date 2022/1/12
 */
@Mapper
public interface MenuResourceConverter extends BaseBeanConverter<MenuResourceItem,MenuResourceEntity> {

    MenuResourceConverter INSTANCE = Mappers.getMapper(MenuResourceConverter.class);

//    @Mapping(target = "i18nNames", expression = "java(jsonStrToStringMap(menuResourceEntity.getI18nNames()))")
    MenuResourceItem entityToModel(MenuResourceEntity menuResourceEntity);

//    @Mapping(target = "i18nNames", expression = "java(jsonStrToStringMap(menuResourceEntity.getI18nNames()))")
    ResourceItem entityToItem(MenuResourceEntity menuResourceEntity);

//    @Mapping(target = "i18nNames", expression = "java(jsonStrToStringMap(menuResourceEntity.getI18nNames()))")
    OperateMenuResourceItem entityToOperateModel(MenuResourceEntity menuResourceEntity);

//    @Mapping(target = "i18nNames", expression = "java(stringMapToJsonStr(newMenuItem.getI18nNames()))")
    MenuResourceEntity newItemEntity(NewMenuItem newMenuItem);

//    @Mapping(target = "i18nNames", expression = "java(jsonStrToStringMap(menuResourceEntity.getI18nNames()))")
    NewMenuItem entityToNewItem(MenuResourceEntity menuResourceEntity);

//    @Mapping(target = "i18nNames", expression = "java(stringMapToJsonStr(resourceItem.getI18nNames()))")
    MenuResourceEntity menuItemToEntity(ResourceItem resourceItem);

//    @Mapping(target = "i18nNames", expression = "java(stringMapToJsonStr(optElement.getI18nNames()))")
    MenuResourceEntity optItemEntity(OptElement optElement);

//    @Mapping(target = "i18nNames", expression = "java(jsonStrToStringMap(menuResourceEntity.getI18nNames()))")
    OptElement entityToOptItem(MenuResourceEntity menuResourceEntity);

}