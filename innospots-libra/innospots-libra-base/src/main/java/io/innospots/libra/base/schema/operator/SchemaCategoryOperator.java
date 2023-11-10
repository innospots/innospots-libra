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

package io.innospots.libra.base.schema.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.connector.schema.SchemaRegistryType;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.BaseCategoryOperator;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.schema.entity.SchemaRegistryEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2022/2/6
 */
public class SchemaCategoryOperator extends BaseCategoryOperator {

    private final SchemaRegistryOperator schemaRegistryOperator;

    public SchemaCategoryOperator(SchemaRegistryOperator schemaRegistryOperator) {
        this.schemaRegistryOperator = schemaRegistryOperator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCategory(Integer categoryId) {
        // cascade delete
        //datasetOperator.remove(new QueryWrapper<SchemaRegistryEntity>().lambda().eq(SchemaRegistryEntity::getCategoryId, categoryId));
        return super.deleteCategory(categoryId);
    }

    public List<BaseCategory> listCategories() {
        return this.listCategories(CategoryType.DATA_SET, () -> {
            QueryWrapper<SchemaRegistryEntity> qw = new QueryWrapper<>();
            qw.lambda().select(SchemaRegistryEntity::getCategoryId,SchemaRegistryEntity::getRegistryId)
                    .eq(SchemaRegistryEntity::getRegistryType,SchemaRegistryType.DATASET);
            List<SchemaRegistryEntity> entries = schemaRegistryOperator.list(qw);
            return entries.stream().collect(Collectors.groupingBy(SchemaRegistryEntity::getCategoryId,Collectors.counting()));
        });
    }
}
