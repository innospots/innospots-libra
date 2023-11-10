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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.CaseFormat;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.request.FormQuery;
import io.innospots.libra.kernel.module.i18n.converter.I18NCurrencyConverter;
import io.innospots.libra.kernel.module.i18n.dao.I18nCurrencyDao;
import io.innospots.libra.kernel.module.i18n.dao.I18nLanguageDao;
import io.innospots.libra.kernel.module.i18n.entity.I18nCurrencyEntity;
import io.innospots.libra.kernel.module.i18n.entity.I18nLanguageEntity;
import io.innospots.libra.kernel.module.i18n.model.I18nCurrency;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * i18n currency crud
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/9
 */
@Slf4j
@Component
public class I18nCurrencyOperator {

    private final I18nCurrencyDao i18nCurrencyDao;

    private final I18nLanguageDao i18nLanguageDao;

    public I18nCurrencyOperator(I18nCurrencyDao i18nCurrencyDao, I18nLanguageDao i18nLanguageDao) {
        this.i18nCurrencyDao = i18nCurrencyDao;
        this.i18nLanguageDao = i18nLanguageDao;
    }

    public I18nCurrency getCurrency(int currencyId) {
        I18nCurrencyEntity entity = i18nCurrencyDao.selectById(currencyId);
        return entity == null ? null : I18NCurrencyConverter.INSTANCE.entityToModel(entity);
    }

    public Boolean createCurrency(I18nCurrency i18nCurrency) {
        I18nCurrencyEntity entity = i18nCurrencyDao.selectByName(i18nCurrency.getName());
        if (entity != null && DataStatus.OnlineOrOffline(entity.getStatus())) {
            log.error("create I18nCurrency name is exists, {}", i18nCurrency.getName());
            throw ResourceException.buildExistException(this.getClass(), "I18nCurrency", i18nCurrency.getName());
        }
        entity = i18nCurrencyDao.selectByCode(i18nCurrency.getCode());
        if (entity != null && DataStatus.OnlineOrOffline(entity.getStatus())) {
            log.error("create I18nCurrency code is exists, {}", i18nCurrency.getCode());
            throw ResourceException.buildExistException(this.getClass(), "I18nCurrency", i18nCurrency.getCode());
        }
        int num = 0;
        if (entity == null) {
            entity = I18NCurrencyConverter.INSTANCE.modelToEntity(i18nCurrency);
            num = i18nCurrencyDao.insert(entity);
        } else {
            I18NCurrencyConverter.INSTANCE.updateEntity4Model(entity, i18nCurrency);
            num = i18nCurrencyDao.updateById(entity);
        }
        return num == 1;
    }

    public Boolean updateCurrency(I18nCurrency i18nCurrency) {
        I18nCurrencyEntity entity = i18nCurrencyDao.selectById(i18nCurrency.getCurrencyId());
        if (entity == null || DataStatus.REMOVED.equals(entity.getStatus())) {
            log.error("modify I18nCurrency {} is delete", i18nCurrency.getCode());
            throw ResourceException.buildUpdateException(this.getClass(), "I18nCurrency is delete", i18nCurrency.getCode());
        }
        I18nCurrencyEntity entityByName = i18nCurrencyDao.selectByName(i18nCurrency.getName());
        if (entityByName != null && !entityByName.getCurrencyId().equals(entity.getCurrencyId())) {
            log.error("modify I18nCurrency name is exists, {}", i18nCurrency.getName());
            throw ResourceException.buildExistException(this.getClass(), "I18nCurrency", i18nCurrency.getName());
        }
        I18nCurrencyEntity entityByCode = i18nCurrencyDao.selectByCode(i18nCurrency.getCode());
        if (entityByCode != null && !entityByCode.getCurrencyId().equals(entity.getCurrencyId())) {
            log.error("modify I18nCurrency code is exists, {}", i18nCurrency.getCode());
            throw ResourceException.buildExistException(this.getClass(), "I18nCurrency", i18nCurrency.getCode());
        }

        if (i18nCurrency.getStatus().equals(DataStatus.OFFLINE)) {
            List<I18nLanguageEntity> languages = i18nLanguageDao.selectByCurrency(i18nCurrency.getCurrencyId());
            if (languages != null && !languages.isEmpty()) {
                throw ResourceException.buildUpdateException(this.getClass(), "I18nCurrency", i18nCurrency.getCode());
            }
        }

        I18NCurrencyConverter.INSTANCE.updateEntity4Model(entity, i18nCurrency);
        int num = i18nCurrencyDao.updateById(entity);
        return num == 1;
    }

