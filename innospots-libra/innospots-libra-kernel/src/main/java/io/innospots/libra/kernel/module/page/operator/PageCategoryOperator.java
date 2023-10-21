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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2022/1/21
 */
@Service
public class PageCategoryOperator extends BaseCategoryOperator {

    @Autowired
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
        List<BaseCategory> list = super.listCategories(CategoryType.PAGE);

        List<Map<String, Object>> groupList = pageOperator.listMaps(
                new QueryWrapper<PageEntity>()
                        .select("CATEGORY_ID,COUNT(1) CNT ")
                        .groupBy("CATEGORY_ID")
                        .eq("IS_DELETE", false)
                        .eq("PAGE_TYPE", "normal"));

        Map<Integer, Integer> groupMap = groupList.stream().collect(
                Collectors.toMap(
                        k -> Integer.valueOf(k.get("CATEGORY_ID").toString()),
                        v -> Integer.valueOf(v.get("CNT").toString()))
        );

        // fill subsetTotal
        for (BaseCategory category : list) {
            Integer count = groupMap.get(category.getCategoryId());
            category.setTotalCount(count == null ? 0 : count);
        }

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
