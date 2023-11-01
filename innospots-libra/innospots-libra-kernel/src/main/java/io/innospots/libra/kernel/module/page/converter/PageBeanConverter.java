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

package io.innospots.libra.kernel.module.page.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.kernel.module.page.entity.PageEntity;
import io.innospots.libra.kernel.module.page.model.Page;
import io.innospots.libra.kernel.module.page.model.PageDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Alfred
 * @date 2022/1/21
 */
@Mapper
public interface PageBeanConverter extends BaseBeanConverter<Page,PageEntity> {

    PageBeanConverter INSTANCE = Mappers.getMapper(PageBeanConverter.class);

    @Mappings({
            @Mapping(source = "id", target = "pageId")
    })
    PageEntity modelToEntity(Page model);

    @Mappings({
            @Mapping(source = "pageId", target = "id")
    })
    Page entityToModel(PageEntity entity);

    PageDetail modelToDetail(Page model);

    List<Page> entitiesToModels(List<PageEntity> entities);

}
