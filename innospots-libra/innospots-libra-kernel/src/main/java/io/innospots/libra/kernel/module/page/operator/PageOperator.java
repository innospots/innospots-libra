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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.dataset.Dataset;
import io.innospots.base.data.dataset.IDatasetReader;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.libra.kernel.module.page.dao.PageDao;
import io.innospots.libra.kernel.module.page.entity.PageEntity;
import io.innospots.libra.kernel.module.page.entity.WidgetEntity;
import io.innospots.libra.kernel.module.page.enums.PageOperationType;
import io.innospots.libra.kernel.module.page.converter.PageConverter;
import io.innospots.libra.kernel.module.page.model.Page;
import io.innospots.libra.kernel.module.page.model.PageDetail;
import io.innospots.libra.kernel.module.page.model.Widget;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Alfred
 * @date 2022/1/23
 */
@Service
public class PageOperator extends ServiceImpl<PageDao, PageEntity> {

    private final WidgetOperator widgetOperator;

    private final IDatasetReader datasetReader;

    public PageOperator(WidgetOperator widgetOperator, IDatasetReader datasetReader
    ) {
        this.widgetOperator = widgetOperator;
        this.datasetReader = datasetReader;
    }

    @Transactional(rollbackFor = Exception.class)
    public PageDetail createOrUpdate(PageDetail pageDetail, PageOperationType pageOperationType) {
        if (StringUtils.isNotBlank(pageDetail.getBoardExtConfig())) {
            Map<String, Object> config = JSONUtils.toMap(pageDetail.getBoardExtConfig());
            if (MapUtils.isNotEmpty(config)) {
                if (config.get("name") != null) {
                    String name = config.get("name").toString();
                    if (name.length() > 64) {
                        throw ValidatorException.buildInvalidException(this.getClass(), "page name is too long, no more than 64 characters");
                    }
                }
                if (config.get("subName") != null) {
                    String subName = config.get("subName").toString();
                    if (subName.length() > 64) {
                        throw ValidatorException.buildInvalidException(this.getClass(), "page title is too long, no more than 64 characters");
                    }
                }
            }
        }
        if (pageOperationType == PageOperationType.SAVE) {
            pageDetail.setStatus(DataStatus.OFFLINE);
        } else {
            pageDetail.setStatus(DataStatus.ONLINE);
        }

        // set default categoryId
        if (pageDetail.getId() != null) {
            PageEntity pageEntity = this.getById(pageDetail.getId());
            if (pageEntity.getCategoryId() == null) {
                pageDetail.setCategoryId(0);
            }
            if (StringUtils.isBlank(pageEntity.getPageType())) {
                pageDetail.setPageType("normal");
            }

        } else {
            if (StringUtils.isBlank(pageDetail.getPageType())) {
                pageDetail.setPageType("normal");
            }
            if (pageDetail.getCategoryId() == null) {
                pageDetail.setCategoryId(0);
            }
        }
        PageEntity entity = PageConverter.INSTANCE.modelToEntity(pageDetail);
        super.saveOrUpdate(entity);
        pageDetail.setId(entity.getPageId());

        List<Widget> widgets = pageDetail.getWidgets();
        if (CollectionUtils.isNotEmpty(widgets)) {
            widgets.forEach(v -> v.setDashboardId(pageDetail.getId()));
            widgetOperator.createOrUpdate(widgets);
        }

        if (CollectionUtils.isNotEmpty(pageDetail.getWidgetDeleteIds())) {
            widgetOperator.removeByIds(pageDetail.getWidgetDeleteIds());
        }
        return pageDetail;
    }

