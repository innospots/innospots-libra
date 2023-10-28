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

package io.innospots.libra.kernel.module.i18n.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.libra.kernel.module.i18n.dao.I18nLanguageDao;
import io.innospots.libra.kernel.module.i18n.entity.I18nLanguageEntity;
import io.innospots.libra.kernel.module.i18n.mapper.I18NLanguageBeanConverter;
import io.innospots.libra.kernel.module.i18n.model.I18nLanguage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.innospots.base.enums.DataStatus.valueOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * i18n language crud
 *
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/20
 */
@Slf4j
@Component
public class I18nLanguageOperator {

    private I18nLanguageDao i18nLanguageDao;

    public I18nLanguageOperator(I18nLanguageDao i18nLanguageDao) {
        this.i18nLanguageDao = i18nLanguageDao;
    }

    public I18nLanguage getLanguage(Integer languageId) {
        I18nLanguageEntity entity = i18nLanguageDao.selectById(languageId);
        return entity == null ? null : I18NLanguageBeanConverter.INSTANCE.entityToModel(entity);
    }

    public I18nLanguage getLanguage(String locale) {
        I18nLanguageEntity entity = i18nLanguageDao.selectByLocale(locale);
        return entity == null ? null : I18NLanguageBeanConverter.INSTANCE.entityToModel(entity);
    }

    public Boolean hasExist(String locale) {
        I18nLanguageEntity entity = i18nLanguageDao.selectByLocale(locale);
        return entity != null && DataStatus.OnlineOrOffline(entity.getStatus());
    }

    public Boolean create(I18nLanguage i18nLanguage) {
        I18nLanguageEntity entity = i18nLanguageDao.selectByName(i18nLanguage.getName());
        if (entity != null && DataStatus.OnlineOrOffline(entity.getStatus())) {
            log.error("create I18nLanguage name is exists, {}", i18nLanguage.getName());
            throw ResourceException.buildExistException(this.getClass(), "I18nLanguage name exists", i18nLanguage.getName());
        }

        I18nLanguageEntity entityByLocale = i18nLanguageDao.selectByLocale(i18nLanguage.getLocale());
        if (entityByLocale != null && DataStatus.OnlineOrOffline(entityByLocale.getStatus())) {
            log.error("create I18nLanguage is locale exists, {}", i18nLanguage.getLocale());
            throw ResourceException.buildExistException(this.getClass(), "I18nLanguage locale exists", i18nLanguage.getLocale());
        }
        int num = 0;
        if (entity == null && entityByLocale == null) {
            entity = I18NLanguageBeanConverter.INSTANCE.modelToEntity(i18nLanguage);
            num = i18nLanguageDao.insert(entity);
        } else {
            if (entity == null && entityByLocale != null) {
                entity = entityByLocale;
            }
            I18NLanguageBeanConverter.INSTANCE.updateEntity4Model(entity, i18nLanguage);
            entity.setCreatedTime(LocalDateTime.now());
            entity.setUpdatedTime(LocalDateTime.now());
            num = i18nLanguageDao.updateById(entity);
        }

        return num == 1;
    }

    public Boolean updateLanguage(I18nLanguage i18nLanguage) {
        I18nLanguageEntity entity = i18nLanguageDao.selectById(i18nLanguage.getLanguageId());
        if (entity == null || DataStatus.REMOVED.equals(entity.getStatus())) {
            log.error("modify I18nLanguage {} is delete", i18nLanguage.getLanguageId());
            throw ResourceException.buildUpdateException(this.getClass(), "I18nLanguage is delete", i18nLanguage.getLanguageId());
        }

        I18nLanguageEntity entityByName = i18nLanguageDao.selectByName(i18nLanguage.getName());
        if (entityByName != null && !entityByName.getLanguageId().equals(entity.getLanguageId())) {
            log.error("create I18nLanguage is exists, {}", i18nLanguage.getLocale());
            throw ResourceException.buildExistException(this.getClass(), "I18nLanguage", i18nLanguage.getLocale());
        }
        I18nLanguageEntity entityByLocale = i18nLanguageDao.selectByLocale(i18nLanguage.getLocale());
        if (entityByLocale != null && !entityByLocale.getLanguageId().equals(entity.getLanguageId())) {
            log.error("create I18nLanguage is exists, {}", i18nLanguage.getLocale());
            throw ResourceException.buildExistException(this.getClass(), "I18nLanguage", i18nLanguage.getLocale());
        }


        I18NLanguageBeanConverter.INSTANCE.updateEntity4Model(entity, i18nLanguage);
        entity.setUpdatedTime(LocalDateTime.now());
        int num = i18nLanguageDao.updateById(entity);
        return num == 1;
    }