    public Boolean updateStatus(Integer currencyId, DataStatus status) {
        I18nCurrencyEntity entity = i18nCurrencyDao.selectById(currencyId);
        if (entity == null || DataStatus.REMOVED.equals(entity.getStatus())) {
            log.error("delete I18nCurrency {} is delete", currencyId);
            throw ResourceException.buildDeleteException(this.getClass(), "I18nCurrency is delete", currencyId);
        }
        if (DataStatus.REMOVED.equals(status) && !DataStatus.OFFLINE.equals(entity.getStatus())) {
            log.error("delete I18nCurrency {} not delete, status is {}", currencyId, entity.getStatus());
            throw ResourceException.buildDeleteException(this.getClass(), "I18nCurrency {} not delete status is {}", currencyId, entity.getStatus());
        }

        List<I18nLanguageEntity> languages = i18nLanguageDao.selectByCurrency(currencyId);
        if (languages != null && !languages.isEmpty()) {
            throw ResourceException.buildDeleteException(this.getClass(), "I18nCurrency", entity.getCode());
        }

        entity.setStatus(status);
        int num = i18nCurrencyDao.updateById(entity);
        return num == 1;
    }


    public PageBody<I18nCurrency> pageCurrencies(FormQuery request) {
        PageBody<I18nCurrency> result = new PageBody<>();
        Page<I18nCurrencyEntity> queryPage = new Page<>(request.getPage(), request.getSize());

        QueryWrapper<I18nCurrencyEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<I18nCurrencyEntity> lambda = queryWrapper.lambda();
        lambda.in(I18nCurrencyEntity::getStatus, DataStatus.ONLINE, DataStatus.OFFLINE);
        if (StringUtils.isNotEmpty(request.getQueryInput())) {
            lambda.and(i->i.like(I18nCurrencyEntity::getName, request.getQueryInput())
                    .or().like(I18nCurrencyEntity::getCode, request.getQueryInput()));
        }
        if (StringUtils.isNotBlank(request.getSort())) {
            queryWrapper.orderBy(true, request.getAsc(),
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        }


        queryPage = i18nCurrencyDao.selectPage(queryPage, queryWrapper);
        if (queryPage != null) {
            result.setTotal(queryPage.getTotal());
            result.setCurrent(queryPage.getCurrent());
            result.setPageSize(queryPage.getSize());
            result.setTotalPage(queryPage.getPages());
            result.setList(CollectionUtils.isEmpty(queryPage.getRecords()) ? new ArrayList<I18nCurrency>() :
                    I18NCurrencyConverter.INSTANCE.entitiesToModels(queryPage.getRecords()));
        }
        return result;
    }

    public List<I18nCurrency> listCurrencies(DataStatus status) {
        QueryWrapper<I18nCurrencyEntity> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.lambda().eq(I18nCurrencyEntity::getStatus, status);
        } else {
            queryWrapper.lambda().in(I18nCurrencyEntity::getStatus, DataStatus.ONLINE, DataStatus.OFFLINE);
        }

        List<I18nCurrencyEntity> entityList = i18nCurrencyDao.selectList(queryWrapper);
        return I18NCurrencyConverter.INSTANCE.entitiesToModels(entityList);
    }

}
