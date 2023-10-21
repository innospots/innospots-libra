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

package io.innospots.libra.base.category;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ResourceException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Alfred
 * @date 2022/2/10
 */
public class BaseCategoryOperator extends ServiceImpl<BaseCategoryDao, BaseCategoryEntity> {

    private BaseCategory noCategory;

    private BaseCategory recycleBin;

    @Transactional(rollbackFor = Exception.class)
    public BaseCategory createCategory(String categoryName, CategoryType categoryType) {
        if (this.checkNameExist(categoryName, categoryType)) {
            throw ResourceException.buildExistException(this.getClass(), categoryName);
        }
        BaseCategoryEntity entity = new BaseCategoryEntity();
        entity.setCategoryName(categoryName);
        entity.setCategoryType(categoryType);
        super.save(entity);
        return BaseCategoryMapper.INSTANCE.entityToModel(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCategory(Integer categoryId, String categoryName, CategoryType categoryType) {
        if (this.checkNameExistAndExcludeOriginalName(categoryName, categoryType, categoryId)) {
            throw ResourceException.buildExistException(this.getClass(), categoryName);
        }
        return super.update(new UpdateWrapper<BaseCategoryEntity>().lambda()
                .set(BaseCategoryEntity::getCategoryName, categoryName)
                .eq(BaseCategoryEntity::getCategoryId, categoryId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCategory(Integer categoryId) {
        return super.removeById(categoryId);
    }

    public List<BaseCategory> listCategories(CategoryType categoryType) {
        List<BaseCategory> list = new ArrayList<>();
        list.add(getNoCategory());
        list.addAll(this.listCategoriesByType(categoryType));
        return list;
    }

    public List<BaseCategory> listCategoriesByType(CategoryType categoryType) {
        List<BaseCategoryEntity> entities = super.list(
                new QueryWrapper<BaseCategoryEntity>().lambda().eq(BaseCategoryEntity::getCategoryType, categoryType));
        entities.forEach(entity -> {
            if (entity.getOrders() == 0) {
                entity.setOrders(Integer.MAX_VALUE);
            }
        });
        entities.sort(Comparator.comparing(BaseCategoryEntity::getOrders));
        return BaseCategoryMapper.INSTANCE.entitiesToModels(entities);
    }

    public boolean checkNameExist(String categoryName, CategoryType categoryType) {
        return super.count(new QueryWrapper<BaseCategoryEntity>()
                .lambda().eq(BaseCategoryEntity::getCategoryName, categoryName)
                .eq(BaseCategoryEntity::getCategoryType, categoryType)) > 0;
    }

    public boolean checkNameExistAndExcludeOriginalName(String categoryName, CategoryType categoryType, Integer categoryId) {
        return super.count(
                new QueryWrapper<BaseCategoryEntity>()
                        .lambda().eq(BaseCategoryEntity::getCategoryName, categoryName)
                        .eq(BaseCategoryEntity::getCategoryType, categoryType)
                        .ne(BaseCategoryEntity::getCategoryId, categoryId)) > 0;
    }

    private BaseCategory getNoCategory() {
        if (this.noCategory == null) {
            noCategory = new BaseCategory();
            noCategory.setCategoryId(0);
            noCategory.setCategoryName("未分类"); // TODO 国际化
        }
        return this.noCategory;
    }

    public BaseCategory getRecycleBinCategory() {
        if (this.recycleBin == null) {
            recycleBin = new BaseCategory();
            recycleBin.setCategoryId(-1);
            recycleBin.setCategoryName("回收站"); // TODO 国际化
        }
        return this.recycleBin;
    }
}