    public Boolean updateStatus(int languageId, DataStatus status) {
        I18nLanguageEntity entity = i18nLanguageDao.selectById(languageId);
        if (entity == null || DataStatus.REMOVED.equals(entity.getStatus())) {
            log.error("delete I18nLanguage {} is delete", languageId);
            throw ResourceException.buildDeleteException(this.getClass(), "I18nLanguage is delete", languageId);
        }
        if (DataStatus.REMOVED.equals(status) && !DataStatus.OFFLINE.equals(entity.getStatus())) {
            log.error("delete I18nLanguage {} not delete, status is {}", languageId, entity.getStatus());
            throw ResourceException.buildDeleteException(this.getClass(), "I18nLanguage {} not delete status is {}", languageId, entity.getStatus());
        }
        entity.setStatus(status);
        entity.setUpdatedTime(LocalDateTime.now());
        int num = i18nLanguageDao.updateById(entity);
        return num == 1;
    }

    public PageBody<I18nLanguage> pageLanguages(FormQuery formRequest) {
        DataStatus dataStatus = isNotBlank(formRequest.getQueryInput()) ? valueOf(formRequest.getQueryInput()) : null;
        return page(dataStatus, formRequest.getPage(), formRequest.getSize(), formRequest.getPaging());
    }

    public PageBody<I18nLanguage> page(DataStatus status, int page, int size, boolean paging) {
        PageBody<I18nLanguage> result = new PageBody<>();
        Page<I18nLanguageEntity> queryPage = new Page<>(page, size);
        QueryWrapper<I18nLanguageEntity> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        } else {
            queryWrapper.in("status", DataStatus.ONLINE, DataStatus.OFFLINE);
        }
        queryWrapper.orderByDesc(I18nLanguageEntity.FIELD_DEFAULT_LAN);
        queryWrapper.orderByAsc(I18nLanguageEntity.F_CREATED_TIME);

        if (paging) {
            queryPage = i18nLanguageDao.selectPage(queryPage, queryWrapper);
            if (queryPage != null) {
                result.setTotal(queryPage.getTotal());
                result.setCurrent(queryPage.getCurrent());
                result.setPageSize(queryPage.getSize());
                result.setTotalPage(queryPage.getPages());
                result.setList(CollectionUtils.isEmpty(queryPage.getRecords()) ? new ArrayList<I18nLanguage>() :
                        I18NLanguageBeanConverter.INSTANCE.entityToModelList(queryPage.getRecords()));
            }
        } else {
            List<I18nLanguageEntity> languageEntities = i18nLanguageDao.selectList(queryWrapper);
            result.setTotal((long) languageEntities.size());
            result.setPageSize((long) languageEntities.size());
            result.setCurrent(1L);
            result.setTotalPage(1L);
            result.setList(I18NLanguageBeanConverter.INSTANCE.entityToModelList(languageEntities));
        }


        return result;
    }

    public List<I18nLanguage> list(DataStatus status) {
        QueryWrapper<I18nLanguageEntity> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        } else {
            queryWrapper.in("status", DataStatus.ONLINE, DataStatus.OFFLINE);
        }

        List<I18nLanguageEntity> entityList = i18nLanguageDao.selectList(queryWrapper);
        return I18NLanguageBeanConverter.INSTANCE.entityToModelList(entityList);
    }
}
