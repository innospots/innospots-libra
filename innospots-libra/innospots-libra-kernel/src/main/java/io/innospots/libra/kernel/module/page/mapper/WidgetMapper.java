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

package io.innospots.libra.kernel.module.page.mapper;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.kernel.module.page.entity.WidgetEntity;
import io.innospots.libra.kernel.module.page.model.RelationWidget;
import io.innospots.libra.kernel.module.page.model.Widget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Alfred
 * @date 2022/1/24
 */
@Mapper
public interface WidgetMapper extends BaseBeanConverter {
    WidgetMapper INSTANCE = Mappers.getMapper(WidgetMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "widgetId"),
            @Mapping(source = "dashboardId", target = "pageId")
    })
    WidgetEntity modelToEntity(Widget model);

    @Mappings({
            @Mapping(source = "widgetId", target = "id"),
            @Mapping(source = "pageId", target = "dashboardId")
    })
    Widget entityToModel(WidgetEntity entity);

    List<Widget> entitiesToModels(List<WidgetEntity> entities);

    List<WidgetEntity> modelsToEntities(List<Widget> models);

    default List<RelationWidget> jsonStrToRelations(String jsonStr) {
        return JSONUtils.toList(jsonStr, RelationWidget.class);
    }

    default String relationsToJsonStr(List<RelationWidget> relations) {
        return JSONUtils.toJsonString(relations);
    }
}
