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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.json.JSONUtils;
import io.innospots.libra.kernel.module.page.dao.WidgetDao;
import io.innospots.libra.kernel.module.page.entity.WidgetEntity;
import io.innospots.libra.kernel.module.page.converter.WidgetBeanConverter;
import io.innospots.libra.kernel.module.page.model.Widget;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2022/1/24
 */
@Service
public class WidgetOperator extends ServiceImpl<WidgetDao, WidgetEntity> {

    public List<Widget> list(Integer pageId) {
        List<WidgetEntity> entities = super.list(
                new QueryWrapper<WidgetEntity>().lambda().eq(WidgetEntity::getPageId, pageId)
        );
        Map<Integer, WidgetEntity> entityMap = entities.stream().collect(Collectors.toMap(WidgetEntity::getWidgetId, Function.identity()));
        List<Widget> widgets = WidgetBeanConverter.INSTANCE.entitiesToModels(entities);
        if (CollectionUtils.isNotEmpty(widgets)) {
            for (Widget widget : widgets) {
                WidgetEntity entity = entityMap.get(widget.getId());
                if (StringUtils.isNotEmpty(entity.getViewIds())) {
                    widget.setViewIds(JSONUtils.toList(entity.getViewIds(), String.class));

                } else {
                    List<String> viewCodes = JSONUtils.toList(entity.getViewCodes(), String.class);
                    List<String> viewIds = new ArrayList<>();
                    for (String viewCode : viewCodes) {
                        if (StringUtils.isEmpty(viewCode)) {
                            continue;
                        }
                        String[] codes = viewCode.split("\\.");
                        viewIds.add(codes[2]);
                    }
                    widget.setViewIds(viewIds);
                }
            }
        }
        return widgets;
    }

    public List<Widget> createOrUpdate(List<Widget> widgets) {
        List<WidgetEntity> entities = WidgetBeanConverter.INSTANCE.modelsToEntities(widgets);
        super.saveOrUpdateBatch(entities);
        for (int i = 0; i < entities.size(); i++) {
            widgets.get(i).setId(entities.get(i).getWidgetId());
        }
        return widgets;
    }
}
