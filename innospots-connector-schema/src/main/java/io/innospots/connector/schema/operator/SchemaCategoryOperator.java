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

package io.innospots.connector.schema.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.data.schema.SchemaRegistryType;
import io.innospots.connector.schema.entity.SchemaRegistryEntity;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.BaseCategoryOperator;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2022/2/6
 */
public class SchemaCategoryOperator extends BaseCategoryOperator {

    private final DatasetOperator datasetOperator;

    public SchemaCategoryOperator(DatasetOperator datasetOperator) {
        this.datasetOperator = datasetOperator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCategory(Integer categoryId) {
        // cascade delete
        datasetOperator.remove(
                new QueryWrapper<SchemaRegistryEntity>().lambda().eq(SchemaRegistryEntity::getCategoryId, categoryId));
        return super.deleteCategory(categoryId);
    }

    public List<BaseCategory> listCategories() {
        List<BaseCategory> list = super.listCategories(CategoryType.DATA_SET);

        List<Map<String, Object>> groupList = datasetOperator.listMaps(
                new QueryWrapper<SchemaRegistryEntity>()
                        .select("CASE WHEN CATEGORY_ID IS NULL THEN 0 ELSE CATEGORY_ID END AS CATEGORY_ID,COUNT(1) CNT ")
                        .groupBy("CATEGORY_ID")
                        .lambda()
                        .eq(SchemaRegistryEntity::getRegistryType, SchemaRegistryType.DATASET)
        );

        Map<Integer, Integer> groupMap = groupList.stream().collect(
                Collectors.toMap(
                        k -> Integer.valueOf(k.get("CATEGORY_ID").toString()),
                        v -> Integer.valueOf(v.get("CNT").toString())));

        // fill subsetTotal
        for (BaseCategory category : list) {
            Integer count = groupMap.get(category.getCategoryId());
            category.setTotalCount(count == null ? 0 : count);
        }

        return list;
    }
}
