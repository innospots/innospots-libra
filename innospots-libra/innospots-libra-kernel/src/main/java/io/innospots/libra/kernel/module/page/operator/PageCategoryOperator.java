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
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.BaseCategoryOperator;
import io.innospots.libra.kernel.module.page.entity.PageEntity;
import io.innospots.libra.kernel.module.schema.entity.SchemaRegistryEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2022/1/21
 */
@Service
@AllArgsConstructor
public class PageCategoryOperator extends BaseCategoryOperator {

    private PageOperator pageOperator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCategory(Integer categoryId) {
        // cascade delete
        pageOperator.remove(new QueryWrapper<PageEntity>().lambda().eq(PageEntity::getCategoryId, categoryId));
        return super.deleteCategory(categoryId);
    }

    public List<BaseCategory> listCategoryPages() {
        return super.listCategoriesByType(CategoryType.PAGE);
    }

    public List<BaseCategory> listCategories() {
        List<BaseCategory> list = this.listCategories(CategoryType.PAGE, () -> {
            QueryWrapper<PageEntity> qw = new QueryWrapper<>();
            qw.lambda().select(PageEntity::getCategoryId,PageEntity::getPageId)
                    .eq(PageEntity::getIsDelete, false)
                    .eq(PageEntity::getPageType,"normal");
            List<PageEntity> entries = pageOperator.list(qw);
            return entries.stream().collect(Collectors.groupingBy(PageEntity::getCategoryId,Collectors.counting()));
        });
        BaseCategory recycle = getRecycleBinCategory();
        long count = pageOperator.count(
                new QueryWrapper<PageEntity>().lambda()
                        .eq(PageEntity::getIsDelete, true)
                        .eq(PageEntity::getPageType, "normal"));
        recycle.setTotalCount((int) count);
        list.add(recycle);
        return list;
    }


}