    // TODO h2上有bug
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePage(Integer pageId) {
        PageEntity entity = super.getById(pageId);
        if (DataStatus.valueOf(entity.getStatus()) == DataStatus.ONLINE) {
            throw ValidatorException.buildInvalidException(this.getClass(), "page status is enabled, cannot be delete");
        }

        if (!entity.getIsDelete()) {
            entity.setIsDelete(true);
            entity.setStatus(DataStatus.OFFLINE.name());
            super.updateById(entity);

            widgetOperator.update(
                    new UpdateWrapper<WidgetEntity>().lambda()
                            .eq(WidgetEntity::getPageId, pageId)
                            .set(WidgetEntity::getIsDelete, true));

        } else {
            super.removeById(pageId);
            widgetOperator.remove(new QueryWrapper<WidgetEntity>().lambda().eq(WidgetEntity::getPageId, pageId));
            // delete cascade menu
//            BeanContextAware.sendAppEvent(new DynamicMenuDelEvent(pageId, entity.getPageType()));
        }
        return true;
    }

    public PageDetail getPageDetail(Integer pageId) {
        PageEntity entity = super.getById(pageId);
        Page page = PageConverter.INSTANCE.entityToModel(entity);
        List<Widget> widgets = widgetOperator.list(pageId);

        PageDetail pageDetail = PageConverter.INSTANCE.modelToDetail(page);
        pageDetail.setWidgets(widgets);

        Set<Integer> viewIds = new HashSet<>();
        for (Widget widget : widgets) {
            for (String viewId : widget.getViewIds()) {
                if (StringUtils.isBlank(viewId)) {
                    continue;
                }
                viewIds.add(Integer.valueOf(viewId));
            }
        }
        if(viewIds.size() > 0){
            List<Dataset> views = datasetReader.listDatasets(viewIds);
            pageDetail.setViews(views);
        }else{
            pageDetail.setViews(Collections.emptyList());
        }


        return pageDetail;
    }


    public List<Page> listPages(Integer pageCategoryId) {
        QueryWrapper<PageEntity> query = new QueryWrapper<>();
        query.lambda()
                .eq(PageEntity::getPageType, "normal")
                .eq(PageEntity::getIsDelete, false)
                .orderByDesc(PageEntity::getUpdatedTime);

        if (pageCategoryId != null) {
            query.lambda().eq(PageEntity::getCategoryId, pageCategoryId);
        }

        List<PageEntity> entities = this.list(query);

        return PageConverter.INSTANCE.entitiesToModels(entities);
    }

    public PageBody<Page> pagePages(Integer pageCategoryId, String queryCode, int page, int size) {

        QueryWrapper<PageEntity> query = new QueryWrapper<>();
        query.lambda()
                .eq(PageEntity::getPageType, "normal")
                .orderByDesc(PageEntity::getUpdatedTime);

        if (pageCategoryId != null && pageCategoryId == -1) {
            query.lambda().eq(PageEntity::getIsDelete, true);
        } else {
            if (pageCategoryId != null) {
                query.lambda().eq(PageEntity::getCategoryId, pageCategoryId);
            }
            query.lambda().eq(PageEntity::getIsDelete, false);
        }

        if (StringUtils.isNotBlank(queryCode)) {
            query.lambda().like(PageEntity::getName, queryCode);
        }

        IPage<PageEntity> entityPage = super.page(PageDTO.of(page, size), query);

        List<Page> pages = PageConverter.INSTANCE.entitiesToModels(entityPage.getRecords());
        PageBody<Page> pageBody = new PageBody<>();
        pageBody.setList(pages);
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setTotal(entityPage.getTotal());
        return pageBody;
    }

    public boolean updateStatus(Integer pageId, DataStatus status) {
        return super.update(
                new UpdateWrapper<PageEntity>()
                        .lambda().eq(PageEntity::getPageId, pageId)
                        .set(PageEntity::getStatus, status));
    }

    public Boolean recyclePage(Integer id) {
        PageEntity entity = super.getById(id);
        if (entity == null) {
            return false;
        }

        if (Boolean.FALSE.equals(entity.getIsDelete())) {
            return false;
        }

        entity.setIsDelete(false);
        super.updateById(entity);
        return true;
    }
}